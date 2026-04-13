package com.example.nearneed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class HomeSeekerActivity extends AppCompatActivity {

    private TextView tvDeliveryLocation;
    private static final String PREFS = "LocationPrefs";
    private static final String KEY_LOCATION = "delivery_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_home_seeker);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);

        // Connect FAB to PostOptionsActivity (or CreatePostActivity)
        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, PostOptionsActivity.class);
                startActivity(intent);
            });
        }

        // Location picker setup
        tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);
        loadSavedLocation();

        View locationSection = findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v -> showLocationPicker());
        }
    }

    private void loadSavedLocation() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(KEY_LOCATION, null);
        if (saved != null && tvDeliveryLocation != null) {
            tvDeliveryLocation.setText(saved);
        }
    }

    private void saveLocation(String displayText) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LOCATION, displayText);
        editor.apply();

        if (tvDeliveryLocation != null) {
            tvDeliveryLocation.setText(displayText);
        }
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, displayText -> saveLocation(displayText));
    }
}
