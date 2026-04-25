package com.example.nearneed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnContinue;
    private MaterialButton btnDetectLocation;
    private TextView tvDetectedLocation;

    private int selectedRadius = 10;
    
    private static final int LOCATION_PERMISSION_REQ = 101;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
            Intent intent = new Intent(this, IdVerificationActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnDetectLocation.setOnClickListener(v -> detectRealLocation());
    }

    private void detectRealLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ);
            return;
        }

        btnDetectLocation.setEnabled(false);
        btnDetectLocation.setText("Fetching Precise Location...");
        tvDetectedLocation.setText("Detecting...");
        tvDetectedLocation.setTextColor(0xFF64748B);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String detected = addresses.get(0).getAddressLine(0);
                        tvDetectedLocation.setText(detected);
                        tvDetectedLocation.setTextColor(0xFF0F172A);

                        btnDetectLocation.setText("Location Confirmed");
                        btnDetectLocation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF16A34A));
                        btnDetectLocation.setTextColor(android.graphics.Color.WHITE);
                        Toast.makeText(this, "Location set to: " + detected, Toast.LENGTH_SHORT).show();
                    } else {
                        showLocationError("Address not found");
                    }
                } catch (IOException e) {
                    showLocationError("Error getting address");
                }
            } else {
                showLocationError("Unable to get current location");
            }
        }).addOnFailureListener(e -> showLocationError("Location error: " + e.getMessage()));
    }

    private void showLocationError(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        btnDetectLocation.setEnabled(true);
        btnDetectLocation.setText("Retry Location");
        tvDetectedLocation.setText("Unknown Location");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectRealLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                btnDetectLocation.setEnabled(true);
                btnDetectLocation.setText("Use this location");
            }
        }
    }
}
