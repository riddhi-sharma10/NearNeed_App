package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class HomeProviderActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDeliveryLocation;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    
    private NearbyRequestsAdapter nearbyRequestsAdapter;
    private CommunityVolunteeringAdapter communityVolunteeringAdapter;
    private ListenerRegistration badgeListener;
    private TextView tvDashboardNotificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium transparent status bar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_home_provider);

        // Bind Navbar - Provider uses Seeker Navbar for now as per instructions
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvDeliveryLocation = findViewById(R.id.tvDeliveryLocation);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupHeader();
        setupNearbyRequests();
        setupCommunityVolunteering();
        setupObservers();
        setupDashboardNotifications();
        setupRoleToggle();

        findViewById(R.id.viewAllRequestsContainer).setOnClickListener(v ->
            startActivity(new Intent(this, MapsActivity.class)));
            
        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v -> 
            startActivity(new Intent(this, MapsActivity.class))); // View Community visibility in Map
            
        findViewById(R.id.locationSection).setOnClickListener(v -> showLocationPicker());

        View fabAiChat = findViewById(R.id.fab_ai_chat);
        if (fabAiChat != null) {
            fabAiChat.setOnClickListener(v -> {
                startActivity(new Intent(this, AiChatActivity.class));
            });
        }
    }

    private void setupHeader() {
        String cachedName = UserPrefs.getName(this);
        if (!cachedName.isEmpty()) {
            tvGreeting.setText("Hello, " + cachedName);
        } else {
            tvGreeting.setText("Hello, Loading...");
        }

        userViewModel.getName().observe(this, name -> {
            if (name != null && !name.isEmpty() && !name.equals("Hello")) {
                tvGreeting.setText("Hello, " + name);
                UserPrefs.saveName(this, name);
            }
        });
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, (location, lat, lng) -> {
            userViewModel.saveLocation(lat, lng);
        });
    }

    private void setupNearbyRequests() {
        RecyclerView rvNearbyRequests = findViewById(R.id.rvNearbyRequests);
        nearbyRequestsAdapter = new NearbyRequestsAdapter();
        if (rvNearbyRequests != null) {
            rvNearbyRequests.setAdapter(nearbyRequestsAdapter);
        }
    }

    private void setupCommunityVolunteering() {
        RecyclerView rvCommunityVolunteering = findViewById(R.id.rvCommunityVolunteering);
        communityVolunteeringAdapter = new CommunityVolunteeringAdapter();
        if (rvCommunityVolunteering != null) {
            rvCommunityVolunteering.setAdapter(communityVolunteeringAdapter);
        }
    }

    private void setupObservers() {
        // Observe posts and filter by type (GIG vs COMMUNITY)
        postViewModel.getNearbyPosts().observe(this, posts -> {
            if (posts == null) return;
            List<Post> gigs = new ArrayList<>();
            List<Post> community = new ArrayList<>();
            for (Post p : posts) {
                if ("GIG".equalsIgnoreCase(p.type)) {
                    gigs.add(p);
                } else if ("COMMUNITY".equalsIgnoreCase(p.type)) {
                    community.add(p);
                }
            }
            nearbyRequestsAdapter.setPosts(gigs);
            communityVolunteeringAdapter.setPosts(community);
        });

        // Use observeAllActivePosts to ensure everything is synced immediately and globally
        postViewModel.observeAllActivePosts();

        // Observe user profile stats
        userViewModel.getLocation().observe(this, location -> {
            if (tvDeliveryLocation != null && location != null && !location.isEmpty()) {
                tvDeliveryLocation.setText(location);
            }
        });

        userViewModel.getMtdEarnings().observe(this, earnings -> {
            TextView tvEarnings = findViewById(R.id.tv_home_provider_earnings);
            if (tvEarnings != null) tvEarnings.setText(earnings);
        });

        userViewModel.getBookingsCount().observe(this, count -> {
            TextView tvJobs = findViewById(R.id.tv_home_provider_active_jobs);
            if (tvJobs != null) tvJobs.setText(String.valueOf(count));
        });

        userViewModel.getRating().observe(this, rating -> {
            TextView tvRating = findViewById(R.id.tv_home_provider_rating);
            if (tvRating != null) tvRating.setText(String.format("%.1f", rating));
        });
    }

    private void setupDashboardNotifications() {
        ImageView ivDashboardNotifications = findViewById(R.id.ivDashboardNotifications);
        tvDashboardNotificationBadge = findViewById(R.id.tvDashboardNotificationBadge);
        if (ivDashboardNotifications != null) {
            ivDashboardNotifications.setOnClickListener(v -> DashboardNotificationPopup.show(this, v, null));
        }

        // Real-time badge sync
        badgeListener = NotificationCenter.listenUnreadCount(count -> {
            if (tvDashboardNotificationBadge != null) {
                if (count > 0) {
                    tvDashboardNotificationBadge.setText(String.valueOf(count));
                    tvDashboardNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    tvDashboardNotificationBadge.setVisibility(View.GONE);
                }
            }
        });
        
        // Search bar binding
        View searchEdit = findViewById(R.id.searchEditText);
        View searchEmptyState = findViewById(R.id.tvSearchEmptyState);
        if (searchEdit instanceof EditText && searchEmptyState instanceof TextView) {
            DashboardSearchHelper.bindProviderSearch((EditText) searchEdit, nearbyRequestsAdapter, 
                communityVolunteeringAdapter, (TextView) searchEmptyState);
        }
    }

    private void setupRoleToggle() {
        findViewById(R.id.tab_seeker).setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        findViewById(R.id.tab_provider).setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));
    }

    private void switchRole(String newRole) {
        if (newRole.equals(RoleManager.getRole(this))) return;
        RoleManager.setRole(this, newRole);
        // Navigate back to MainActivity to handle role routing
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (badgeListener != null) {
            badgeListener.remove();
            badgeListener = null;
        }
    }
}
