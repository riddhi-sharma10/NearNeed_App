package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationPopupAdapter extends RecyclerView.Adapter<NotificationPopupAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClicked(AppNotification notification);
    }

    private final List<AppNotification> items;
    private final OnNotificationClickListener listener;

    public NotificationPopupAdapter(List<AppNotification> items, OnNotificationClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_dashboard_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        AppNotification item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            unreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }

        void bind(AppNotification item, OnNotificationClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvMessage.setText(item.getMessage());
            tvTime.setText(formatTime(item.getTimestamp()));

            unreadDot.setVisibility(item.isRead() ? View.INVISIBLE : View.VISIBLE);
            tvTitle.setAlpha(item.isRead() ? 0.72f : 1f);
            tvMessage.setAlpha(item.isRead() ? 0.72f : 0.9f);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClicked(item);
                }
            });
        }

        private String formatTime(long timestamp) {
            long diffMs = Math.max(0, System.currentTimeMillis() - timestamp);
            long mins = diffMs / (60 * 1000);
            if (mins < 1) {
                return "just now";
            }
            if (mins < 60) {
                return mins + "m ago";
            }
            long hours = mins / 60;
            if (hours < 24) {
                return hours + "h ago";
            }
            return (hours / 24) + "d ago";
        }
    }
}
