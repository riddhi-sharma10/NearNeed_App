package com.example.nearneed;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CommunityVolunteeringAdapter extends RecyclerView.Adapter<CommunityVolunteeringAdapter.CommunityViewHolder> {

    public static class CommunityPost {
        public String title;
        public String postedBy;
        public String description;
        public String location;
        public String slotsNeeded;

        public CommunityPost(String title, String postedBy, String description, String location, String slotsNeeded) {
            this.title = title;
            this.postedBy = postedBy;
            this.description = description;
            this.location = location;
            this.slotsNeeded = slotsNeeded;
        }
    }

    private List<CommunityPost> posts = new ArrayList<>();
    private List<CommunityPost> allPosts = new ArrayList<>();

    public void setPosts(List<CommunityPost> posts) {
        this.allPosts = new ArrayList<>(posts);
        this.posts = new ArrayList<>(posts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            posts = new ArrayList<>(allPosts);
            notifyDataSetChanged();
            return;
        }

        String lower = query.trim().toLowerCase();
        List<CommunityPost> filtered = new ArrayList<>();
        for (CommunityPost post : allPosts) {
            if ((post.title != null && post.title.toLowerCase().contains(lower))
                    || (post.postedBy != null && post.postedBy.toLowerCase().contains(lower))
                    || (post.description != null && post.description.toLowerCase().contains(lower))
                    || (post.location != null && post.location.toLowerCase().contains(lower))
                    || (post.slotsNeeded != null && post.slotsNeeded.toLowerCase().contains(lower))) {
                filtered.add(post);
            }
        }

        posts = filtered;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommunityViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        CommunityPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class CommunityViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvPostedBy;
        private TextView tvDescription;
        private MaterialButton btnView;

        public CommunityViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCommunityTitle);
            tvPostedBy = itemView.findViewById(R.id.tvPostedBy);
            tvDescription = itemView.findViewById(R.id.tvCommunityDescription);
            btnView = itemView.findViewById(R.id.btnViewCommunity);
        }

        public void bind(CommunityPost post) {
            tvTitle.setText(post.title);
            tvPostedBy.setText(post.postedBy);
            tvDescription.setText(post.description);

            boolean isProvider = RoleManager.isProvider(itemView.getContext());
            btnView.setText(isProvider ? "Volunteer" : "View Responses");

            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), CommunityPostDetailActivity.class);
                intent.putExtra("title", post.title);
                intent.putExtra("description", post.description);
                intent.putExtra("postedBy", post.postedBy);
                intent.putExtra("location", post.location);
                intent.putExtra("slots", post.slotsNeeded);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
