package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeSeekerActivity extends AppCompatActivity {

    private TextView tvDeliveryLocation;
    private TextView tvDashboardNotificationBadge;
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

        // Setup role toggle
        setupRoleToggle();

        // Setup community volunteer buttons
        setupCommunityButtons();

        DashboardSearchHelper.bindSeekerSearch(findViewById(android.R.id.content), true, this);

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

        setupDashboardNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotificationBadge();
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

    private void setupDashboardNotifications() {
        ImageView ivDashboardNotifications = findViewById(R.id.ivDashboardNotifications);
        tvDashboardNotificationBadge = findViewById(R.id.tvDashboardNotificationBadge);

        if (ivDashboardNotifications != null) {
            ivDashboardNotifications.setOnClickListener(v ->
                DashboardNotificationPopup.show(this, v, this::refreshNotificationBadge)
            );
        }

        refreshNotificationBadge();
    }

    private void refreshNotificationBadge() {
        if (tvDashboardNotificationBadge == null) {
            return;
        }

        int unread = NotificationCenter.unreadCount(this);
        if (unread <= 0) {
            tvDashboardNotificationBadge.setVisibility(View.GONE);
            return;
        }

        tvDashboardNotificationBadge.setVisibility(View.VISIBLE);
        tvDashboardNotificationBadge.setText(String.valueOf(Math.min(unread, 9)));
    }

    /**
     * Sets up community volunteer buttons and My Posts view all.
     */
    private void setupCommunityButtons() {
        // Wire "View All" My Posts → Bookings (gigs only)
        View btnViewAllGigs = findViewById(R.id.btnViewAllGigs);
        if (btnViewAllGigs != null) {
            btnViewAllGigs.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("filter_type", "gigs");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Wire "View All" community → Bookings (community only)
        View viewAllCommunity = findViewById(R.id.btn_view_all_community);
        if (viewAllCommunity != null) {
            viewAllCommunity.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("filter_type", "community");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Wire gig post buttons → GigPostDetailActivity
        View btnViewPostGig1 = findViewById(R.id.btn_view_post_gig_1);
        View btnViewPostGig2 = findViewById(R.id.btn_view_post_gig_2);
        if (btnViewPostGig1 != null) {
            btnViewPostGig1.setOnClickListener(v -> showGigPostDetail("Plumbing Repair",
                    "Fixing leaky faucets and clogged drains for the community.",
                    "Plumbing • Urgent",
                    "₹400 - 600",
                    "0.5 km away",
                    "2-3 hours"));
        }
        if (btnViewPostGig2 != null) {
            btnViewPostGig2.setOnClickListener(v -> showGigPostDetail("Electrical Fix",
                    "Wiring inspection and circuit breaker repairs for Sector 15.",
                    "Electrical • Normal",
                    "₹800 - 1200",
                    "1.2 km away",
                    "3-4 hours"));
        }

        // Wire community card buttons → CommunityPostDetailActivity
        View btnHelp1 = findViewById(R.id.btn_help_community_1);
        View btnHelp2 = findViewById(R.id.btn_help_community_2);
        if (btnHelp1 != null) {
            btnHelp1.setOnClickListener(v -> showCommunityPostDetail("Grocery Assistance",
                    "Sarah J. needs help picking out fresh groceries for her weekly meals.",
                    "Sarah J.",
                    "2 hours ago",
                    "0.4 miles away",
                    "5 volunteers needed"));
        }
        if (btnHelp2 != null) {
            btnHelp2.setOnClickListener(v -> showCommunityPostDetail("Tech Setup Help",
                    "David M. needs assistance setting up his new computer and installing software.",
                    "David M.",
                    "5 hours ago",
                    "1.2 miles away",
                    "3 volunteers needed"));
        }
    }

    /**
     * Navigates to GigPostDetailActivity with gig details.
     */
    private void showGigPostDetail(String title, String description, String category,
                                   String budget, String distance, String duration) {
        Intent intent = new Intent(this, GigPostDetailActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("category", category);
        intent.putExtra("budget", budget);
        intent.putExtra("distance", distance);
        intent.putExtra("duration", duration);
        startActivity(intent);
    }

    /**
     * Navigates to CommunityPostDetailActivity with community post details.
     */
    private void showCommunityPostDetail(String title, String description, String postedBy,
                                         String postedTime, String location, String slots) {
        Intent intent = new Intent(this, CommunityPostDetailActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("postedBy", postedBy);
        intent.putExtra("postedTime", postedTime);
        intent.putExtra("location", location);
        intent.putExtra("slots", slots);
        startActivity(intent);
    }

    /**
     * Shows the volunteer message bottom sheet.
     */
    private void showVolunteerSheet(String postTitle) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_community_respond_sheet, null);
        dialog.setContentView(sheetView);

        MaterialButton applyBtn = sheetView.findViewById(R.id.btn_apply_volunteer);
        if (applyBtn != null) {
            applyBtn.setOnClickListener(v -> {
                dialog.dismiss();
                showSuccessDialog();
            });
        }

        dialog.show();
    }

    /**
     * Shows success dialog and auto-closes after 3 seconds.
     */
    private void showSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("You're in!")
                .setMessage("Your response has been sent. Redirecting to home...")
                .setIcon(android.R.drawable.ic_dialog_info);

        Dialog dialog = builder.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            finish();
        }, 3000);
    }

    /**
     * Sets up the role toggle (Seeker/Provider) button functionality.
     */
    private void setupRoleToggle() {
        TextView tabSeeker = findViewById(R.id.tab_seeker);
        TextView tabProvider = findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        }

        if (tabProvider != null) {
            tabProvider.setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));
        }

        // Update visual state to show Seeker is active
        updateToggleAppearance();
    }

    /**
     * Switches role and navigates to appropriate home screen.
     */
    private void switchRole(String newRole) {
        if (newRole.equals(RoleManager.getRole(this))) {
            return; // Already in this role
        }

        // Persist choice
        RoleManager.setRole(this, newRole);

        // Show toast notification
        String msg = RoleManager.ROLE_SEEKER.equals(newRole)
                ? "Switched to Seeker mode"
                : "Switched to Provider mode";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity (dispatcher)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Updates the visual state of the toggle to reflect current role.
     */
    private void updateToggleAppearance() {
        TextView tabSeeker = findViewById(R.id.tab_seeker);
        TextView tabProvider = findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setBackgroundResource(R.drawable.bg_seeker_tab_active);
            tabSeeker.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
        }

        if (tabProvider != null) {
            tabProvider.setBackground(null);
            tabProvider.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }
    }
}
