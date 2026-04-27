package com.example.nearneed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.material.button.MaterialButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentOtpActivity extends AppCompatActivity {

    private static final int SMS_CONSENT_REQUEST = 200;

    private String phone;
    private String bookingId, serviceName, providerName, completionNotes;
    private double serviceAmount;
    private int userRating;

    private EditText[] otpBoxes;
    private MaterialButton btnVerify;
    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_otp_verify);

        phone           = getIntent().getStringExtra("phone");
        bookingId       = getIntent().getStringExtra("booking_id");
        serviceName     = getIntent().getStringExtra("service_name");
        providerName    = getIntent().getStringExtra("provider_name");
        serviceAmount   = getIntent().getDoubleExtra("service_amount", 0);
        userRating      = getIntent().getIntExtra("user_rating", 0);
        completionNotes = getIntent().getStringExtra("completion_notes");

        TextView tvCodeSentTo = findViewById(R.id.tvCodeSentTo);
        if (phone != null && !phone.isEmpty()) {
            tvCodeSentTo.setText("Code sent to +91 " + phone);
        }

        otpBoxes = new EditText[]{
                findViewById(R.id.otpBox1), findViewById(R.id.otpBox2),
                findViewById(R.id.otpBox3), findViewById(R.id.otpBox4),
                findViewById(R.id.otpBox5), findViewById(R.id.otpBox6)
        };

        setupOtpBoxes();

        btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(v -> proceedToSuccess());

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnResend).setOnClickListener(v ->
                Toast.makeText(this, "OTP already sent via payment gateway", Toast.LENGTH_SHORT).show());

        // Start SmsUserConsent to auto-read the OTP SMS sent by Razorpay
        startSmsUserConsent();
    }

    // ── OTP box wiring ──────────────────────────────────────────────────────────

    private void setupOtpBoxes() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int idx = i;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && idx < otpBoxes.length - 1)
                        otpBoxes[idx + 1].requestFocus();
                    if (getOtp().length() == 6) proceedToSuccess();
                }
            });
            otpBoxes[i].setOnKeyListener((v, code, ev) -> {
                if (code == KeyEvent.KEYCODE_DEL
                        && ev.getAction() == KeyEvent.ACTION_DOWN
                        && otpBoxes[idx].getText().toString().isEmpty()
                        && idx > 0) {
                    otpBoxes[idx - 1].requestFocus();
                    otpBoxes[idx - 1].setText("");
                    return true;
                }
                return false;
            });
        }
        otpBoxes[0].requestFocus();
    }

    private String getOtp() {
        StringBuilder sb = new StringBuilder();
        for (EditText b : otpBoxes) sb.append(b.getText().toString().trim());
        return sb.toString();
    }

    // ── SMS User Consent (autofill) ─────────────────────────────────────────────

    private void startSmsUserConsent() {
        // Start listening for ONE incoming SMS
        SmsRetriever.getClient(this).startSmsUserConsent(null);

        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                    Bundle extras = intent.getExtras();
                    if (extras == null) return;
                    Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                    if (status == null) return;
                    if (status.getStatusCode() == CommonStatusCodes.SUCCESS) {
                        // Show consent dialog so user can approve reading the SMS
                        Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                        if (consentIntent != null) {
                            try {
                                startActivityForResult(consentIntent, SMS_CONSENT_REQUEST);
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SMS_CONSENT_REQUEST && resultCode == RESULT_OK && data != null) {
            String smsText = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
            if (smsText != null) {
                String otp = extractOtp(smsText);
                if (otp != null && otp.length() == 6) {
                    fillOtp(otp);
                }
            }
        }
    }

    /** Extract first 4–8 digit number from SMS text */
    private String extractOtp(String smsText) {
        Matcher m = Pattern.compile("\\b(\\d{4,8})\\b").matcher(smsText);
        return m.find() ? m.group(1) : null;
    }

    /** Fill all OTP boxes with the given string */
    private void fillOtp(String otp) {
        for (int i = 0; i < Math.min(otp.length(), otpBoxes.length); i++) {
            otpBoxes[i].setText(String.valueOf(otp.charAt(i)));
        }
        if (otp.length() == otpBoxes.length) {
            otpBoxes[otpBoxes.length - 1].requestFocus();
        }
    }

    // ── Navigate to success ─────────────────────────────────────────────────────

    private void proceedToSuccess() {
        if (getOtp().length() != 6) {
            Toast.makeText(this, "Please enter the complete 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        // Whatever OTP the user entered → proceed to success (no real verification needed)
        Intent intent = new Intent(this, ProcessingPaymentActivity.class);
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("provider_name", providerName);
        intent.putExtra("service_amount", serviceAmount);
        intent.putExtra("user_rating", userRating);
        intent.putExtra("completion_notes", completionNotes);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            try { unregisterReceiver(smsReceiver); } catch (Exception ignored) {}
        }
    }
}
