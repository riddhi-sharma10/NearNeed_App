package com.example.nearneed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class HomeSeekerNoPostsActivity extends AppCompatActivity {

    private TextView tvDeliveryLocation;
    private static final String PREFS = "LocationPrefs";
    private static final String KEY_LOCATION = "delivery_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_seeker_no_posts);

        // Connect the FAB to potentially a Create Post Activity (standard behavior)
        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
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

        EditText searchEdit = findViewById(R.id.searchEditText);
        DashboardSearchHelper.bindMapSearchShortcut(searchEdit, this);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), true);
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
