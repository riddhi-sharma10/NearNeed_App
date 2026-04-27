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



    private List<Post> posts = new ArrayList<>();
    private List<Post> allPosts = new ArrayList<>();

    public void setPosts(List<Post> posts) {
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
        List<Post> filtered = new ArrayList<>();
        for (Post post : allPosts) {
            if ((post.title != null && post.title.toLowerCase().contains(lower))
                    || (post.postedBy != null && post.postedBy.toLowerCase().contains(lower))
                    || (post.description != null && post.description.toLowerCase().contains(lower))
                    || (post.location != null && post.location.toLowerCase().contains(lower))) {
                filtered.add(post);
            }
        }

        posts = filtered;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post_card, parent, false);
        // Adjust width for horizontal scroll
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = (int) (300 * parent.getContext().getResources().getDisplayMetrics().density);
        view.setLayoutParams(lp);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        Post post = posts.get(position);
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

        public void bind(Post post) {
            tvTitle.setText(post.title);
            tvPostedBy.setText(post.postedBy != null ? post.postedBy : "Community Member");
            tvDescription.setText(post.description);

            boolean isProvider = RoleManager.isProvider(itemView.getContext());
            btnView.setText(isProvider ? "Volunteer" : "View Responses");

            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), CommunityPostDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                intent.putExtra("creator_id", post.createdBy);
                intent.putExtra("title", post.title);
                intent.putExtra("description", post.description);
                intent.putExtra("postedBy", post.postedBy);
                intent.putExtra("location", post.location);
                intent.putExtra("slots", post.slots != null ? post.slots.toString() : "0");
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
