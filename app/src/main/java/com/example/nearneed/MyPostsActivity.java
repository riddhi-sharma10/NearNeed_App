package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

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

        RecyclerView rv = findViewById(R.id.rvMyPosts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);
        rv.setItemViewCacheSize(20);

        List<MyPost> posts = buildMockPosts();

        rv.setAdapter(new MyPostsAdapter(posts));
    }

    private List<MyPost> buildMockPosts() {
        List<MyPost> posts = new ArrayList<>();

        String[][] seed = new String[][]{
                {"Emergency Food Delivery", "Food Support • 0/4 Volunteers", "URGENT", "URGENT"},
                {"Plumbing Repair", "₹400 budget", "PAID GIG", "PAID"},
                {"Garden Drive Block B", "Active • 12 Volunteers", "COMMUNITY", "COMMUNITY"},
                {"Dog Walking (Eve)", "₹200/walk", "PAID GIG", "PAID"},
                {"Senior App Assistance", "Community Support", "COMMUNITY", "COMMUNITY"},
                {"Night Watch Group", "5 Volunteers needed", "URGENT", "URGENT"},
                {"Medicine Pickup", "Urgent • 2 volunteers needed", "URGENT", "URGENT"},
                {"Pet Boarding", "₹900 total", "PAID GIG", "PAID"},
                {"Weekend Cleanup Drive", "Community • 18 joined", "COMMUNITY", "COMMUNITY"},
                {"Emergency Ride Needed", "Urgent • 1 driver", "URGENT", "URGENT"}
        };

        for (int i = 0; i < 4; i++) {
            for (String[] row : seed) {
                String title = i == 0 ? row[0] : row[0] + " #" + (i + 1);
                posts.add(new MyPost(title, row[1], row[2], row[3]));
            }
        }
        return posts;
    }

    private static class MyPost {
        String title, detail, badge, type;

        MyPost(String title, String detail, String badge, String type) {
            this.title = title;
            this.detail = detail;
            this.badge = badge;
            this.type = type; // URGENT, PAID, COMMUNITY
        }
    }

    private class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
        private final List<MyPost> posts;

        MyPostsAdapter(List<MyPost> posts) {
            this.posts = posts;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            MyPost post = posts.get(position);
            return (post.title + post.detail + post.type).hashCode();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MyPost post = posts.get(position);
            holder.tvTitle.setText(post.title);
            holder.tvDetail.setText(post.detail);
            holder.tvBadge.setText(post.badge);

            // Apply badge colors based on type
            if ("URGENT".equals(post.type)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_urgent_badge);
                holder.tvBadge.setTextColor(getColor(R.color.urgent_red));
            } else if ("COMMUNITY".equals(post.type)) {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_community_badge);
                holder.tvBadge.setTextColor(getColor(R.color.community_green));
            } else {
                holder.tvBadge.setBackgroundResource(R.drawable.bg_paid_badge);
                holder.tvBadge.setTextColor(getColor(R.color.brand_primary));
            }

            holder.btnAction.setOnClickListener(v -> 
                Toast.makeText(MyPostsActivity.this, "Viewing details for: " + post.title, Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetail, tvBadge, btnAction;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_post_title);
                tvDetail = itemView.findViewById(R.id.tv_post_detail);
                tvBadge = itemView.findViewById(R.id.tv_post_badge);
                btnAction = itemView.findViewById(R.id.btn_action_link);
            }
        }
    }
}
