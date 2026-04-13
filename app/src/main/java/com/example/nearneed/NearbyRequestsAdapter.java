package com.example.nearneed;

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

    public static class RequestItem {
        public String title;
        public String distance;
        public String price;
        public int iconDrawable;

        public RequestItem(String title, String distance, String price, int iconDrawable) {
            this.title = title;
            this.distance = distance;
            this.price = price;
            this.iconDrawable = iconDrawable;
        }
    }

    private List<RequestItem> requests = new ArrayList<>();
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(int position);
        void onDecline(int position);
    }

    public NearbyRequestsAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<RequestItem> requests) {
        this.requests = requests;
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
        RequestItem item = requests.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle;
        private TextView tvDistance;
        private TextView tvPrice;
        private ShapeableImageView thumbImage;
        private MaterialButton btnAccept;
        private MaterialButton btnDecline;

        public RequestViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            thumbImage = itemView.findViewById(R.id.thumbImage);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        public void bind(RequestItem item, int position) {
            tvJobTitle.setText(item.title);
            tvDistance.setText(item.distance);
            tvPrice.setText(item.price);
            thumbImage.setImageResource(item.iconDrawable);

            btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(position);
            });

            btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(position);
            });
        }
    }
}
