package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateAccountActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnSubmit;
    private TextInputEditText etFullName, etEmail, etPassword;
    private TextInputLayout tilFullName, tilEmail, tilPassword;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSubmit.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean isValid = true;

            tilFullName.setError(null);
            tilEmail.setError(null);
            tilPassword.setError(null);

            if (name.isEmpty()) {
                tilFullName.setError("Please enter your name");
                isValid = false;
            }

            if (email.isEmpty()) {
                tilEmail.setError("Please enter your email");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Please enter a valid email");
                isValid = false;
            }

            if (password.isEmpty()) {
                tilPassword.setError("Please create a password");
                isValid = false;
            } else if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).+$")) {
                tilPassword.setError("Password must be alphanumeric and include at least one special character");
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            Intent intent = new Intent(CreateAccountActivity.this, OtpEnterActivity.class);
            intent.putExtra("IS_SIGNUP", true);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, OtpEnterActivity.class);
            intent.putExtra("IS_SIGNUP", false);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }
}
