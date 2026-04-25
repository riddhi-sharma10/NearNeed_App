package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.ListenerRegistration;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeSeekerActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDeliveryLocation;
    private TextView tvDashboardNotificationBadge;
    private ListenerRegistration notifListener;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_home_seeker);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);

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

        setupRoleToggle();
        setupCommunityButtons();
        DashboardSearchHelper.bindSeekerSearch(findViewById(android.R.id.content), true, this);

        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, PostOptionsActivity.class)));
        }

        setupDashboardNotifications();
    }

    @Override
    protected void onStart() {
        super.onStart();
        notifListener = NotificationCenter.listenUnreadCount(this::updateBadge);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notifListener != null) {
            notifListener.remove();
            notifListener = null;
        }
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, location -> userViewModel.saveLocation(location));
    }

    private void setupDashboardNotifications() {
        ImageView ivDashboardNotifications = findViewById(R.id.ivDashboardNotifications);
        tvDashboardNotificationBadge = findViewById(R.id.tvDashboardNotificationBadge);

        if (ivDashboardNotifications != null) {
            ivDashboardNotifications.setOnClickListener(v ->
                DashboardNotificationPopup.show(this, v, null));
        }
    }

    private void updateBadge(int count) {
        if (tvDashboardNotificationBadge == null) return;
        if (count <= 0) {
            tvDashboardNotificationBadge.setVisibility(View.GONE);
            return;
        }
        tvDashboardNotificationBadge.setVisibility(View.VISIBLE);
        tvDashboardNotificationBadge.setText(String.valueOf(Math.min(count, 9)));
    }

    private void setupCommunityButtons() {
        View btnViewAllGigs = findViewById(R.id.btnViewAllGigs);
        if (btnViewAllGigs != null) {
            btnViewAllGigs.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("filter_type", "gigs");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View viewAllCommunity = findViewById(R.id.btn_view_all_community);
        if (viewAllCommunity != null) {
            viewAllCommunity.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("filter_type", "community");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View btnViewPostGig1 = findViewById(R.id.btn_view_post_gig_1);
        View btnViewPostGig2 = findViewById(R.id.btn_view_post_gig_2);
        if (btnViewPostGig1 != null) {
            btnViewPostGig1.setOnClickListener(v -> showGigPostDetail("Plumbing Repair",
                "Fixing leaky faucets and clogged drains for the community.",
                "Plumbing • Urgent", "₹400 - 600", "0.5 km away", "2-3 hours"));
        }
        if (btnViewPostGig2 != null) {
            btnViewPostGig2.setOnClickListener(v -> showGigPostDetail("Electrical Fix",
                "Wiring inspection and circuit breaker repairs nearby.",
                "Electrical • Normal", "₹800 - 1200", "1.2 km away", "3-4 hours"));
        }

        View btnHelp1 = findViewById(R.id.btn_help_community_1);
        View btnHelp2 = findViewById(R.id.btn_help_community_2);
        if (btnHelp1 != null) {
            btnHelp1.setOnClickListener(v -> showCommunityPostDetail("Grocery Assistance",
                "A neighbor needs help picking out fresh groceries for her weekly meals.",
                "Community Member", "2 hours ago", "0.4 km away", "5 volunteers needed"));
        }
        if (btnHelp2 != null) {
            btnHelp2.setOnClickListener(v -> showCommunityPostDetail("Tech Setup Help",
                "A neighbor needs assistance setting up a new computer and installing software.",
                "Community Member", "5 hours ago", "1.2 km away", "3 volunteers needed"));
        }
    }

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

    private void showSuccessDialog() {
        Dialog dialog = new MaterialAlertDialogBuilder(this)
            .setTitle("You're in!")
            .setMessage("Your response has been sent. Redirecting to home...")
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) dialog.dismiss();
            finish();
        }, 3000);
    }

    private void setupRoleToggle() {
        TextView tabSeeker = findViewById(R.id.tab_seeker);
        TextView tabProvider = findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        }
        if (tabProvider != null) {
            tabProvider.setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));
        }
        updateToggleAppearance();
    }

    private void switchRole(String newRole) {
        if (newRole.equals(RoleManager.getRole(this))) return;

        RoleManager.setRole(this, newRole);
        String msg = RoleManager.ROLE_SEEKER.equals(newRole)
            ? "Switched to Seeker mode" : "Switched to Provider mode";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

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
