package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NotificationCenter {

    private static final String PREFS = "dashboard_notifications";
    private static final String KEY_ITEMS = "items";

    private NotificationCenter() {
    }

    public static List<AppNotification> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_ITEMS, "");
        if (raw == null || raw.isEmpty()) {
            List<AppNotification> seeded = seedNotifications();
            saveNotifications(context, seeded);
            return seeded;
        }

        List<AppNotification> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new AppNotification(
                    o.optString("id"),
                    o.optString("title"),
                    o.optString("message"),
                    o.optLong("timestamp"),
                    o.optBoolean("read", false)
                ));
            }
        } catch (Exception ignored) {
            out = seedNotifications();
            saveNotifications(context, out);
        }
        return out;
    }

    public static void saveNotifications(Context context, List<AppNotification> notifications) {
        JSONArray arr = new JSONArray();
        for (AppNotification n : notifications) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", n.getId());
                o.put("title", n.getTitle());
                o.put("message", n.getMessage());
                o.put("timestamp", n.getTimestamp());
                o.put("read", n.isRead());
            } catch (Exception ignored) {
            }
            arr.put(o);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ITEMS, arr.toString()).apply();
    }

    public static void markAsRead(Context context, String notificationId) {
        List<AppNotification> items = getNotifications(context);
        for (AppNotification n : items) {
            if (n.getId().equals(notificationId)) {
                n.setRead(true);
                break;
            }
        }
        saveNotifications(context, items);
    }

    public static void clearAll(Context context) {
        saveNotifications(context, new ArrayList<>());
    }

    public static int unreadCount(Context context) {
        int count = 0;
        for (AppNotification n : getNotifications(context)) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    private static List<AppNotification> seedNotifications() {
        long now = System.currentTimeMillis();
        List<AppNotification> list = new ArrayList<>();
        list.add(new AppNotification(UUID.randomUUID().toString(), "Booking Update", "Your upcoming booking starts in 30 minutes.", now - 2 * 60 * 1000L, false));
        list.add(new AppNotification(UUID.randomUUID().toString(), "New Response", "You received 2 responses on Community Help post.", now - 25 * 60 * 1000L, false));
        list.add(new AppNotification(UUID.randomUUID().toString(), "Payment Received", "A payment has been credited to your wallet.", now - 3 * 60 * 60 * 1000L, true));
        list.add(new AppNotification(UUID.randomUUID().toString(), "Status Changed", "One booking moved to Completed.", now - 26 * 60 * 60 * 1000L, true));
        return list;
    }
}
