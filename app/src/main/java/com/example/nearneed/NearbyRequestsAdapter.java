package com.example.nearneed;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class NearbyRequestsAdapter extends RecyclerView.Adapter<NearbyRequestsAdapter.RequestViewHolder> {



    private List<Post> requests = new ArrayList<>();
    private List<Post> allRequests = new ArrayList<>();
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(int position);
        void onDecline(int position);
    }

    public NearbyRequestsAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<Post> requests) {
        this.allRequests = new ArrayList<>(requests);
        this.requests = new ArrayList<>(requests);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            requests = new ArrayList<>(allRequests);
            notifyDataSetChanged();
            return;
        }

        String lower = query.trim().toLowerCase();
        List<Post> filtered = new ArrayList<>();
        for (Post item : allRequests) {
            if ((item.title != null && item.title.toLowerCase().contains(lower))
                    || (item.description != null && item.description.toLowerCase().contains(lower))
                    || (item.distance != null && item.distance.toLowerCase().contains(lower))
                    || (item.budget != null && item.budget.toLowerCase().contains(lower))) {
                filtered.add(item);
            }
        }

        requests = filtered;
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
        Post item = requests.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle;
        private TextView tvDistance;
        private TextView tvDescription;
        private MaterialButton btnView;

        public RequestViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnView = itemView.findViewById(R.id.btnView);
        }

        public void bind(Post item, int position) {
            tvJobTitle.setText(item.title);
            tvDistance.setText(item.distance != null ? item.distance : "Nearby");
            tvDescription.setText(item.description);

            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), GigPostDetailActivity.class);
                intent.putExtra("post_id", item.postId);
                intent.putExtra("title", item.title);
                intent.putExtra("budget", item.budget);
                intent.putExtra("distance", item.distance);
                intent.putExtra("description", item.description);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
