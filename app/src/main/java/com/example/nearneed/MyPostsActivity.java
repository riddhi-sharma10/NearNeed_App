package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private MyPostsAdapter adapter;
    private PostViewModel viewModel;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.rvMyPosts);
        layoutEmpty = findViewById(R.id.layout_empty);
        
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyPostsAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        setupViewModel();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);
        
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getUserPosts().observe(this, posts -> {
            adapter.setPosts(posts);
            updateEmptyState(posts.isEmpty());
        });

        viewModel.observeUserPosts(this, userId);
    }

    private void updateEmptyState(boolean isEmpty) {
        if (layoutEmpty == null) return;
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            ImageView ivIllustration = layoutEmpty.findViewById(R.id.ivEmptyIllustration);
            if (ivIllustration != null) {
                ivIllustration.setImageResource(R.drawable.img_empty_posts);
            }
            TextView tvTitle = layoutEmpty.findViewById(R.id.tvEmptyTitle);
            if (tvTitle != null) {
                tvTitle.setText("You haven't posted anything yet");
            }
            TextView tvMessage = layoutEmpty.findViewById(R.id.tvEmptyMessage);
            if (tvMessage != null) {
                tvMessage.setText("Your requests for help or gig opportunities will appear here.");
            }
            View btnAction = layoutEmpty.findViewById(R.id.btnEmptyAction);
            if (btnAction != null) {
                btnAction.setOnClickListener(v -> {
                    startActivity(new Intent(this, PostOptionsActivity.class));
                });
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    private class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
        private List<Post> posts;

        MyPostsAdapter(List<Post> posts) {
            this.posts = posts;
        }

        void setPosts(List<Post> posts) {
            this.posts = posts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Post post = posts.get(position);
            holder.tvTitle.setText(post.title);
            
            String detail;
            if ("GIG".equals(post.type)) {
                detail = post.budget != null && !post.budget.isEmpty() ? "Budget: " + post.budget : "Paid Gig";
            } else {
                detail = (post.slotsFilled != null ? post.slotsFilled : 0) + "/" + 
                         (post.slots != null ? post.slots : 0) + " Volunteers";
            }
            holder.tvDetail.setText(detail);

            // Set dummy image based on category
            int dummyResId = getDummyImageForCategory(post.category);
            holder.ivPostImage.setImageResource(dummyResId);
            
            holder.tvBadge.setText(post.type != null ? post.type : "POST");

            // Apply badge colors based on category/type
            if ("High Urgency".equalsIgnoreCase(post.category) || "URGENT".equalsIgnoreCase(post.type)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_urgent_badge);
                holder.tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(MyPostsActivity.this, R.color.urgent_red));
                holder.tvBadge.setText("URGENT");
            } else if ("COMMUNITY".equalsIgnoreCase(post.type)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_community_badge);
                holder.tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(MyPostsActivity.this, R.color.community_green));
                holder.tvBadge.setText("COMMUNITY");
            } else {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_paid_badge);
                holder.tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(MyPostsActivity.this, R.color.brand_primary));
                holder.tvBadge.setText("PAID GIG");
            }

            holder.btnAction.setOnClickListener(v -> {
                if ("COMMUNITY".equalsIgnoreCase(post.type)) {
                    Intent intent = new Intent(MyPostsActivity.this, VolunteersActivity.class);
                    intent.putExtra("post_id", post.postId);
                    intent.putExtra("post_title", post.title);
                    intent.putExtra("max_slots", post.volunteersNeeded != null ? post.volunteersNeeded : 0);
                    intent.putExtra("is_seeker", true);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MyPostsActivity.this, ResponsesActivity.class);
                    intent.putExtra("post_id", post.postId);
                    intent.putExtra("post_title", post.title);
                    intent.putExtra("is_gig", true);
                    startActivity(intent);
                }
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
        public int getItemCount() {
            return posts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetail, tvBadge, btnAction;
            ImageView ivPostImage;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_post_title);
                tvDetail = itemView.findViewById(R.id.tv_post_detail);
                tvBadge = itemView.findViewById(R.id.tv_post_badge);
                btnAction = itemView.findViewById(R.id.btn_action_link);
                ivPostImage = itemView.findViewById(R.id.iv_post_image);
            }
        }
    }
}
