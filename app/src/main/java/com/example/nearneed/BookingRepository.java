package com.example.nearneed;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Create a new booking (simple form).
     */
    public static void createBooking(String postId, String seekerId, String providerId, SaveCallback callback) {
        createBooking(postId, null, null, seekerId, providerId, null, callback);
    }

    /**
     * Create a new booking (full form).
     */
    public static void createBooking(String postId, String postTitle, String postType,
                                     String seekerId, String providerId, String applicationId,
                                     SaveCallback callback) {
        if (postId == null || callback == null) return;

        Booking booking = new Booking(postId, seekerId, providerId);
        booking.postTitle = postTitle;
        booking.postType = postType;
        booking.applicationId = applicationId;
        booking.createdAt = System.currentTimeMillis();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .add(booking)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); });
    }

    /**
     * Update booking status.
     */
    public static void updateBookingStatus(String bookingId, String status, SaveCallback callback) {
        if (bookingId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(bookingId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Observe bookings for current user (Context overload used by ViewModel).
     */
    public static ListenerRegistration observeUserBookings(Context context, BookingListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return observeUserBookings(uid, "seeker", listener);
    }

    /**
     * Load bookings from Room cache (stub — falls back to empty list).
     */
    public static void loadBookingsFromRoom(Context context, String uid, BookingListener listener) {
        if (listener != null) listener.onBookingsLoaded(new ArrayList<>());
    }

    /**
     * Observe bookings for a specific post.
     */
    public static ListenerRegistration observeBookingsForPost(String postId, BookingListener listener) {
        if (postId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snapshot != null) {
                        List<Booking> bookings = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking b = fromSnapshot(doc);
                            if (b != null) bookings.add(b);
                        }
                        listener.onBookingsLoaded(bookings);
                    }
                });
    }

    /**
     * Mark payment as completed for a booking.
     */
    public static void markPaymentCompleted(String bookingId, SaveCallback callback) {
        if (bookingId == null || callback == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "paid");
        updates.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess(bookingId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Submit a rating for a booking.
     */
    public static void submitRating(String bookingId, int rating, String review, String raterRole, SaveCallback callback) {
        if (bookingId == null || callback == null) return;
        String ratingField = "provider".equals(raterRole) ? "seekerRating" : "providerRating";
        Map<String, Object> updates = new HashMap<>();
        updates.put(ratingField, rating);
        updates.put(ratingField + "Review", review);
        updates.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess(bookingId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Observe bookings for a user.
     */
    public static ListenerRegistration observeUserBookings(String userId, String role, BookingListener listener) {
        if (userId == null || listener == null) return null;

        String field = "seekerId";
        if ("provider".equals(role)) {
            field = "providerId";
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo(field, userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
