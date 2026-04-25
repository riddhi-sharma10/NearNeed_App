package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpEnterActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnSendOtp;
    private EditText etPhone;

    private FirebaseAuth mAuth;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_enter);

        if (!NearNeedApp.isFirebaseReady()) {
            Toast.makeText(this, "Firebase not configured. Please contact support.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        etPhone = findViewById(R.id.etPhone);

        setupListeners();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!phone.matches("\\d{10}")) {
                Toast.makeText(this, getString(R.string.txt_please_enter_a_valid_10_digit), Toast.LENGTH_SHORT).show();
                return;
            }

            sendVerificationCode(phone);
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            // Devices that auto-verify. For now, we will still push them to Verify Activity
            // with the verified credential code if available, but let's just complete sign in.
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.w("PhoneAuth", "onVerificationFailed", e);
            Toast.makeText(OtpEnterActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnSendOtp.setEnabled(true);
            btnSendOtp.setText("Get OTP");
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d("PhoneAuth", "onCodeSent:" + verificationId);

            mVerificationId = verificationId;

            boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false);
            String phone = etPhone.getText().toString().trim();

            Intent intent = new Intent(OtpEnterActivity.this, OtpVerifyActivity.class);
            intent.putExtra("IS_SIGNUP", isSignup);
            intent.putExtra("PHONE_NUMBER", phone);
            intent.putExtra("VERIFICATION_ID", mVerificationId);

            startActivity(intent);
            finish();
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false);
                        Intent intent;
                        if (isSignup) {
                            intent = new Intent(OtpEnterActivity.this, ProfileInfoActivity.class);
                        } else {
                            intent = new Intent(OtpEnterActivity.this, AccountTypeActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OtpEnterActivity.this, "Sign in failed.", Toast.LENGTH_SHORT).show();
                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Get OTP");
                    }
                });
    }
}
