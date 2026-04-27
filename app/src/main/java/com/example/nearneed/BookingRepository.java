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
    public static void createBooking(String postId, String seekerId, String providerId, Double amount, SaveCallback callback) {
        createBooking(postId, null, null, seekerId, providerId, null, amount, null, callback);
    }

    /**
     * Create a new booking (full form) — fetches seeker & provider names before saving.
     */
    public static void createBooking(String postId, String postTitle, String postType,
                                     String seekerId, String providerId, String applicationId,
                                     Double amount, Long scheduledDate, SaveCallback callback) {
        if (postId == null || callback == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String[] seekerName   = {""};
        final String[] providerName = {""};
        final int[]    done         = {0};

        Runnable saveBooking = () -> {
            done[0]++;
            if (done[0] < 2) return;

            Booking booking = new Booking(postId, seekerId, providerId);
            booking.postTitle     = postTitle;
            booking.postType      = postType;
            booking.applicationId = applicationId;
            booking.createdAt     = System.currentTimeMillis();
            booking.scheduledDate = scheduledDate != null ? scheduledDate : System.currentTimeMillis();
            booking.seekerName    = seekerName[0];
            booking.providerName  = providerName[0];
            booking.amount        = amount;
            booking.status        = "upcoming";

            db.collection(BOOKINGS_COLLECTION)
                    .add(booking)
                    .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                    .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); });
        };

        if (seekerId != null && !seekerId.isEmpty()) {
            db.collection("users").document(seekerId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            String n = doc.getString("name");
                            if (n == null || n.isEmpty()) n = doc.getString("fullName");
                            if (n != null && !n.isEmpty()) seekerName[0] = n;
                        }
                        saveBooking.run();
                    })
                    .addOnFailureListener(e -> saveBooking.run());
        } else {
            done[0]++;
            saveBooking.run();
        }

        if (providerId != null && !providerId.isEmpty()) {
            db.collection("users").document(providerId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            String n = doc.getString("name");
                            if (n == null || n.isEmpty()) n = doc.getString("fullName");
                            if (n != null && !n.isEmpty()) providerName[0] = n;
                        }
                        saveBooking.run();
                    })
                    .addOnFailureListener(e -> saveBooking.run());
        } else {
            done[0]++;
            saveBooking.run();
        }
    }

    /**
     * Update booking status.
     */
    public static void updateBookingStatus(String bookingId, String status, SaveCallback callback) {
        if (bookingId == null || callback == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // 1. Fetch booking to get postId
        db.collection(BOOKINGS_COLLECTION).document(bookingId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String postId = snapshot.getString("postId");
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", status);
                        updates.put("timestamp", System.currentTimeMillis());

                        // 2. Update booking status
                        db.collection(BOOKINGS_COLLECTION).document(bookingId).update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // 3. Sync status to the linked post if status is completed/cancelled
                                    if (postId != null && ("completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status))) {
                                        Map<String, Object> postUpdates = new HashMap<>();
                                        postUpdates.put("status", status);
                                        db.collection("posts").document(postId).update(postUpdates);
                                    }
                                    callback.onSuccess(bookingId);
                                })
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("Booking not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Observe bookings for current user (Context overload used by ViewModel).
     */
    public static ListenerRegistration observeUserBookings(Context context, BookingListener listener) {
        if (context == null || listener == null) return null;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Return unified listener that handles both roles for a truly real-time experience
        return observeUserBookings(uid, "all", listener);
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
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snapshot != null) {
                        List<Booking> bookings = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking b = fromSnapshot(doc);
                            if (b != null) bookings.add(b);
                        }
                        
                        // Client-side sort by timestamp descending
                        java.util.Collections.sort(bookings, (b1, b2) -> {
                            Long t1 = b1.timestamp != null ? b1.timestamp : 0L;
                            Long t2 = b2.timestamp != null ? b2.timestamp : 0L;
                            return t2.compareTo(t1);
                        });
                        
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
                .addOnSuccessListener(v -> {
                    // Sync to User Profile if seeker is rating provider
                    if (!"provider".equalsIgnoreCase(raterRole)) {
                        FirebaseFirestore.getInstance().collection(BOOKINGS_COLLECTION)
                                .document(bookingId).get().addOnSuccessListener(doc -> {
                                    if (doc != null && doc.exists()) {
                                        String providerId = doc.getString("providerId");
                                        Double amt = doc.getDouble("amount");
                                        if (providerId != null) {
                                            UserProfileRepository.incrementProviderStats(providerId, 
                                                amt != null ? amt : 0.0, rating);
                                        }
                                    }
                                });
                    }
                    callback.onSuccess(bookingId);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Observe bookings for a user.
     */
    public static ListenerRegistration observeUserBookings(String userId, String role, BookingListener listener) {
        if (userId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if ("all".equalsIgnoreCase(role)) {
            return observeAllUserBookings(userId, listener);
        }

        String field = "seekerId";
        if (RoleManager.ROLE_PROVIDER.equalsIgnoreCase(role) || "provider".equalsIgnoreCase(role)) {
            field = "providerId";
        }

        return db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo(field, userId)
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
                        
                        // Client-side sort by timestamp descending
                        java.util.Collections.sort(bookings, (b1, b2) -> {
                            Long t1 = b1.timestamp != null ? b1.timestamp : 0L;
                            Long t2 = b2.timestamp != null ? b2.timestamp : 0L;
                            return t2.compareTo(t1);
                        });
                        
                        listener.onBookingsLoaded(bookings);
                    }
                });
    }

    private static ListenerRegistration observeAllUserBookings(String userId, BookingListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final List<Booking>[] seekerResult   = new List[]{new ArrayList<>()};
        final List<Booking>[] providerResult = new List[]{new ArrayList<>()};

        Runnable combiner = () -> {
            // Merge both lists, deduplicating by bookingId (a booking should never appear twice)
            java.util.LinkedHashMap<String, Booking> byId = new java.util.LinkedHashMap<>();
            for (Booking b : seekerResult[0])   { if (b.bookingId != null) byId.put(b.bookingId, b); }
            for (Booking b : providerResult[0]) { if (b.bookingId != null) byId.putIfAbsent(b.bookingId, b); }

            List<Booking> combined = new ArrayList<>(byId.values());
            java.util.Collections.sort(combined, (b1, b2) -> {
                Long t1 = b1.timestamp != null ? b1.timestamp : 0L;
                Long t2 = b2.timestamp != null ? b2.timestamp : 0L;
                return t2.compareTo(t1);
            });
            listener.onBookingsLoaded(combined);
        };

        ListenerRegistration reg1 = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("seekerId", userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null) {
                        seekerResult[0] = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking b = fromSnapshot(doc);
                            if (b != null) seekerResult[0].add(b);
                        }
                        combiner.run();
                    }
                });

        ListenerRegistration reg2 = db.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("providerId", userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null) {
                        providerResult[0] = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Booking b = fromSnapshot(doc);
                            if (b != null) providerResult[0].add(b);
                        }
                        combiner.run();
                    }
                });

        return new ListenerRegistration() {
            @Override public void remove() {
                reg1.remove();
                reg2.remove();
            }
        };
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
