package com.example.nearneed;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class NearbyRequestsAdapter extends RecyclerView.Adapter<NearbyRequestsAdapter.RequestViewHolder> {

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
        for (Post item : allPosts) {
            if ((item.title != null && item.title.toLowerCase().contains(lower))
                    || (item.description != null && item.description.toLowerCase().contains(lower))) {
                filtered.add(item);
            }
        }

        posts = filtered;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle, tvDistance, tvDescription;
        private MaterialButton btnView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnView = itemView.findViewById(R.id.btnView);
        }

        public void bind(Post post) {
            tvJobTitle.setText(post.title);
            tvDistance.setText("Nearby");
            tvDescription.setText(post.description);

            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), RequestDetailActivity.class);
                intent.putExtra("post_id", post.postId);
                intent.putExtra("title", post.title);
                intent.putExtra("type", post.type);
                intent.putExtra("creator_id", post.createdBy);
                intent.putExtra("description", post.description);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
