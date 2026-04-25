package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeProviderActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDeliveryLocation;
    private TextView tvDashboardNotificationBadge;
    private ListenerRegistration notifListener;
    private NearbyRequestsAdapter nearbyRequestsAdapter;
    private CommunityVolunteeringAdapter communityVolunteeringAdapter;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (RoleManager.ROLE_SEEKER.equals(RoleManager.getRole(this))) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home_provider);

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
        setupDashboardNotifications();

        findViewById(R.id.viewCalendar).setOnClickListener(v ->
            startActivity(new Intent(this, CalendarProviderActivity.class)));

        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                .setTitle("Switch to Seeker Mode")
                .setMessage("To post a community request, please switch to Seeker mode.")
                .setPositiveButton("Switch Now", (d, w) -> switchRole(RoleManager.ROLE_SEEKER))
                .setNegativeButton("Cancel", null)
                .show());

        findViewById(R.id.earningsCard).setOnClickListener(v ->
            startActivity(new Intent(this, MyEarningsActivity.class)));

        View activeJobsCard = findViewById(R.id.statsGridActiveJobs);
        if (activeJobsCard != null) {
            activeJobsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, BookingsActivity.class);
                intent.putExtra("active_tab", "ongoing");
                startActivity(intent);
            });
        }

        View ratingCard = findViewById(R.id.statsGridRating);
        if (ratingCard != null) {
            ratingCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
        }

        findViewById(R.id.viewAllRequestsContainer).setOnClickListener(v ->
            startActivity(new Intent(this, MapsActivity.class)));

        setupNearbyRequests();

        EditText searchEdit = findViewById(R.id.searchEditText);
        TextView searchEmptyState = findViewById(R.id.tvSearchEmptyState);
        DashboardSearchHelper.bindProviderSearch(searchEdit, nearbyRequestsAdapter,
            communityVolunteeringAdapter, searchEmptyState);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content),
            SeekerNavbarController.TAB_HOME);
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

    private void setupNearbyRequests() {
        RecyclerView rvNearbyRequests = findViewById(R.id.rvNearbyRequests);
        nearbyRequestsAdapter = new NearbyRequestsAdapter(new NearbyRequestsAdapter.OnRequestActionListener() {
            @Override public void onAccept(int position) {}
            @Override public void onDecline(int position) {}
        });

        List<NearbyRequestsAdapter.RequestItem> requests = new ArrayList<>();
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Leaky Faucet Repair", "0.8 km", "₹45 est",
            "Fixing leaky faucets in kitchen area. Water is dripping from underneath the sink.",
            R.drawable.ic_plumber));
        requests.add(new NearbyRequestsAdapter.RequestItem(
            "Light Installation", "2.4 km", "₹80 est",
            "Installing LED lights in the bedroom. Need someone with basic electrical knowledge.",
            R.drawable.ic_electrician));

        nearbyRequestsAdapter.setRequests(requests);
        rvNearbyRequests.setAdapter(nearbyRequestsAdapter);

        setupCommunityVolunteering();
    }

    private void setupCommunityVolunteering() {
        RecyclerView rvCommunityVolunteering = findViewById(R.id.rvCommunityVolunteering);
        communityVolunteeringAdapter = new CommunityVolunteeringAdapter();

        List<CommunityVolunteeringAdapter.CommunityPost> communityPosts = new ArrayList<>();
        communityPosts.add(new CommunityVolunteeringAdapter.CommunityPost(
            "Park Cleanup Drive", "Community Member",
            "Help us clean and beautify the neighborhood park. Bring gloves and energy!",
            "0.6 km away", "8 volunteers needed"));
        communityPosts.add(new CommunityVolunteeringAdapter.CommunityPost(
            "Free Coaching Session", "Community Member",
            "Teaching basic English to underprivileged kids. Make a difference!",
            "1.5 km away", "3 volunteers needed"));

        communityVolunteeringAdapter.setPosts(communityPosts);
        if (rvCommunityVolunteering != null) {
            rvCommunityVolunteering.setAdapter(communityVolunteeringAdapter);
        }
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

    private void setupRoleToggle() {
        TextView tabSeeker = findViewById(R.id.tab_seeker);
        TextView tabProvider = findViewById(R.id.tab_provider);

        if (tabSeeker != null) tabSeeker.setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        if (tabProvider != null) tabProvider.setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));

        updateToggleAppearance();
    }

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
