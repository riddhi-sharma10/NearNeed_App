package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;

public class OtpVerifyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnVerify;
    private TextView tvCodeSentTo;
    private TextView btnResend;
    private TextView tvWrongNumber;
    private TextView tvTimer;

    private EditText[] otpBoxes = new EditText[6];
    private CountDownTimer countDownTimer;
    private static final long RESEND_COUNTDOWN_MS = 30_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge so gradient extends behind status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.activity_otp_verify);

        // Bind views
        btnBack       = findViewById(R.id.btnBack);
        btnVerify     = findViewById(R.id.btnVerify);
        btnResend     = findViewById(R.id.btnResend);
        tvCodeSentTo  = findViewById(R.id.tvCodeSentTo);
        tvWrongNumber = findViewById(R.id.tvWrongNumber);
        tvTimer       = findViewById(R.id.tvTimer);

        otpBoxes[0] = findViewById(R.id.otpBox1);
        otpBoxes[1] = findViewById(R.id.otpBox2);
        otpBoxes[2] = findViewById(R.id.otpBox3);
        otpBoxes[3] = findViewById(R.id.otpBox4);
        otpBoxes[4] = findViewById(R.id.otpBox5);
        otpBoxes[5] = findViewById(R.id.otpBox6);

        // Populate phone number from intent
        String phone = getIntent().getStringExtra("PHONE_NUMBER");
        if (phone != null && !phone.isEmpty()) {
            tvCodeSentTo.setText("Code sent to +91 " + phone);
        }

        setupOtpInputLogic();
        setupClickListeners();
        startResendCountdown();

        // Auto-focus first box and show keyboard
        otpBoxes[0].requestFocus();
        otpBoxes[0].postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(otpBoxes[0], InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  OTP Input — auto-advance + backspace-retreat
    // ─────────────────────────────────────────────────────────────────────────
    private void setupOtpInputLogic() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int idx = i;

            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && idx < otpBoxes.length - 1) {
                        otpBoxes[idx + 1].requestFocus();
                    }
                }
            });

            // Backspace key: clear current and move back
            otpBoxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && idx > 0
                        && otpBoxes[idx].getText().toString().isEmpty()) {
                    otpBoxes[idx - 1].requestFocus();
                    otpBoxes[idx - 1].setText("");
                    return true;
                }
                return false;
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Click Listeners
    // ─────────────────────────────────────────────────────────────────────────
    private void setupClickListeners() {

        btnBack.setOnClickListener(v -> onBackPressed());

        tvWrongNumber.setOnClickListener(v -> onBackPressed());

        btnResend.setOnClickListener(v -> {
            for (EditText box : otpBoxes) box.setText("");
            otpBoxes[0].requestFocus();
            startResendCountdown();
            Toast.makeText(this, "OTP resent successfully!", Toast.LENGTH_SHORT).show();
        });

        btnVerify.setOnClickListener(v -> {
            String otp = collectOtp();
            if (otp.length() < 6) {
                Toast.makeText(this, "Please enter the complete 6-digit OTP", Toast.LENGTH_SHORT).show();
                highlightFirstEmpty();
                return;
            }
            // TODO: Verify OTP with Firebase / backend here.
            // For now navigate forward.
            boolean isSignup = getIntent().getBooleanExtra("IS_SIGNUP", false);
            Intent intent;
            if (isSignup) {
                intent = new Intent(this, ProfileInfoActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
            if (!isSignup) finishAffinity(); else finish();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private String collectOtp() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) sb.append(box.getText().toString().trim());
        return sb.toString();
    }

    private void highlightFirstEmpty() {
        for (EditText box : otpBoxes) {
            if (box.getText().toString().isEmpty()) {
                box.requestFocus();
                break;
            }
        }
    }

    private void startResendCountdown() {
        btnResend.setEnabled(false);
        btnResend.setAlpha(0.4f);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(RESEND_COUNTDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secs = millisUntilFinished / 1000;
                tvTimer.setText("Resend available in " + secs + "s");
                tvTimer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                btnResend.setEnabled(true);
                btnResend.setAlpha(1.0f);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
