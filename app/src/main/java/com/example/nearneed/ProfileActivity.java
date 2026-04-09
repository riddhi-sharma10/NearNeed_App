package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = RoleManager.getRole(this);
        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            setContentView(R.layout.layout_profile_provider);
            SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_PROFILE);
        } else {
            setContentView(R.layout.layout_profile_seeker);
            SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_PROFILE);
        }

        ProfileModeSwitcher.bind(this, findViewById(android.R.id.content), role);
        bindMenuClicks();
    }

    private void bindMenuClicks() {
        // Help & Support
        View seekerHelp = findViewById(R.id.menu_help);
        if (seekerHelp != null) {
            seekerHelp.setOnClickListener(v -> openHelpSupport());
        }

        View providerHelp = findViewById(R.id.menu_help_provider);
        if (providerHelp != null) {
            providerHelp.setOnClickListener(v -> openHelpSupport());
        }

        // Settings
        View seekerSettings = findViewById(R.id.menu_settings);
        if (seekerSettings != null) {
            seekerSettings.setOnClickListener(v -> openSettings());
        }

        View providerSettings = findViewById(R.id.menu_settings_provider);
        if (providerSettings != null) {
            providerSettings.setOnClickListener(v -> openSettings());
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openHelpSupport() {
        Intent intent = new Intent(this, HelpSupportActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
