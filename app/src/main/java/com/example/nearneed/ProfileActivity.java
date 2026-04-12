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
        // Posts & Jobs
        View myPosts = findViewById(R.id.menu_my_posts);
        if (myPosts != null) {
            myPosts.setOnClickListener(v -> openMyPosts());
        }

        View myJobs = findViewById(R.id.menu_my_jobs);
        if (myJobs != null) {
            myJobs.setOnClickListener(v -> openMyPosts());
        }

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

        // Earnings
        View menuEarnings = findViewById(R.id.menu_earnings);
        if (menuEarnings != null) {
            menuEarnings.setOnClickListener(v -> openEarnings());
        }

        View btnViewEarnings = findViewById(R.id.btn_view_earnings);
        if (btnViewEarnings != null) {
            btnViewEarnings.setOnClickListener(v -> openEarnings());
        }

        // Logout
        View seekerLogout = findViewById(R.id.menu_logout);
        if (seekerLogout != null) {
            seekerLogout.setOnClickListener(v -> showLogoutDialog());
        }

        View providerLogout = findViewById(R.id.menu_logout_provider);
        if (providerLogout != null) {
            providerLogout.setOnClickListener(v -> showLogoutDialog());
        }

        // Edit Profile
        View btnEditProfileSeeker = findViewById(R.id.btn_edit_profile);
        if (btnEditProfileSeeker != null) {
            btnEditProfileSeeker.setOnClickListener(v -> openEditProfile());
        }

        // On provider mode it might be named differently
        View btnEditProfileProvider = findViewById(R.id.btn_edit_profile_provider);
        if (btnEditProfileProvider != null) {
            btnEditProfileProvider.setOnClickListener(v -> openEditProviderProfile());
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openEditProviderProfile() {
        Intent intent = new Intent(this, EditProfileProviderActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLogoutDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null);
        dialog.setContentView(view);
        
        // Ensure transparent background for custom padding shadow effect
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        view.findViewById(R.id.btn_logout_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        view.findViewById(R.id.btn_logout_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openMyPosts() {
        Intent intent = new Intent(this, MyPostsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    private void openEarnings() {
        Intent intent = new Intent(this, MyEarningsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
