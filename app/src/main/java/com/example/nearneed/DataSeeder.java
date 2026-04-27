package com.example.nearneed;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class DataSeeder {

    private static final String TAG = "DataSeeder";
    private final FirebaseFirestore db;

    public interface SeederCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public DataSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Wipes old collections and establishes the 8 core schemas by pushing a single 
     * blank/dummy document to each collection. This ensures Firestore creates the 
     * collections and registers the fields without populating the UI with heavy mock data.
     */
    public void resetAndSeed(SeederCallback callback) {
        // In a real production scenario, wiping collections from the client is difficult 
        // due to Firestore's limits on deleting entire collections from a mobile device.
        // For this demo seeder, we will just push the template documents directly.
        // It will overwrite the dummy documents if they already exist, ensuring schema integrity.

        WriteBatch batch = db.batch();
        long currentTimestamp = System.currentTimeMillis();

        // 1. USERS Schema Template
        DocumentReference userRef = db.collection("users").document("schema_template_user");
        Map<String, Object> userSchema = new HashMap<>();
        userSchema.put("uid", "schema_template_user");
        userSchema.put("name", "Template User");
        userSchema.put("email", "template@example.com");
        userSchema.put("phone", "+910000000000");
        userSchema.put("role", "seeker"); // or "provider"
        userSchema.put("isVerified", false);
        userSchema.put("profileImageUrl", "");
        userSchema.put("latitude", 0.0);
        userSchema.put("longitude", 0.0);
        userSchema.put("category", ""); // For providers
        userSchema.put("rating", 0.0); // For providers
        userSchema.put("timestamp", currentTimestamp);
        batch.set(userRef, userSchema);

        // 2. POSTS Schema Template (Gigs & Community)
        DocumentReference postRef = db.collection("posts").document("schema_template_post");
        Map<String, Object> postSchema = new HashMap<>();
        postSchema.put("postId", "schema_template_post");
        postSchema.put("type", "gig"); // or "community"
        postSchema.put("title", "Template Post");
        postSchema.put("description", "Template Description");
        postSchema.put("createdBy", "schema_template_user");
        postSchema.put("latitude", 0.0);
        postSchema.put("longitude", 0.0);
        postSchema.put("category", "General");
        postSchema.put("locationName", "Template Location");
        postSchema.put("budget", 0.0); // For Gigs
        postSchema.put("volunteersNeeded", 0); // For Community
        postSchema.put("eventDate", currentTimestamp); // For Community
        postSchema.put("timestamp", currentTimestamp);
        batch.set(postRef, postSchema);

        // 3. BOOKINGS Schema Template (Supports Seeker/Provider, Past/Ongoing/Upcoming)
        DocumentReference bookingRef = db.collection("bookings").document("schema_template_booking");
        Map<String, Object> bookingSchema = new HashMap<>();
        bookingSchema.put("bookingId", "schema_template_booking");
        bookingSchema.put("postId", "schema_template_post");
        bookingSchema.put("seekerId", "schema_template_user");
        bookingSchema.put("providerId", "schema_template_user");
        bookingSchema.put("status", "upcoming"); // "upcoming", "ongoing", "completed", "cancelled"
        bookingSchema.put("paymentStatus", "pending"); // "pending", "paid", "refunded"
        bookingSchema.put("amount", 0.0);
        bookingSchema.put("cancellationReason", ""); // For Past/Cancelled
        bookingSchema.put("scheduledDate", currentTimestamp); // For Upcoming
        bookingSchema.put("timestamp", currentTimestamp);
        batch.set(bookingRef, bookingSchema);

        // 4. APPLICATIONS Schema Template
        DocumentReference appRef = db.collection("applications").document("schema_template_app");
        Map<String, Object> appSchema = new HashMap<>();
        appSchema.put("applicationId", "schema_template_app");
        appSchema.put("postId", "schema_template_post");
        appSchema.put("providerId", "schema_template_user");
        appSchema.put("message", "Template application message");
        appSchema.put("status", "pending"); // "pending", "accepted", "rejected"
        appSchema.put("timestamp", currentTimestamp);
        batch.set(appRef, appSchema);

        // 5. RESPONSES Schema Template (Community volunteers)
        DocumentReference respRef = db.collection("responses").document("schema_template_response");
        Map<String, Object> respSchema = new HashMap<>();
        respSchema.put("responseId", "schema_template_response");
        respSchema.put("postId", "schema_template_post");
        respSchema.put("volunteerId", "schema_template_user");
        respSchema.put("message", "Template volunteer message");
        respSchema.put("status", "pending");
        respSchema.put("timestamp", currentTimestamp);
        batch.set(respRef, respSchema);

        // 6. CHATS Schema Template (Inbox View metadata)
        DocumentReference chatRef = db.collection("chats").document("schema_template_chat");
        Map<String, Object> chatSchema = new HashMap<>();
        chatSchema.put("chatId", "schema_template_chat");
        chatSchema.put("bookingId", "schema_template_booking");
        chatSchema.put("seekerId", "schema_template_user");
        chatSchema.put("providerId", "schema_template_user");
        chatSchema.put("lastMessage", "Template last message");
        chatSchema.put("timestamp", currentTimestamp);
        batch.set(chatRef, chatSchema);

        // 7. MESSAGES Schema Template (Individual text bubbles)
        DocumentReference msgRef = db.collection("messages").document("schema_template_msg");
        Map<String, Object> msgSchema = new HashMap<>();
        msgSchema.put("messageId", "schema_template_msg");
        msgSchema.put("chatId", "schema_template_chat");
        msgSchema.put("senderId", "schema_template_user");
        msgSchema.put("text", "Template chat bubble");
        msgSchema.put("timestamp", currentTimestamp);
        batch.set(msgRef, msgSchema);

        // 8. NOTIFICATIONS Schema Template
        DocumentReference notifRef = db.collection("notifications").document("schema_template_notif");
        Map<String, Object> notifSchema = new HashMap<>();
        notifSchema.put("notificationId", "schema_template_notif");
        notifSchema.put("userId", "schema_template_user");
        notifSchema.put("title", "Template Notification");
        notifSchema.put("body", "Template body text");
        notifSchema.put("type", "general"); // "booking_update", "payment", "general"
        notifSchema.put("isRead", false);
        notifSchema.put("timestamp", currentTimestamp);
        batch.set(notifRef, notifSchema);

        // Execute batch write to lock in schemas
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully seeded schema templates to Firestore.");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error seeding schema templates", e);
                    if (callback != null) callback.onFailure(e);
                });
    }
}
