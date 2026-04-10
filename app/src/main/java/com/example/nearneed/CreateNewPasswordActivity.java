package com.example.nearneed;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateNewPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;

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

        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> validateAndUpdate());
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
        if (newPass.length() < 8) {
            tilNewPassword.setError("Password must be at least 8 characters");
            return;
        }
        if (!newPass.matches(".*\\d.*")) {
            tilNewPassword.setError("Password must contain at least one number");
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
        // Return to EditProfileActivity (clear this activity from the stack)
        finish();
    }
}
