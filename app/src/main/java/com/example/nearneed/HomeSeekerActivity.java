package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeSeekerActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvDeliveryLocation;
    private TextView tvDashboardNotificationBadge;
    private ListenerRegistration notifListener;
    private UserViewModel userViewModel;
    private PostViewModel postViewModel;
    
    private RecyclerView rvMyGigs;
    private RecyclerView rvCommunity;
    private View emptyStateContainer, emptyStateCommunityContainer;
    private View postsContentContainer;
    
    private DashboardGigsAdapter gigsAdapter;
    private CommunityVolunteeringAdapter communityAdapter;

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

        // Show cached values
        String cachedName = UserPrefs.getName(this);
        String cachedLocation = UserPrefs.getLocation(this);
        
        if (cachedName != null && !cachedName.isEmpty()) {
            tvGreeting.setText("Hello, " + cachedName);
        } else {
            tvGreeting.setText("Hello, Loading...");
        }
        
        if (cachedLocation != null && !cachedLocation.isEmpty()) {
            tvDeliveryLocation.setText("DELIVER TO: " + cachedLocation.toUpperCase());
        } else {
            tvDeliveryLocation.setText("DELIVER TO: LOADING...");
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        userViewModel.getName().observe(this, name -> {
            if (name != null && !name.isEmpty() && !name.equals("Hello")) {
                tvGreeting.setText("Hello, " + name);
                UserPrefs.saveName(this, name);
            }
        });
        userViewModel.getLocation().observe(this, location -> {
            if (location != null && !location.isEmpty() && !location.contains("Fetching")) {
                tvDeliveryLocation.setText("DELIVER TO: " + location.toUpperCase());
                UserPrefs.saveLocation(this, location);
            }
        });

        findViewById(R.id.locationSection).setOnClickListener(v -> showLocationPicker());
        
        setupRoleToggle();
        setupRecyclerViews();
        setupFAB();
        setupDashboardNotifications();
        setupObservers();

        View fabAiChat = findViewById(R.id.fab_ai_chat);
        if (fabAiChat != null) {
            fabAiChat.setOnClickListener(v -> {
                openAcceptedProviderChat();
            });
        }

        checkAndFetchLocation();
    }

    private static final int LOCATION_PERMISSION_REQ = 101;
    
    private void checkAndFetchLocation() {
        String cachedLocation = UserPrefs.getLocation(this);
        if (cachedLocation == null || cachedLocation.isEmpty() || cachedLocation.contains("Loading")) {
            if (LocationHelper.hasLocationPermissions(this)) {
                fetchLocationSilently();
            } else {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ);
            }
        }
    }

    private void fetchLocationSilently() {
        tvDeliveryLocation.setText("DELIVER TO: FETCHING...");
        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                GeocodingHelper.reverseGeocode(HomeSeekerActivity.this, lat, lng, new GeocodingHelper.OnAddressResolvedListener() {
                    @Override
                    public void onAddressResolved(String address) {
                        userViewModel.saveLocation(address, lat, lng);
                        tvDeliveryLocation.setText("DELIVER TO: " + address.toUpperCase());
                    }
                    @Override
                    public void onFailure() {
                        tvDeliveryLocation.setText("DELIVER TO: UNKNOWN");
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                tvDeliveryLocation.setText("DELIVER TO: UNKNOWN");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQ) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fetchLocationSilently();
            } else {
                tvDeliveryLocation.setText("DELIVER TO: UNKNOWN");
            }
        }
    }

    private void openAcceptedProviderChat() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        // Query for the most recent active booking (upcoming or in_progress)
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("seekerId", currentUid)
                .whereIn("status", java.util.Arrays.asList("upcoming", "in_progress", "confirmed"))
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        String providerId = doc.getString("providerId");
                        String providerName = doc.getString("providerName");
                        
                        if (providerId != null) {
                            ChatBottomSheet.newInstance(providerId, providerName != null ? providerName : "Provider", currentUid, providerId)
                                    .show(getSupportFragmentManager(), "ChatBottomSheet");
                        } else {
                            startActivity(new Intent(this, AiChatActivity.class));
                        }
                    } else {
                        // Fallback to AI Chat if no accepted provider found
                        startActivity(new Intent(this, AiChatActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, AiChatActivity.class));
                });
    }

    private void setupRecyclerViews() {
        rvMyGigs = findViewById(R.id.rvMyGigPosts);
        rvCommunity = findViewById(R.id.rvCommunityNeeds);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        emptyStateCommunityContainer = findViewById(R.id.empty_state_container_community);
        postsContentContainer = findViewById(R.id.posts_content_container);

        rvMyGigs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        gigsAdapter = new DashboardGigsAdapter();
        rvMyGigs.setAdapter(gigsAdapter);

        rvCommunity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        communityAdapter = new CommunityVolunteeringAdapter();
        rvCommunity.setAdapter(communityAdapter);
        
        findViewById(R.id.btnViewAllGigs).setOnClickListener(v -> startActivity(new Intent(this, MyPostsActivity.class)));
        findViewById(R.id.btn_view_all_community).setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingsActivity.class);
            intent.putExtra("filter_type", "community");
            startActivity(intent);
        });
        
        if (findViewById(R.id.btn_post_now_empty) != null) {
            findViewById(R.id.btn_post_now_empty).setOnClickListener(v -> 
                startActivity(new Intent(this, PostOptionsActivity.class)));
        }

        if (findViewById(R.id.btn_post_community_empty) != null) {
            findViewById(R.id.btn_post_community_empty).setOnClickListener(v ->
                startActivity(new Intent(this, PostOptionsActivity.class)));
        }
    }

    private void setupObservers() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (!userId.isEmpty()) {
            postViewModel.getUserPosts().observe(this, posts -> {
                if (posts == null) return;
                List<Post> myGigs = new ArrayList<>();
                List<Post> myCommunity = new ArrayList<>();
                for (Post p : posts) {
                    if ("GIG".equalsIgnoreCase(p.type)) myGigs.add(p);
                    else if ("COMMUNITY".equalsIgnoreCase(p.type)) myCommunity.add(p);
                }
                gigsAdapter.setPosts(myGigs);
                communityAdapter.setPosts(myCommunity);

                if (myGigs.isEmpty()) {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    rvMyGigs.setVisibility(View.GONE);
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    rvMyGigs.setVisibility(View.VISIBLE);
                }

                if (myCommunity.isEmpty()) {
                    if (emptyStateCommunityContainer != null) emptyStateCommunityContainer.setVisibility(View.VISIBLE);
                    if (rvCommunity != null) rvCommunity.setVisibility(View.GONE);
                } else {
                    if (emptyStateCommunityContainer != null) emptyStateCommunityContainer.setVisibility(View.GONE);
                    if (rvCommunity != null) rvCommunity.setVisibility(View.VISIBLE);
                }
            });
            postViewModel.observeUserPosts(this, userId);
        }
    }

    private void setupFAB() {
        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, PostOptionsActivity.class)));
        }
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, (location, lat, lng) -> userViewModel.saveLocation(location, lat, lng));
    }

    private void setupDashboardNotifications() {
        ImageView ivDashboardNotifications = findViewById(R.id.ivDashboardNotifications);
        tvDashboardNotificationBadge = findViewById(R.id.tvDashboardNotificationBadge);
        if (ivDashboardNotifications != null) {
            ivDashboardNotifications.setOnClickListener(v -> DashboardNotificationPopup.show(this, v, () -> {
                // Potential callback logic
            }));
        }

        // Real-time badge sync
        notifListener = NotificationCenter.listenUnreadCount(count -> {
            if (tvDashboardNotificationBadge != null) {
                if (count > 0) {
                    tvDashboardNotificationBadge.setText(String.valueOf(count));
                    tvDashboardNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    tvDashboardNotificationBadge.setVisibility(View.GONE);
                }
            }
        });
        
        // Bind search
        DashboardSearchHelper.bindSeekerSearch(findViewById(android.R.id.content), gigsAdapter, communityAdapter, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notifListener != null) {
            notifListener.remove();
            notifListener = null;
        }
    }

    private void setupRoleToggle() {
        findViewById(R.id.tab_seeker).setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        findViewById(R.id.tab_provider).setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));
    }

    private void switchRole(String newRole) {
        if (newRole.equals(RoleManager.getRole(this))) return;
        RoleManager.setRole(this, newRole);
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private static class DashboardGigsAdapter extends RecyclerView.Adapter<DashboardGigsAdapter.ViewHolder> {
        private List<Post> posts = new ArrayList<>();

        void setPosts(List<Post> posts) {
            this.posts = posts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
            // Adjust width for horizontal scroll
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = (int) (260 * parent.getContext().getResources().getDisplayMetrics().density);
            view.setLayoutParams(lp);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.tvTitle.setText(post.title);
            holder.tvDetail.setText(post.budget != null ? "Budget: " + post.budget : "Gig");
            
            String status = post.status != null ? post.status.toUpperCase() : "ACTIVE";
            holder.tvBadge.setText(status);

            // Set dummy image based on category
            int dummyResId = getDummyImageForCategory(post.category);
            holder.ivPostImage.setImageResource(dummyResId);
            
            // Status-based coloring
            if ("COMPLETED".equals(status)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_pill_completed_soft);
                holder.tvBadge.setTextColor(Color.parseColor("#059669"));
            } else if ("CANCELLED".equals(status)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_pill_cancelled_soft);
                holder.tvBadge.setTextColor(Color.parseColor("#DC2626"));
            } else {
                // Active/Urgent style
                holder.tvBadge.setBackgroundResource(R.drawable.bg_urgent_badge);
                holder.tvBadge.setTextColor(Color.parseColor("#DC2626"));
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), GigPostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                intent.putExtra("title", post.title);
                intent.putExtra("category", post.category);
                intent.putExtra("budget", post.budget);
                intent.putExtra("description", post.description);
                intent.putExtra("distance", post.distance);
                intent.putExtra("address", post.location);
                v.getContext().startActivity(intent);
            });
        }

        private int getDummyImageForCategory(String category) {
            if (category == null) return R.drawable.welcome_bg_optimized;
            String lower = category.toLowerCase();
            if (lower.contains("plumb")) return R.drawable.img_gig_hero_plumbing;
            if (lower.contains("clean")) return R.drawable.img_gig_hero_cleaning;
            if (lower.contains("electr")) return R.drawable.img_gig_hero_electrical;
            if (lower.contains("pet") || lower.contains("dog")) return R.drawable.img_dog_walking;
            if (lower.contains("garden") || lower.contains("lawn") || lower.contains("mow")) return R.drawable.img_lawn_mowing;
            return R.drawable.welcome_bg_optimized;
        }

        @Override
        public int getItemCount() { return posts.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetail, tvBadge;
            ImageView ivPostImage;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_post_title);
                tvDetail = v.findViewById(R.id.tv_post_detail);
                tvBadge = v.findViewById(R.id.tv_post_badge);
                ivPostImage = v.findViewById(R.id.iv_post_image);
            }
        }
    }
}
