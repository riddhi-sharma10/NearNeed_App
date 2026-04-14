package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResponsesAdapter extends RecyclerView.Adapter<ResponsesAdapter.ResponseViewHolder> {

    private List<Response> responses;
    private OnResponseActionListener listener;

    public interface OnResponseActionListener {
        void onAccept(String responseId, int position);
        void onDecline(String responseId, int position);
    }

    public ResponsesAdapter(List<Response> responses, OnResponseActionListener listener) {
        this.responses = responses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_response_card, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        Response response = responses.get(position);
        holder.bind(response, position, listener);
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    public static class ResponseViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvRating;
        private TextView tvMessage;
        private TextView tvLocation;
        private TextView tvTime;
        private TextView tvStatus;
        private MaterialButton btnAccept, btnDecline;
        private LinearLayout mainContainer;

        public ResponseViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            mainContainer = itemView;
            ivAvatar = itemView.findViewById(R.id.ivApplicantAvatar);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvRating = itemView.findViewById(R.id.tvApplicantRating);
            tvMessage = itemView.findViewById(R.id.tvApplicantMessage);
            tvLocation = itemView.findViewById(R.id.tvApplicantLocation);
            tvTime = itemView.findViewById(R.id.tvAppliedTime);
            tvStatus = itemView.findViewById(R.id.tvResponseStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        public void bind(Response response, int position, OnResponseActionListener listener) {
            tvName.setText(response.getApplicantName());
            tvRating.setText(String.format("★ %.1f", response.getApplicantRating()));
            tvMessage.setText(response.getMessage());
            tvLocation.setText(response.getLocation());
            tvTime.setText(formatTime(response.getTimestamp()));

            // Set status badge
            String status = response.getStatus();
            tvStatus.setText(status.toUpperCase());
            updateStatusBadge(status);

            // Update button states based on status
            if ("accepted".equals(status)) {
                btnAccept.setEnabled(false);
                btnAccept.setAlpha(0.5f);
                btnDecline.setEnabled(false);
                btnDecline.setAlpha(0.5f);
            } else if ("declined".equals(status)) {
                mainContainer.setAlpha(0.6f);
                btnAccept.setEnabled(false);
                btnDecline.setEnabled(false);
            } else {
                btnAccept.setEnabled(true);
                btnAccept.setAlpha(1.0f);
                btnDecline.setEnabled(true);
                btnDecline.setAlpha(1.0f);
                mainContainer.setAlpha(1.0f);
            }

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(response.getResponseId(), position);
                }
            });

            btnDecline.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecline(response.getResponseId(), position);
                }
            });
        }

        private void updateStatusBadge(String status) {
            switch (status.toLowerCase()) {
                case "new":
                    tvStatus.setTextColor(0xFF1E3A8A);
                    tvStatus.setBackgroundColor(0xFFDBEAFE);
                    break;
                case "accepted":
                    tvStatus.setTextColor(0xFF065F46);
                    tvStatus.setBackgroundColor(0xFFD1FAE5);
                    break;
                case "declined":
                    tvStatus.setTextColor(0xFFB91C1C);
                    tvStatus.setBackgroundColor(0xFFFEE2E2);
                    break;
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            long days = diff / (24 * 60 * 60 * 1000);

            if (minutes < 60) {
                return minutes + " min ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else if (days < 7) {
                return days + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
