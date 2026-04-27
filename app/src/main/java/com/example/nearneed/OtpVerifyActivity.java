package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtpVerifyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnVerify;
    private TextView tvCodeSentTo;
    private TextView btnResend;

    private EditText[] otpBoxes = new EditText[6];
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        btnBack = findViewById(R.id.btnBack);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);
        tvCodeSentTo = findViewById(R.id.tvCodeSentTo);

        // Map boxes
        otpBoxes[0] = findViewById(R.id.otpBox1);
        otpBoxes[1] = findViewById(R.id.otpBox2);
        otpBoxes[2] = findViewById(R.id.otpBox3);
        otpBoxes[3] = findViewById(R.id.otpBox4);
        otpBoxes[4] = findViewById(R.id.otpBox5);
        otpBoxes[5] = findViewById(R.id.otpBox6);

        String phone = getIntent().getStringExtra("PHONE_NUMBER");
        if (phone != null) {
            tvCodeSentTo.setText("Code sent to +91 " + phone);
        }
        
        mVerificationId = getIntent().getStringExtra("VERIFICATION_ID");

        // Keep verify locked until all OTP digits are entered.
        btnVerify.setEnabled(false);
        btnVerify.setAlpha(0.6f);

        setupListeners();
        setupOtpInputLogic();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnVerify.setOnClickListener(v -> {
            StringBuilder codeBuilder = new StringBuilder();
            for (EditText otpBox : otpBoxes) {
                codeBuilder.append(otpBox.getText().toString());
            }

            String code = codeBuilder.toString();
            if (code.length() != 6) {
                Toast.makeText(this, "Please enter 6 digit code", Toast.LENGTH_SHORT).show();
                return;
            }

            verifySignInCode(code);
        });

        btnResend.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.txt_code_sent), Toast.LENGTH_SHORT).show();
            // TODO: In the future, re-trigger phoneAuth verification.
        });
    }

    private void verifySignInCode(String code) {
        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        if (mVerificationId != null && !mVerificationId.isEmpty() && !mVerificationId.equals("dummy_verification_id")) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        } else {
            Toast.makeText(this, "Verification ID missing. Try requesting a new OTP.", Toast.LENGTH_SHORT).show();
            btnVerify.setEnabled(true);
            btnVerify.setText("Verify");
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false);
                        checkIfUserExistsAndRoute(isSignup);
                    } else {
                        Toast.makeText(this, "Verification failed or Incorrect Code.", Toast.LENGTH_SHORT).show();
                        btnVerify.setEnabled(true);
                        btnVerify.setText("Verify");
                    }
                });
    }

    private void checkIfUserExistsAndRoute(boolean isSignup) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean userExists = doc.exists() && doc.getString("name") != null;
                    if (isSignup) {
                        if (userExists) {
                            // Account already registered — send to login via WelcomeActivity
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "An account already exists for this number. Please log in.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, WelcomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Genuinely new user — wipe stale cache, go to profile setup
                            UserPrefs.clear(this);
                            startActivity(new Intent(this, ProfileInfoActivity.class));
                            finish();
                        }
                    } else { // Login
                        if (userExists) {
                            // Valid login
                            startActivity(new Intent(this, AccountTypeActivity.class));
                            finish();
                        } else {
                            // No account exists — send to signup via WelcomeActivity
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "No account found for this number. Please sign up.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, WelcomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // If the check fails, proceed as a safe fallback based on action
                    if (isSignup) {
                        UserPrefs.clear(this);
                        startActivity(new Intent(this, ProfileInfoActivity.class));
                    } else {
                        startActivity(new Intent(this, AccountTypeActivity.class));
                    }
                    finish();
                });
    }

    private void setupOtpInputLogic() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int currentIndex = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpBoxes.length - 1) {
                        otpBoxes[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        otpBoxes[currentIndex - 1].requestFocus();
                    }

                    updateVerifyButtonState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updateVerifyButtonState() {
        boolean isOtpComplete = true;
        for (EditText otpBox : otpBoxes) {
            CharSequence value = otpBox.getText();
            if (value == null || value.length() != 1) {
                isOtpComplete = false;
                break;
            }
        }

        btnVerify.setEnabled(isOtpComplete);
        btnVerify.setAlpha(isOtpComplete ? 1.0f : 0.6f);
    }
}
