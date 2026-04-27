package com.example.nearneed;

/**
 * Utility for sending notifications to users.
 * Writes to NotificationCenter (Firestore) so the in-app badge and
 * notification list update in real-time for any active session.
 */
public class FcmNotifier {

    private FcmNotifier() {}

    public static void sendToUser(String userId, String title, String body) {
        if (userId == null || userId.isEmpty()) return;
        NotificationCenter.sendNotificationToUser(userId, title, body);
    }
}
