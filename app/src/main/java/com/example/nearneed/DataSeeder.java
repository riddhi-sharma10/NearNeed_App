package com.example.nearneed;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class DataSeeder {

    private static final String TAG = "DataSeeder";
    private final FirebaseFirestore db;

    public DataSeeder() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface SeederCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Master Reset: Clears specific collections and seeds with fresh, structured data.
     */
    public void resetAndSeed(SeederCallback callback) {
        Log.d(TAG, "Starting Full Database Reset and Rebuild...");
        
        String[] collections = {
            "users", "Users", 
            "posts", "Posts", 
            "applications", "Applications", 
            "bookings", "Bookings", 
            "chats", "Chats", 
            "messages", "Messages",
            "notifications", "Notifications",
            "responses", "Responses"
        };
        
        clearCollections(collections, 0, new SeederCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "All collections cleared. Starting fresh seeding...");
                performFreshSeed(callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to clear collections", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    private void clearCollections(String[] collections, int index, SeederCallback callback) {
        if (index >= collections.length) {
            callback.onSuccess();
            return;
        }

        String collectionName = collections[index];
        db.collection(collectionName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                clearCollections(collections, index + 1, callback);
                return;
            }

            WriteBatch batch = db.batch();
            int count = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
                count++;
                if (count >= 450) { // Stay safely below 500 limit
                    break;
                }
            }
            
            batch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Cleared batch for: " + collectionName);
                // Recursively call until collection is empty
                clearCollections(collections, index, callback);
            }).addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error accessing " + collectionName + " - likely rules restriction.", e);
            // Move to next collection even if one fails due to rules
            clearCollections(collections, index + 1, callback);
        });
    }

    private void performFreshSeed(SeederCallback callback) {
        WriteBatch batch = db.batch();
        long now = System.currentTimeMillis();

        // 1. Seed Users
        seedUser(batch, "sample_seeker_1", "Julian Vance", "julian@example.com", "seeker", 40.7128, -74.0060);
        seedUser(batch, "sample_provider_1", "Sarah Johnson", "sarah@example.com", "provider", 40.7306, -73.9352);
        seedUser(batch, "sample_provider_2", "Mike Miller", "mike@example.com", "provider", 40.7589, -73.9851);

        // 2. Seed Posts
        seedPost(batch, "post_1", "gig", "Deep Kitchen Cleaning", "Need a full deep clean of kitchen.", "sample_seeker_1", 40.7128, -74.0060, "Cleaning", "₹800", null, now);
        seedPost(batch, "post_2", "community", "Central Park Cleanup", "Join us for a morning cleanup drive.", "sample_seeker_1", 40.7850, -73.9682, "Environment", null, 10, now - 500000);
        seedPost(batch, "post_3", "gig", "Yoga Trainer Needed", "Need a personal yoga trainer.", "sample_seeker_1", 40.7130, -74.0062, "Fitness", "₹500", null, now - 1000000);

        // 3. Seed Applications
        seedApplication(batch, "app_1", "post_1", "sample_provider_1", "I am an expert in cleaning.", "pending", now);
        seedApplication(batch, "app_2", "post_2", "sample_provider_2", "I love parks!", "accepted", now - 200000);

        // 4. Seed Bookings
        seedBooking(batch, "book_1", "post_1", "sample_seeker_1", "sample_provider_1", "upcoming", "pending", now);

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Fresh data seeded successfully.");
            if (callback != null) callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Seeding failed", e);
            if (callback != null) callback.onFailure(e);
        });
    }

    private void seedUser(WriteBatch batch, String id, String name, String email, String role, double lat, double lon) {
        Map<String, Object> u = new HashMap<>();
        u.put("name", name);
        u.put("email", email);
        u.put("phone", "1234567890");
        u.put("role", role);
        u.put("isVerified", true);
        u.put("profileImageUrl", "");
        u.put("latitude", lat);
        u.put("longitude", lon);
        if ("provider".equals(role)) {
            u.put("category", "General");
            u.put("rating", 4.5);
        }
        batch.set(db.collection("users").document(id), u);
    }

    private void seedPost(WriteBatch batch, String id, String type, String title, String desc, String creator, double lat, double lon, String cat, String budget, Integer vol, long ts) {
        Map<String, Object> p = new HashMap<>();
        p.put("type", type);
        p.put("title", title);
        p.put("description", desc);
        p.put("createdBy", creator);
        p.put("latitude", lat);
        p.put("longitude", lon);
        p.put("category", cat);
        if ("gig".equals(type)) p.put("budget", budget);
        if ("community".equals(type)) p.put("volunteersNeeded", vol);
        p.put("timestamp", ts);
        batch.set(db.collection("posts").document(id), p);
    }

    private void seedApplication(WriteBatch batch, String id, String postId, String applicantId, String message, String status, long ts) {
        Map<String, Object> a = new HashMap<>();
        a.put("postId", postId);
        a.put("applicantId", applicantId);
        a.put("message", message);
        a.put("status", status);
        a.put("timestamp", ts);
        batch.set(db.collection("applications").document(id), a);
    }

    private void seedBooking(WriteBatch batch, String id, String postId, String seekerId, String providerId, String status, String payStatus, long ts) {
        Map<String, Object> b = new HashMap<>();
        b.put("postId", postId);
        b.put("seekerId", seekerId);
        b.put("providerId", providerId);
        b.put("status", status);
        b.put("paymentStatus", payStatus);
        b.put("timestamp", ts);
        batch.set(db.collection("bookings").document(id), b);
    }
}
