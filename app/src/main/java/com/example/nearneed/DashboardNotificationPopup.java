package com.example.nearneed;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public final class DashboardNotificationPopup {

    private DashboardNotificationPopup() {}

    public static void show(Activity activity, View anchor, Runnable onChanged) {
        View content = LayoutInflater.from(activity).inflate(R.layout.dialog_dashboard_notifications, null);

        PopupWindow popupWindow = new PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        popupWindow.setElevation(18f);
        popupWindow.setOutsideTouchable(true);

        RecyclerView rv = content.findViewById(R.id.rvDashboardNotifications);
        TextView tvClearAll = content.findViewById(R.id.tvClearAllNotifications);
        TextView tvEmpty = content.findViewById(R.id.tvEmptyNotifications);

        List<AppNotification> items = new ArrayList<>();

        NotificationPopupAdapter adapter = new NotificationPopupAdapter(items, notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                NotificationCenter.markAsRead(notification.getId());
                rv.getAdapter().notifyDataSetChanged();
                if (onChanged != null) onChanged.run();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setAdapter(adapter);
        updateEmptyState(items, rv, tvEmpty, tvClearAll);

        // Load from Firestore — list shows empty state until data arrives
        NotificationCenter.fetchOnce(loaded -> {
            items.addAll(loaded);
            adapter.notifyDataSetChanged();
            updateEmptyState(items, rv, tvEmpty, tvClearAll);
        });

        tvClearAll.setOnClickListener(v -> {
            NotificationCenter.clearAll();
            items.clear();
            adapter.notifyDataSetChanged();
            updateEmptyState(items, rv, tvEmpty, tvClearAll);
            if (onChanged != null) onChanged.run();
        });

        int popupWidth = (int) (320 * activity.getResources().getDisplayMetrics().density);
        content.measure(
            View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.UNSPECIFIED
        );

        int xOff = -popupWidth + anchor.getWidth();
        int yOff = (int) (8 * activity.getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchor, xOff, yOff);
    }

    private static void updateEmptyState(List<AppNotification> items, RecyclerView rv, TextView tvEmpty, TextView tvClearAll) {
        boolean isEmpty = items.isEmpty();
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        tvClearAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
