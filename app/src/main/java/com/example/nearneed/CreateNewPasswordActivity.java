package com.example.nearneed;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateNewPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnUpdatePassword;

    private TextView tvPasswordLength, tvPasswordUppercase, tvPasswordLowercase, tvPasswordNumber, tvPasswordSpecial;

    private boolean hasLength = false, hasUppercase = false, hasLowercase = false, hasNumber = false, hasSpecial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarNewPassword);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Initialize validation checklist views
        tvPasswordLength = findViewById(R.id.tvPasswordLength);
        tvPasswordUppercase = findViewById(R.id.tvPasswordUppercase);
        tvPasswordLowercase = findViewById(R.id.tvPasswordLowercase);
        tvPasswordNumber = findViewById(R.id.tvPasswordNumber);
        tvPasswordSpecial = findViewById(R.id.tvPasswordSpecial);

        // Add text watcher for real-time validation
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordRealTime(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnUpdatePassword.setOnClickListener(v -> validateAndUpdate());
    }

    private void validatePasswordRealTime(String password) {
        // Check length (8+ characters)
        hasLength = password.length() >= 8;
        updateValidationView(tvPasswordLength, hasLength, "8+ characters");

        // Check uppercase
        hasUppercase = password.matches(".*[A-Z].*");
        updateValidationView(tvPasswordUppercase, hasUppercase, "Uppercase letter");

        // Check lowercase
        hasLowercase = password.matches(".*[a-z].*");
        updateValidationView(tvPasswordLowercase, hasLowercase, "Lowercase letter");

        // Check number
        hasNumber = password.matches(".*[0-9].*");
        updateValidationView(tvPasswordNumber, hasNumber, "Number");

        // Check special character
        hasSpecial = password.matches(".*[!@#$%^&*].*");
        updateValidationView(tvPasswordSpecial, hasSpecial, "Special character (!@#$%^&*)");

        // Enable/disable button based on all requirements met
        updateButtonState();
    }

    private void updateValidationView(TextView tv, boolean isMet, String text) {
        if (isMet) {
            tv.setText("✓ " + text);
            tv.setTextColor(getColor(R.color.community_green));
        } else {
            tv.setText("✗ " + text);
            tv.setTextColor(getColor(R.color.text_muted));
        }
    }

    private void updateButtonState() {
        boolean isPasswordValid = hasLength && hasUppercase && hasLowercase && hasNumber && hasSpecial;
        btnUpdatePassword.setEnabled(isPasswordValid);

        if (isPasswordValid) {
            btnUpdatePassword.setBackgroundTintList(getColorStateList(R.color.brand_primary));
        } else {
            btnUpdatePassword.setBackgroundTintList(getColorStateList(R.color.text_muted));
        }
    }

    private void validateAndUpdate() {
        String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (TextUtils.isEmpty(newPass)) {
            tilNewPassword.setError("Enter new password");
            return;
        }

        if (!validatePassword(newPass)) {
            tilNewPassword.setError("Password does not meet all requirements");
            return;
        }

        if (TextUtils.isEmpty(confirmPass)) {
            tilConfirmPassword.setError("Confirm your password");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validatePassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*].*");
    }
}
