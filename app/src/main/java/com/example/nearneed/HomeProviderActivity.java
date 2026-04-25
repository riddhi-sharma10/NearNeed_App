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
    private PostViewModel postViewModel;

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

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        setupObservers();

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
        nearbyRequestsAdapter = new NearbyRequestsAdapter();
        rvNearbyRequests.setAdapter(nearbyRequestsAdapter);
        setupCommunityVolunteering();
    }

    private void setupCommunityVolunteering() {
        RecyclerView rvCommunityVolunteering = findViewById(R.id.rvCommunityVolunteering);
        communityVolunteeringAdapter = new CommunityVolunteeringAdapter();
        if (rvCommunityVolunteering != null) {
            rvCommunityVolunteering.setAdapter(communityVolunteeringAdapter);
        }
    }

    private void setupObservers() {
        // Default radius 5km
        postViewModel.getNearbyPosts().observe(this, posts -> {
            List<Post> gigs = new ArrayList<>();
            List<Post> community = new ArrayList<>();
            for (Post p : posts) {
                if ("GIG".equals(p.type)) gigs.add(p);
                else if ("COMMUNITY".equals(p.type)) community.add(p);
            }
            nearbyRequestsAdapter.setPosts(gigs);
            communityVolunteeringAdapter.setPosts(community);
        });

        postViewModel.observeNearbyPosts(this, 28.4595, 77.0266, 5.0); // Default to Gurgaon if no loc
        
        userViewModel.getLocation().observe(this, loc -> {
            // Update radius when location changes if needed
            // For now just re-observe with fake coords or real ones if available
        });
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
