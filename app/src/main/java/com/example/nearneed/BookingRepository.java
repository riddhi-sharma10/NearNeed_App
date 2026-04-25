package com.example.nearneed;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for managing Booking data with Firestore.
 * Firestore structure: bookings/{bookingId}
 */
public class BookingRepository {

    private static final String BOOKINGS_COLLECTION = "bookings";

    public interface BookingListener {
        void onBookingsLoaded(List<Booking> bookings);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess(String bookingId);
        void onFailure(Exception e);
    }

    /**
     * Create a new booking (typically after application acceptance).
     */
    public static void createBooking(String postId, String postTitle, String postType,
                                     String seekerId, String providerId, 
                                     String applicationId, SaveCallback callback) {
        if (postId == null || seekerId == null || providerId == null || callback == null) return;

        String bookingId = UUID.randomUUID().toString();

        Booking booking = new Booking(postId, postTitle, postType, seekerId, providerId, providerId);
        booking.bookingId = bookingId;
        booking.applicationId = applicationId;
        booking.createdAt = System.currentTimeMillis();
        booking.status = "confirmed";
        booking.paymentStatus = "pending";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .set(booking)
                .addOnSuccessListener(aVoid -> {
                    // Notify provider they have been booked
                    NotificationCenter.sendNotificationToUser(providerId, "You've been Booked!", 
                        "You have a new booking for: " + postTitle);
                    callback.onSuccess(bookingId);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Observe bookings for current user (as seeker or provider).
     * Returns ListenerRegistration for cleanup.
     */
    public static ListenerRegistration observeUserBookings(Context context, BookingListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("seekerId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Booking> bookings = new ArrayList<>();
                        List<BookingEntity> entities = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking booking = fromSnapshot(doc);
                            if (booking != null) {
                                bookings.add(booking);
                                entities.add(BookingEntity.fromBooking(booking));
                            }
                        }

                        // Async cache in Room
                        new Thread(() -> {
                            AppDatabase.getDatabase(context).bookingDao().insertAll(entities);
                        }).start();

                        listener.onBookingsLoaded(bookings);
                    }
                });
    }

    /**
     * Load bookings from Room for offline-first display.
     */
    public static void loadBookingsFromRoom(Context context, String userId, BookingListener listener) {
        new Thread(() -> {
            List<BookingEntity> entities = AppDatabase.getDatabase(context).bookingDao().getUserBookings(userId);
            List<Booking> bookings = new ArrayList<>();
            for (BookingEntity entity : entities) {
                bookings.add(entity.toBooking());
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onBookingsLoaded(bookings);
            });
        }).start();
    }

    /**
     * Simpler version: observe all user bookings (separate queries for seeker and provider).
     */
    public static ListenerRegistration observeUserBookingsSimple(BookingListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("seekerId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Booking> bookings = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking booking = fromSnapshot(doc);
                            if (booking != null) {
                                bookings.add(booking);
                            }
                        }
                        listener.onBookingsLoaded(bookings);
                    }
                });
    }

    /**
     * Observe bookings for a specific post (for post creator).
     */
    public static ListenerRegistration observeBookingsForPost(String postId, BookingListener listener) {
        if (postId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Booking> bookings = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking booking = fromSnapshot(doc);
                            if (booking != null) {
                                bookings.add(booking);
                            }
                        }
                        listener.onBookingsLoaded(bookings);
                    }
                });
    }

    /**
     * Update booking status.
     */
    public static void updateBookingStatus(String bookingId, String newStatus, SaveCallback callback) {
        if (bookingId == null || newStatus == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", System.currentTimeMillis());

        if ("completed".equals(newStatus)) {
            updates.put("completedAt", System.currentTimeMillis());
        } else if ("cancelled".equals(newStatus)) {
            updates.put("cancelledAt", System.currentTimeMillis());
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Notify both parties
                    db.collection(BOOKINGS_COLLECTION).document(bookingId).get()
                        .addOnSuccessListener(snapshot -> {
                            Booking booking = fromSnapshot(snapshot);
                            if (booking != null) {
                                String msg = "The booking for '" + booking.postTitle + "' is now " + newStatus + ".";
                                NotificationCenter.sendNotificationToUser(booking.seekerId, "Booking Status Update", msg);
                                NotificationCenter.sendNotificationToUser(booking.providerId, "Booking Status Update", msg);
                            }
                        });
                    callback.onSuccess(bookingId);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Mark booking payment as completed.
     */
    public static void markPaymentCompleted(String bookingId, SaveCallback callback) {
        if (bookingId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "completed");
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(bookingId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Submit rating and review for a booking.
     */
    public static void submitRating(String bookingId, int rating, String review, 
                                    String raterRole, SaveCallback callback) {
        if (bookingId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("ratedAt", System.currentTimeMillis());

        if ("seeker".equals(raterRole)) {
            updates.put("seekerRating", rating);
            updates.put("seekerReview", review);
        } else if ("provider".equals(raterRole)) {
            updates.put("providerRating", rating);
            updates.put("providerReview", review);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(bookingId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deserialize Firestore document snapshot to Booking object.
     */
    public static Booking fromSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Booking booking = doc.toObject(Booking.class);
        if (booking != null) {
            booking.bookingId = doc.getId();
        }
        return booking;
    }
}
