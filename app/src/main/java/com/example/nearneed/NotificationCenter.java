package com.example.nearneed;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NotificationCenter {

    private NotificationCenter() {}

    public interface OnNotificationsLoaded {
        void onLoaded(List<AppNotification> notifications);
    }

    public interface OnCountChanged {
        void onChange(int count);
    }

    /** Write a new notification to Firestore for the signed-in user. */
    public static void addNotification(String title, String message) {
        CollectionReference ref = notifRef();
        if (ref == null) return;
        String id = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());
        data.put("read", false);
        ref.document(id).set(data);
    }

    /** Fetch all notifications once, newest-first. Used when opening the popup. */
    public static void fetchOnce(OnNotificationsLoaded callback) {
        CollectionReference ref = notifRef();
        if (ref == null) {
            callback.onLoaded(new ArrayList<>());
            return;
        }
        ref.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    List<AppNotification> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        list.add(fromDoc(doc));
                    }
                    callback.onLoaded(list);
                })
                .addOnFailureListener(e -> callback.onLoaded(new ArrayList<>()));
    }

    /**
     * Attach a real-time listener for the unread count.
     * Call remove() on the returned registration in onStop().
     */
    public static ListenerRegistration listenUnreadCount(OnCountChanged callback) {
        CollectionReference ref = notifRef();
        if (ref == null) {
            callback.onChange(0);
            return () -> {};
        }
        return ref.whereEqualTo("read", false)
                .addSnapshotListener((snap, err) -> {
                    if (snap != null) callback.onChange(snap.size());
                });
    }

    /** Mark a single notification as read. */
    public static void markAsRead(String notificationId) {
        CollectionReference ref = notifRef();
        if (ref == null) return;
        ref.document(notificationId).update("read", true);
    }

    /** Delete all notifications for the current user. */
    public static void clearAll() {
        CollectionReference ref = notifRef();
        if (ref == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ref.get().addOnSuccessListener(snap -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                batch.delete(doc.getReference());
            }
            batch.commit();
        });
    }

    private static CollectionReference notifRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        return FirebaseFirestore.getInstance()
                .collection("Users").document(user.getUid())
                .collection("notifications");
    }

    private static AppNotification fromDoc(DocumentSnapshot doc) {
        Long ts = doc.getLong("timestamp");
        String id = doc.getString("id");
        return new AppNotification(
                id != null ? id : doc.getId(),
                doc.getString("title"),
                doc.getString("message"),
                ts != null ? ts : 0L,
                Boolean.TRUE.equals(doc.getBoolean("read"))
        );
    }
}
