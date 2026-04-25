package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

public class HomeSeekerNoPostsActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDeliveryLocation;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_seeker_no_posts);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);

        // Show cached values immediately so there's no blank flash
        String cachedName = UserPrefs.getName(this);
        String cachedLocation = UserPrefs.getLocation(this);
        if (!cachedName.isEmpty()) {
            tvGreeting.setText("Hello, " + cachedName);
        }
        if (cachedLocation != null && !cachedLocation.isEmpty()) {
            tvDeliveryLocation.setText(cachedLocation);
        }

        // ViewModel drives real-time updates from Firestore
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getName().observe(this, name -> {
            tvGreeting.setText("Hello, " + name);
            UserPrefs.saveName(this, name);
        });
        userViewModel.getLocation().observe(this, location -> {
            tvDeliveryLocation.setText(location);
            UserPrefs.saveLocation(this, location);
        });

        // Location section click
        View locationSection = findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v -> showLocationPicker());
        }

        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));
        }

        EditText searchEdit = findViewById(R.id.searchEditText);
        DashboardSearchHelper.bindMapSearchShortcut(searchEdit, this);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), true);
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, location -> userViewModel.saveLocation(location));
    }
}
