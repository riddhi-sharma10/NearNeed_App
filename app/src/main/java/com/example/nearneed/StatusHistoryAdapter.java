package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatusHistoryAdapter extends RecyclerView.Adapter<StatusHistoryAdapter.StatusViewHolder> {

    private List<Status> statusHistory;

    public StatusHistoryAdapter(List<Status> statusHistory) {
        this.statusHistory = statusHistory;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_status_history, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        Status status = statusHistory.get(position);
        holder.bind(status, position == statusHistory.size() - 1); // Last item doesn't have line
    }

    @Override
    public int getItemCount() {
        return statusHistory.size();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivStatusIcon;
        private TextView tvStatusType;
        private TextView tvDescription;
        private TextView tvTime;
        private ImageView ivTimeline;

        public StatusViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            tvStatusType = itemView.findViewById(R.id.tvStatusType);
            tvDescription = itemView.findViewById(R.id.tvStatusDescription);
            tvTime = itemView.findViewById(R.id.tvStatusTime);
            ivTimeline = itemView.findViewById(R.id.ivTimelineLine);
        }

        public void bind(Status status, boolean isLast) {
            // Set icon and color
            int colorResId = status.getStatusColorResId();
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            ivStatusIcon.setColorFilter(color);

            // Set status text
            tvStatusType.setText(formatStatusType(status.getStatusType()));
            tvStatusType.setTextColor(color);

            // Set description
            tvDescription.setText(status.getDescription());

            // Set time
            String timeText = formatTime(status.getTimestamp());
            tvTime.setText(timeText);

            // Hide timeline for last item
            if (isLast) {
                ivTimeline.setVisibility(android.view.View.GONE);
            } else {
                ivTimeline.setVisibility(android.view.View.VISIBLE);
            }
        }

        private String formatStatusType(String statusType) {
            return statusType.replace("_", " ").toUpperCase();
        }

        private String formatTime(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
