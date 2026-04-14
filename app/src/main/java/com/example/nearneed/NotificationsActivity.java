package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationsAdapter adapter;
    private List<Notification> notifications;
    private LinearLayout unreadCountSection;
    private TextView tvUnreadCount;
    private LinearLayout emptyState;
    private TextView btnMarkAllAsRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_notifications);

        // Initialize views
        rvNotifications = findViewById(R.id.rvNotifications);
        unreadCountSection = findViewById(R.id.unreadCountSection);
        tvUnreadCount = findViewById(R.id.tvUnreadCount);
        emptyState = findViewById(R.id.emptyState);
        btnMarkAllAsRead = findViewById(R.id.btnMarkAllAsRead);

        // Setup RecyclerView
        notifications = loadNotifications();
        adapter = new NotificationsAdapter(notifications, new NotificationsAdapter.OnNotificationActionListener() {
            @Override
            public void onNotificationClick(String notificationId, int position) {
                // Handle notification click - navigate to related content
                handleNotificationClick(notificationId, position);
            }

            @Override
            public void onMarkAsRead(String notificationId, int position) {
                markAsRead(notificationId, position);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvNotifications.setLayoutManager(layoutManager);
        rvNotifications.setAdapter(adapter);

        // Setup mark all as read button
        btnMarkAllAsRead.setOnClickListener(v -> markAllAsRead());

        // Update UI based on notifications
        updateNotificationUI();

        // Bind the unified navbar – no active tab (notifications is not in navbar)
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), -1);
    }

    private void updateNotificationUI() {
        int unreadCount = getUnreadCount();

        if (notifications.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
            unreadCountSection.setVisibility(View.GONE);
            btnMarkAllAsRead.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);

            if (unreadCount > 0) {
                unreadCountSection.setVisibility(View.VISIBLE);
                tvUnreadCount.setText("You have " + unreadCount + " unread notification" + (unreadCount > 1 ? "s" : ""));
                btnMarkAllAsRead.setVisibility(View.VISIBLE);
            } else {
                unreadCountSection.setVisibility(View.GONE);
                btnMarkAllAsRead.setVisibility(View.GONE);
            }
        }
    }

    private int getUnreadCount() {
        int count = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    private void markAsRead(String notificationId, int position) {
        if (position >= 0 && position < notifications.size()) {
            Notification notification = notifications.get(position);
            notification.setRead(true);
            adapter.notifyItemChanged(position);

            // Save to SharedPreferences
            saveNotifications();

            // Update UI
            updateNotificationUI();

            // Update navbar badge
            updateNavbarBadge();
        }
    }

    private void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        adapter.notifyDataSetChanged();
        saveNotifications();
        updateNotificationUI();
        updateNavbarBadge();
        Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void handleNotificationClick(String notificationId, int position) {
        // Navigate to relevant screen based on notification type
        // TODO: Implement navigation logic based on notification.getType() and notification.getRelatedId()
        // For now, just show a toast
        if (position >= 0 && position < notifications.size()) {
            Notification notification = notifications.get(position);
            switch (notification.getType()) {
                case "booking":
                    // Navigate to BookingsActivity with the related booking ID
                    break;
                case "payment":
                    // Navigate to PaymentActivity
                    break;
                case "message":
                    // Navigate to ChatActivity or message thread
                    break;
                case "status_update":
                    // Navigate to the relevant post/booking
                    break;
            }
        }
    }

    private List<Notification> loadNotifications() {
        // Load from SharedPreferences or database
        // For now, return sample data
        SharedPreferences prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE);
        String savedNotifications = prefs.getString("notifications_list", "");

        List<Notification> loadedNotifications = new ArrayList<>();

        if (savedNotifications.isEmpty()) {
            // Return sample notifications for demonstration
            loadedNotifications.add(new Notification("1", "New Booking Request", "Sarah J. has applied to your plumbing service", "booking", false, System.currentTimeMillis() - 3600000, "booking_1"));
            loadedNotifications.add(new Notification("2", "Payment Received", "Payment of ₹500 received from David M.", "payment", false, System.currentTimeMillis() - 7200000, "payment_1"));
            loadedNotifications.add(new Notification("3", "Status Update", "Your community help request has 5 responses", "status_update", true, System.currentTimeMillis() - 86400000, "post_1"));
            loadedNotifications.add(new Notification("4", "New Message", "You have a new message from Service Provider", "message", true, System.currentTimeMillis() - 172800000, "chat_1"));
        }

        return loadedNotifications;
    }

    private void saveNotifications() {
        // Save to SharedPreferences or database
        SharedPreferences prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // TODO: Serialize notifications list and save
        editor.apply();
    }

    private void updateNavbarBadge() {
        // Update the unread count badge on the navbar chat icon
        // This would typically be done by sending a broadcast or updating a shared ViewModel
        // For now, we'll just update the SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_state", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("unread_count", getUnreadCount());
        editor.apply();
    }
}
