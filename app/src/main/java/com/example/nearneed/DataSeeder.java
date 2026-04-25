package com.example.nearneed;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
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

    public void seedSampleData(SeederCallback callback) {
        Log.d(TAG, "Starting final comprehensive data seeding...");
        WriteBatch batch = db.batch();

        try {
            seedUsers(batch);
            seedPosts(batch);
            seedApplications(batch);
            seedBookings(batch);

            batch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "All sample data committed successfully.");
                if (callback != null) callback.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error committing batch", e);
                if (callback != null) callback.onFailure(e);
            });

        } catch (Exception e) {
            Log.e(TAG, "Seeding logic failed", e);
            if (callback != null) callback.onFailure(e);
        }
    }

    private void seedUsers(WriteBatch batch) {
        // Seeker 1
        Map<String, Object> s1 = new HashMap<>();
        s1.put("userId", "sample_seeker_1");
        s1.put("fullName", "Julian Vance");
        s1.put("email", "julian@example.com");
        s1.put("role", "ROLE_SEEKER");
        s1.put("isVerified", true);
        s1.put("location", "Manhattan, NY");
        batch.set(db.collection("Users").document("sample_seeker_1"), s1);

        // Provider 1
        Map<String, Object> p1 = new HashMap<>();
        p1.put("userId", "sample_provider_1");
        p1.put("fullName", "Sarah Johnson");
        p1.put("email", "sarah@example.com");
        p1.put("role", "ROLE_PROVIDER");
        p1.put("isVerified", true);
        p1.put("location", "Brooklyn, NY");
        batch.set(db.collection("Users").document("sample_provider_1"), p1);
    }

    private void seedPosts(WriteBatch batch) {
        long now = System.currentTimeMillis();

        // GIG 1
        Map<String, Object> g1 = new HashMap<>();
        g1.put("postId", "post_gig_1");
        g1.put("userId", "sample_seeker_1");
        g1.put("title", "Deep Kitchen Cleaning");
        g1.put("description", "Full deep clean of kitchen cabinets and appliances.");
        g1.put("type", "GIG");
        g1.put("category", "Cleaning");
        g1.put("budget", "₹800");
        g1.put("status", "active");
        g1.put("createdAt", now);
        g1.put("lat", 40.7128); // NYC coordinates
        g1.put("lng", -74.0060);
        batch.set(db.collection("posts").document("post_gig_1"), g1);

        // GIG 2
        Map<String, Object> g2 = new HashMap<>();
        g2.put("postId", "post_gig_2");
        g2.put("userId", "sample_seeker_1");
        g2.put("title", "Yoga Trainer Needed");
        g2.put("description", "Need a personal yoga trainer for 1 hour session.");
        g2.put("type", "GIG");
        g1.put("category", "Fitness");
        g2.put("budget", "₹500");
        g2.put("status", "active");
        g2.put("createdAt", now - 100000);
        g2.put("lat", 40.7130);
        g2.put("lng", -74.0062);
        batch.set(db.collection("posts").document("post_gig_2"), g2);

        // COMMUNITY 1
        Map<String, Object> c1 = new HashMap<>();
        c1.put("postId", "post_comm_1");
        c1.put("userId", "sample_seeker_1");
        c1.put("title", "Central Park Cleanup");
        c1.put("description", "Join us for a morning cleanup drive.");
        c1.put("type", "COMMUNITY");
        c1.put("status", "active");
        c1.put("createdAt", now - 200000);
        c1.put("lat", 40.7850);
        c1.put("lng", -73.9682);
        c1.put("slots", 10);
        c1.put("slotsFilled", 3);
        batch.set(db.collection("posts").document("post_comm_1"), c1);
    }

    private void seedApplications(WriteBatch batch) {
        long now = System.currentTimeMillis();

        Map<String, Object> a1 = new HashMap<>();
        a1.put("applicationId", "app_1");
        a1.put("postId", "post_gig_1");
        a1.put("postTitle", "Deep Kitchen Cleaning");
        a1.put("applicantId", "sample_provider_1");
        a1.put("applicantName", "Sarah Johnson");
        a1.put("creatorId", "sample_seeker_1");
        a1.put("status", "pending");
        a1.put("appliedAt", now);
        batch.set(db.collection("applications").document("app_1"), a1);
    }

    private void seedBookings(WriteBatch batch) {
        long now = System.currentTimeMillis();

        // Confirmed (Upcoming)
        Map<String, Object> b1 = new HashMap<>();
        b1.put("bookingId", "book_1");
        b1.put("postTitle", "Garden Maintenance");
        b1.put("seekerId", "sample_seeker_1");
        b1.put("providerId", "sample_provider_1");
        b1.put("providerName", "Sarah Johnson");
        b1.put("status", "confirmed");
        b1.put("createdAt", now);
        batch.set(db.collection("bookings").document("book_1"), b1);

        // In Progress (Ongoing)
        Map<String, Object> b2 = new HashMap<>();
        b2.put("bookingId", "book_2");
        b2.put("postTitle", "Plumbing Repair");
        b2.put("seekerId", "sample_seeker_1");
        b2.put("providerId", "sample_provider_1");
        b2.put("providerName", "Sarah Johnson");
        b2.put("status", "in_progress");
        b2.put("createdAt", now - 3600000);
        batch.set(db.collection("bookings").document("book_2"), b2);

        // Completed (Past)
        Map<String, Object> b3 = new HashMap<>();
        b3.put("bookingId", "book_3");
        b3.put("postTitle", "Car Wash");
        b3.put("seekerId", "sample_seeker_1");
        b3.put("providerId", "sample_provider_1");
        b3.put("providerName", "Sarah Johnson");
        b3.put("status", "completed");
        b3.put("createdAt", now - 86400000);
        batch.set(db.collection("bookings").document("book_3"), b3);
    }
}
