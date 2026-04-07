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
    private MaterialButton btnUseLocation;
    private AutoCompleteTextView etSearchLocation;
    private ProgressBar pbLocationDetecting;
    private ImageView ivLocationPin;
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
        btnUseLocation = findViewById(R.id.btnUseLocation);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        pbLocationDetecting = findViewById(R.id.pbLocationDetecting);
        ivLocationPin = findViewById(R.id.ivLocationPin);
        tvDetectedLocation = findViewById(R.id.tvDetectedLocation);

        setupSearchSuggestions();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityPreferencesActivity.class);
            startActivity(intent);
        });

        btnUseLocation.setOnClickListener(v -> simulateLocationDetection());

        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchLocation.getText().toString().trim();
                if (!query.isEmpty()) {
                    Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void setupSearchSuggestions() {
        String[] mockLocations = {
            "Gurugram, Haryana",
            "Sohna Road, Gurugram",
            "BML Munjal University, Kaphera",
            "MG Road, Gurugram",
            "Cyber Hub, Gurugram",
            "Sector 14, Gurugram",
            "IFFCO Chowk, Gurugram",
            "Indira Gandhi International Airport, Delhi",
            "Vasant Kunj, New Delhi",
            "Saket, New Delhi",
            "Hauz Khas, New Delhi"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, mockLocations);
        etSearchLocation.setAdapter(adapter);
    }

    private void simulateLocationDetection() {
        Toast.makeText(this, "Requesting location permission...", Toast.LENGTH_SHORT).show();

        btnUseLocation.setEnabled(false);
        btnUseLocation.setText("Fetching Precise Location...");
        pbLocationDetecting.setVisibility(View.VISIBLE);
        ivLocationPin.setVisibility(View.GONE);
        tvDetectedLocation.setText("Detecting...");
        tvDetectedLocation.setTextColor(0xFF64748B);

        new Handler().postDelayed(() -> {
            tvDetectedLocation.setText("Triangulating GPS...");

            new Handler().postDelayed(() -> {
                pbLocationDetecting.setVisibility(View.GONE);
                ivLocationPin.setVisibility(View.VISIBLE);

                String detected = "BML Munjal University, Kaphera";
                tvDetectedLocation.setText(detected);
                tvDetectedLocation.setTextColor(0xFF0F172A);

                btnUseLocation.setText("Location Confirmed");
                btnUseLocation.setEnabled(false);
                btnUseLocation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF16A34A));
                btnUseLocation.setTextColor(android.graphics.Color.WHITE);

                Toast.makeText(this, "Location set to: " + detected, Toast.LENGTH_SHORT).show();
                etSearchLocation.setText("");
            }, 1500);
        }, 1000);
    }
}
