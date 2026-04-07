package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnContinue;
    private MaterialButton btnDetectLocation;
    private TextView tvDetectedLocation;

    private int selectedRadius = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);
        btnDetectLocation = findViewById(R.id.btnDetectLocation);
        tvDetectedLocation = findViewById(R.id.tvDetectedLocation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityPreferencesActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnDetectLocation.setOnClickListener(v -> simulateLocationDetection());
    }


    private void simulateLocationDetection() {
        Toast.makeText(this, "Requesting location permission...", Toast.LENGTH_SHORT).show();

        btnDetectLocation.setEnabled(false);
        btnDetectLocation.setText("Fetching Precise Location...");
        tvDetectedLocation.setText("Detecting...");
        tvDetectedLocation.setTextColor(0xFF64748B);

        new Handler().postDelayed(() -> {
            tvDetectedLocation.setText("Triangulating GPS...");

            new Handler().postDelayed(() -> {
                String detected = "BML Munjal University, Kaphera";
                tvDetectedLocation.setText(detected);
                tvDetectedLocation.setTextColor(0xFF0F172A);

                btnDetectLocation.setText("Location Confirmed");
                btnDetectLocation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF16A34A));
                btnDetectLocation.setTextColor(android.graphics.Color.WHITE);

                Toast.makeText(this, "Location set to: " + detected, Toast.LENGTH_SHORT).show();
            }, 1500);
        }, 1000);
    }
}
