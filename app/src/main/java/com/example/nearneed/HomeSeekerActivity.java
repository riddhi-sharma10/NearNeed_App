package com.example.nearneed;

import android.app.Dialog;
import android.content.Intent;
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
    private View emptyStateContainer;
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
        
        if (!cachedName.isEmpty()) {
            tvGreeting.setText("Hello, " + cachedName);
        } else {
            tvGreeting.setText("Hello, Loading...");
        }
        
        if (cachedLocation != null && !cachedLocation.isEmpty()) {
            tvDeliveryLocation.setText(cachedLocation);
        } else {
            tvDeliveryLocation.setText("Loading location...");
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
                tvDeliveryLocation.setText(location);
                UserPrefs.saveLocation(this, location);
            }
        });

        findViewById(R.id.locationSection).setOnClickListener(v -> showLocationPicker());
        
        setupRoleToggle();
        setupRecyclerViews();
        setupFAB();
        setupDashboardNotifications();
        setupObservers();
    }

    private void setupRecyclerViews() {
        rvMyGigs = findViewById(R.id.rvMyGigPosts);
        rvCommunity = findViewById(R.id.rvCommunityNeeds);
        emptyStateContainer = findViewById(R.id.empty_state_container);
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
    }

    private void setupObservers() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null 
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (!userId.isEmpty()) {
            postViewModel.getUserPosts().observe(this, posts -> {
                List<Post> myGigs = new ArrayList<>();
                for (Post p : posts) {
                    if ("GIG".equalsIgnoreCase(p.type)) myGigs.add(p);
                }
                gigsAdapter.setPosts(myGigs);
                
                if (myGigs.isEmpty()) {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    postsContentContainer.setVisibility(View.GONE);
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    postsContentContainer.setVisibility(View.VISIBLE);
                }
            });
            postViewModel.observeUserPosts(this, userId);
        }

        // Observe global active posts for Community Needs to ensure immediate sync from all users
        postViewModel.getNearbyPosts().observe(this, posts -> {
            if (posts == null) return;
            List<Post> communityPosts = new ArrayList<>();
            for (Post p : posts) {
                if ("COMMUNITY".equalsIgnoreCase(p.type)) {
                    communityPosts.add(p);
                }
            }
            communityAdapter.setPosts(communityPosts);
        });
        
        // Sync globally to ensure all community posts appear immediately
        postViewModel.observeAllActivePosts();
    }

    private void setupFAB() {
        MaterialButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, PostOptionsActivity.class)));
        }
    }

    private void showLocationPicker() {
        LocationPickerHelper.show(this, (location, lat, lng) -> userViewModel.saveLocation(lat, lng));
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
            holder.tvBadge.setText(post.status != null ? post.status.toUpperCase() : "ACTIVE");
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), GigPostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                intent.putExtra("title", post.title);
                intent.putExtra("category", post.category);
                intent.putExtra("budget", post.budget);
                intent.putExtra("description", post.description);
                intent.putExtra("distance", post.distance);
                intent.putExtra("duration", post.duration);
                intent.putExtra("address", post.location);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return posts.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetail, tvBadge;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_post_title);
                tvDetail = v.findViewById(R.id.tv_post_detail);
                tvBadge = v.findViewById(R.id.tv_post_badge);
            }
        }
    }
}
