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

public class OtpVerifyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnVerify;
    private TextView tvCodeSentTo;
    private TextView btnResend;

    private EditText[] otpBoxes = new EditText[6];

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

        setupListeners();
        setupOtpInputLogic();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnVerify.setOnClickListener(v -> {
            boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false);
            Intent intent;
            if (isSignup) {
                // Redirect to Profile Setup (Step 1)
                intent = new Intent(this, ProfileInfoActivity.class);
            } else {
                // Redirect to Dashboard (Login flow)
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
            if (!isSignup) finishAffinity();
            else finish();
        });

        btnResend.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.txt_code_sent), Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
}
