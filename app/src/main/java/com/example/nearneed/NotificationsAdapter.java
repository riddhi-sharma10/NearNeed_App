package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationActionListener listener;

    public interface OnNotificationActionListener {
        void onNotificationClick(String notificationId, int position);
        void onMarkAsRead(String notificationId, int position);
    }

    public NotificationsAdapter(List<Notification> notifications, OnNotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, position, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;
        private View unreadIndicator;
        private LinearLayout mainContainer;

        public NotificationViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            mainContainer = itemView;
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(Notification notification, int position, OnNotificationActionListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            // Format time
            long timestamp = notification.getTimestamp();
            String timeText = formatTime(timestamp);
            tvTime.setText(timeText);

            // Set icon based on type
            int iconRes = getIconForType(notification.getType());
            ivIcon.setImageResource(iconRes);

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? android.view.View.GONE : android.view.View.VISIBLE);

            // Set alpha based on read status
            mainContainer.setAlpha(notification.isRead() ? 0.6f : 1.0f);

            // Click listener
            mainContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMarkAsRead(notification.getId(), position);
                    listener.onNotificationClick(notification.getId(), position);
                }
            });
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }

        private int getIconForType(String type) {
            switch (type) {
                case "booking":
                    return R.drawable.ic_bookings_calendar;
                case "payment":
                    return R.drawable.ic_payment_wallet_blue;
                case "message":
                    return R.drawable.ic_nav_messages;
                case "status_update":
                    return R.drawable.ic_check_circle_green;
                default:
                    return R.drawable.ic_nav_home;
            }
        }
    }
}
