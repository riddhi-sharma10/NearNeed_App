package com.example.nearneed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileActivity extends AppCompatActivity {

    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = RoleManager.getRole(this);
        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            setContentView(R.layout.layout_profile_provider);
        } else {
            setContentView(R.layout.layout_profile_seeker);
        }

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_PROFILE);
        ProfileModeSwitcher.bind(this, findViewById(android.R.id.content), role);
        bindMenuClicks();

        // Instant display from local cache while Firestore loads
        applyLocalCache();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        profileListener = FirebaseFirestore.getInstance()
                .collection("Users").document(user.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    applySnapshot(snapshot);
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }

    private void applyLocalCache() {
        String name = UserPrefs.getName(this);
        String location = UserPrefs.getLocation(this);
        String photoUriStr = UserPrefs.getPhotoUri(this);

        setName(name);
        setLocation(location);

        if (photoUriStr != null) {
            // Could be a local content URI or a remote https URL — Glide handles both
            ImageView ivPhoto = findViewById(R.id.iv_profile_picture);
            TextView tvInitials = findViewById(R.id.tv_profile_initials);
            if (ivPhoto != null) {
                Glide.with(this).load(Uri.parse(photoUriStr)).circleCrop()
                        .into(ivPhoto);
                ivPhoto.setVisibility(View.VISIBLE);
                if (tvInitials != null) tvInitials.setVisibility(View.GONE);
            }
        }
    }

    private void applySnapshot(DocumentSnapshot snapshot) {
        String name = snapshot.getString("fullName");
        String location = snapshot.getString("location");
        String photoUrl = snapshot.getString("photoUrl");
        Boolean verified = snapshot.getBoolean("isVerified");

        // Persist to local cache for instant display next time
        if (name != null && !name.isEmpty()) UserPrefs.saveName(this, name);
        if (location != null && !location.isEmpty()) UserPrefs.saveLocation(this, location);
        if (photoUrl != null) UserPrefs.savePhotoUri(this, photoUrl);
        if (verified != null) UserPrefs.saveVerified(this, verified);

        setName(name != null ? name : "");
        setLocation(location != null ? location : "");

        ImageView ivPhoto = findViewById(R.id.iv_profile_picture);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        if (photoUrl != null && !photoUrl.isEmpty() && ivPhoto != null) {
            Glide.with(this).load(photoUrl).circleCrop().into(ivPhoto);
            ivPhoto.setVisibility(View.VISIBLE);
            if (tvInitials != null) tvInitials.setVisibility(View.GONE);
        } else if (ivPhoto != null) {
            ivPhoto.setVisibility(View.GONE);
            if (tvInitials != null) tvInitials.setVisibility(View.VISIBLE);
        }
    }

    private void setName(String name) {
        if (name == null || name.isEmpty()) return;

        TextView tvName = findViewById(R.id.tv_profile_name);
        if (tvName != null) {
            tvName.setText(name);
            VerifiedBadgeHelper.apply(this, tvName, UserPrefs.isVerified(this));
        }

        TextView tvInitials = findViewById(R.id.tv_profile_initials);
        if (tvInitials != null) {
            String[] parts = name.trim().split("\\s+");
            String initials = parts.length >= 2
                    ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
                    : String.valueOf(parts[0].charAt(0));
            tvInitials.setText(initials.toUpperCase());
        }
    }

    private void setLocation(String location) {
        if (location == null || location.isEmpty()) return;

        TextView tvLocSeeker = findViewById(R.id.tv_profile_location_seeker);
        if (tvLocSeeker != null) tvLocSeeker.setText(location);

        TextView tvLocProvider = findViewById(R.id.tv_profile_location);
        if (tvLocProvider != null) tvLocProvider.setText(location);
    }

    private void bindMenuClicks() {
        View myPosts = findViewById(R.id.menu_my_posts);
        if (myPosts != null) myPosts.setOnClickListener(v -> openMyPosts());

        View myJobs = findViewById(R.id.menu_my_jobs);
        if (myJobs != null) myJobs.setOnClickListener(v -> openMyPosts());

        View seekerHelp = findViewById(R.id.menu_help);
        if (seekerHelp != null) seekerHelp.setOnClickListener(v -> openHelpSupport());

        View providerHelp = findViewById(R.id.menu_help_provider);
        if (providerHelp != null) providerHelp.setOnClickListener(v -> openHelpSupport());

        View seekerSettings = findViewById(R.id.menu_settings);
        if (seekerSettings != null) seekerSettings.setOnClickListener(v -> openSettings());

        View providerSettings = findViewById(R.id.menu_settings_provider);
        if (providerSettings != null) providerSettings.setOnClickListener(v -> openSettings());

        View btnViewEarnings = findViewById(R.id.btn_view_earnings);
        if (btnViewEarnings != null) btnViewEarnings.setOnClickListener(v -> openEarnings());

        View seekerLogout = findViewById(R.id.menu_logout);
        if (seekerLogout != null) seekerLogout.setOnClickListener(v -> showLogoutDialog());

        View providerLogout = findViewById(R.id.menu_logout_provider);
        if (providerLogout != null) providerLogout.setOnClickListener(v -> showLogoutDialog());

        View btnEditProfileSeeker = findViewById(R.id.btn_edit_profile);
        if (btnEditProfileSeeker != null) btnEditProfileSeeker.setOnClickListener(v -> openEditProfile());

        View btnEditProfileProvider = findViewById(R.id.btn_edit_profile_provider);
        if (btnEditProfileProvider != null) btnEditProfileProvider.setOnClickListener(v -> openEditProviderProfile());

        View cardReviewsStat = findViewById(R.id.card_reviews_stat);
        if (cardReviewsStat != null) cardReviewsStat.setOnClickListener(v ->
                startActivity(new Intent(this, ReviewsActivity.class)));
    }

    private void openEditProfile() {
        startActivity(new Intent(this, EditProfileActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openEditProviderProfile() {
        startActivity(new Intent(this, EditProfileProviderActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showLogoutDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        view.findViewById(R.id.btn_logout_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        view.findViewById(R.id.btn_logout_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void openMyPosts() {
        startActivity(new Intent(this, MyPostsActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openHelpSupport() {
        startActivity(new Intent(this, HelpSupportActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openEarnings() {
        startActivity(new Intent(this, MyEarningsActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

