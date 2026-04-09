package com.example.nearneed;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupClickListeners();
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_save_settings).setOnClickListener(v -> {
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btn_language).setOnClickListener(v -> 
            Toast.makeText(this, "Language selection coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btn_delete_account).setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes, continue", (dialog, which) -> navigateToSignup())
                .setNegativeButton("No, take me back", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void navigateToSignup() {
        android.content.Intent intent = new android.content.Intent(this, WelcomeActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
