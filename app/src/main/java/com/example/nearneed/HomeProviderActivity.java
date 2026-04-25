package com.example.nearneed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Replaced by {@link HomeFragment}.
 * Layout logic has been migrated; this class is kept only for back-compat.
 */
@Deprecated
public class HomeProviderActivity extends AppCompatActivity {

    private TextView tvDeliveryLocation;
    private TextView tvDashboardNotificationBadge;
    private ListenerRegistration notifListener;
    private NearbyRequestsAdapter nearbyRequestsAdapter;
    private CommunityVolunteeringAdapter communityVolunteeringAdapter;
    private static final String PREFS = "LocationPrefs";
    private static final String KEY_LOCATION = "delivery_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure user is in the correct role for this activity
        if (RoleManager.ROLE_SEEKER.equals(RoleManager.getRole(this))) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home_provider);

        // Setup role toggle
        setupRoleToggle();

        // Location picker setup
        tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);
        loadSavedLocation();

        View locationSection = findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v -> showLocationPicker());
        }

        setupDashboardNotifications();

        // Today's schedule → Calendar
        findViewById(R.id.viewCalendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarProviderActivity.class);
            startActivity(intent);
        });

        // Post community request - restricted to seeker mode
        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Switch to Seeker Mode")
                    .setMessage("To post a community request, please switch to Seeker mode.")
                    .setPositiveButton("Switch Now", (d, w) -> switchRole(RoleManager.ROLE_SEEKER))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Earnings card → My Earnings
        findViewById(R.id.earningsCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyEarningsActivity.class);
            startActivity(intent);
        });

        // Active Jobs card → Bookings (ongoing tab)
        View activeJobsCard = findViewById(R.id.statsGridActiveJobs);
        if (activeJobsCard != null) {
            activeJobsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("active_tab", "ongoing");
                startActivity(intent);
            });
        }

        // Rating card → Profile
        View ratingCard = findViewById(R.id.statsGridRating);
        if (ratingCard != null) {
            ratingCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // View All Requests → Map Activity
        findViewById(R.id.viewAllRequestsContainer).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        });

        // Setup nearby requests RecyclerView
        setupNearbyRequests();

        EditText searchEdit = findViewById(R.id.searchEditText);
        TextView searchEmptyState = findViewById(R.id.tvSearchEmptyState);
        DashboardSearchHelper.bindProviderSearch(searchEdit, nearbyRequestsAdapter, communityVolunteeringAdapter, searchEmptyState);

        // Bind the unified navbar – Home tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
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

    private void setupNearbyRequests() {
        RecyclerView rvNearbyRequests = findViewById(R.id.rvNearbyRequests);
        nearbyRequestsAdapter = new NearbyRequestsAdapter(new NearbyRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(int position) {
                // Handle accept
            }

            @Override
            public void onDecline(int position) {
                // Handle decline
            }
        });

        // Sample data
        List<NearbyRequestsAdapter.RequestItem> requests = new ArrayList<>();
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Leaky Faucet Repair",
            "0.8 km",
            "₹45 est",
            "Fixing leaky faucets in kitchen area. Water is dripping from underneath the sink.",
            R.drawable.ic_plumber
        ));
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Light Installation",
            "2.4 km",
            "₹80 est",
            "Installing LED lights in the bedroom. Need someone with basic electrical knowledge.",
            R.drawable.ic_electrician
        ));

        nearbyRequestsAdapter.setRequests(requests);
        rvNearbyRequests.setAdapter(nearbyRequestsAdapter);

        // Setup community volunteering section
        setupCommunityVolunteering();
    }

    private void setupCommunityVolunteering() {
        RecyclerView rvCommunityVolunteering = findViewById(R.id.rvCommunityVolunteering);
        communityVolunteeringAdapter = new CommunityVolunteeringAdapter();

        // Sample community posts
        List<CommunityVolunteeringAdapter.CommunityPost> communityPosts = new ArrayList<>();
        communityPosts.add(new CommunityVolunteeringAdapter.CommunityPost(
            "Park Cleanup Drive",
            "Sarah Johnson",
            "Help us clean and beautify the neighborhood park. Bring gloves and energy!",
            "0.6 km away",
            "8 volunteers needed"
        ));
        communityPosts.add(new CommunityVolunteeringAdapter.CommunityPost(
            "Free Coaching Session",
            "Rajesh Kumar",
            "Teaching basic English to underprivileged kids. Make a difference!",
            "1.5 km away",
            "3 volunteers needed"
        ));

        communityVolunteeringAdapter.setPosts(communityPosts);
        if (rvCommunityVolunteering != null) {
            rvCommunityVolunteering.setAdapter(communityVolunteeringAdapter);
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

    private void setupDashboardNotifications() {
        ImageView ivDashboardNotifications = findViewById(R.id.ivDashboardNotifications);
        tvDashboardNotificationBadge = findViewById(R.id.tvDashboardNotificationBadge);

        if (ivDashboardNotifications != null) {
            ivDashboardNotifications.setOnClickListener(v ->
                DashboardNotificationPopup.show(this, v, null)
            );
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

        // Update visual state to show Provider is active
        updateToggleAppearance();
    }

    /**
     * Updates the visual state of the toggle to reflect current role.
     */
    private void updateToggleAppearance() {
        TextView tabSeeker = findViewById(R.id.tab_seeker);
        TextView tabProvider = findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setBackground(null);
            tabSeeker.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        }

        if (tabProvider != null) {
            tabProvider.setBackgroundResource(R.drawable.bg_seeker_tab_active);
            tabProvider.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
        }
    }
}
