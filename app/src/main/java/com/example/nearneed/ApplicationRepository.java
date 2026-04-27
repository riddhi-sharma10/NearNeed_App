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
 * Repository for managing Application data with Firestore.
 * Firestore structure: applications/{applicationId}
 */
public class ApplicationRepository {

    private static final String APPLICATIONS_COLLECTION = "applications";

    public interface ApplicationListener {
        void onApplicationsLoaded(List<Application> applications);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess(String applicationId);
        void onFailure(Exception e);
    }

    /**
     * Submit an application to a post (simple form).
     */
    public static void submitApplication(String postId, String message, SaveCallback callback) {
        submitApplication(postId, null, null, null, message, null, null, callback);
    }

    /**
     * Submit an application to a post (full form).
     */
    public static void submitApplication(String postId, String postTitle, String postType,
                                         String creatorId, String message, String budget,
                                         String paymentMethod, SaveCallback callback) {
        if (postId == null || callback == null) return;

        String applicantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Application app = new Application(postId, applicantId, message);
        app.postTitle = postTitle;
        app.postType = postType;
        app.creatorId = creatorId;
        app.appliedAt = System.currentTimeMillis();
        if (budget != null) {
            try {
                app.proposedBudget = Double.parseDouble(budget.replace("₹", "").trim());
            } catch (NumberFormatException ignored) {}
        }
        app.paymentMethod = paymentMethod;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Enrich with applicant profile before saving so the seeker sees name/phone/location
        db.collection("users").document(applicantId).get()
                .addOnSuccessListener(profileDoc -> {
                    if (profileDoc != null && profileDoc.exists()) {
                        app.applicantName     = profileDoc.getString("name");
                        app.applicantPhone    = profileDoc.getString("phone");
                        app.applicantLocation = profileDoc.getString("location");
                        Double rating = profileDoc.getDouble("rating");
                        if (rating != null) app.applicantRating = rating;
                    }
                    saveApplication(db, app, creatorId, postTitle, callback);
                })
                .addOnFailureListener(e -> saveApplication(db, app, creatorId, postTitle, callback));
    }

    /**
     * Update application status.
     */
    public static void updateApplicationStatus(String applicationId, String status, SaveCallback callback) {
        if (applicationId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(applicationId);
                    // Notify the applicant when their application is accepted or rejected
                    if ("accepted".equals(status) || "rejected".equals(status)) {
                        db.collection(APPLICATIONS_COLLECTION).document(applicationId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc == null || !doc.exists()) return;
                                    String applicantId = doc.getString("applicantId");
                                    String postTitle   = doc.getString("postTitle");
                                    if (applicantId == null || applicantId.isEmpty()) return;

                                    String title, body;
                                    if ("accepted".equals(status)) {
                                        title = "Application Accepted!";
                                        body  = postTitle != null
                                                ? "You were accepted for \"" + postTitle + "\". Open the app to get started."
                                                : "Your application was accepted! Open the app to get started.";
                                    } else {
                                        title = "Application Update";
                                        body  = postTitle != null
                                                ? "Your application for \"" + postTitle + "\" was not selected."
                                                : "Your application was not selected this time.";
                                    }
                                    FcmNotifier.sendToUser(applicantId, title, body);
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void acceptApplication(String applicationId, SaveCallback callback) {
        updateApplicationStatus(applicationId, "accepted", callback);
    }

    public static void rejectApplication(String applicationId, SaveCallback callback) {
        updateApplicationStatus(applicationId, "rejected", callback);
    }

    public static void withdrawApplication(String applicationId, SaveCallback callback) {
        updateApplicationStatus(applicationId, "withdrawn", callback);
    }

    public static void completeApplication(String applicationId, SaveCallback callback) {
        updateApplicationStatus(applicationId, "completed", callback);
    }

    /**
     * Observe applications for a post (basic).
     */
    public static ListenerRegistration observeApplicationsForPost(String postId, ApplicationListener listener) {
        if (postId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snapshot != null) {
                        List<Application> applications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Application app = fromSnapshot(doc);
                            if (app != null) applications.add(app);
                        }
                        listener.onApplicationsLoaded(applications);
                    }
                });
    }

    /**
     * Observe applications for a post (Context overload for offline-first callers).
     */
    public static ListenerRegistration observeApplicationsForPost(Context context, String postId, ApplicationListener listener) {
        return observeApplicationsForPost(postId, listener);
    }

    /**
     * Load applications for a post from Room cache (stub — falls back to empty list).
     */
    public static void loadApplicationsFromRoom(Context context, String postId, ApplicationListener listener) {
        if (listener != null) listener.onApplicationsLoaded(new ArrayList<>());
    }

    /**
     * Observe current user's own applications in real-time.
     */
    public static ListenerRegistration observeUserApplications(Context context, ApplicationListener listener) {
        if (listener == null) return null;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("applicantId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snapshot != null) {
                        List<Application> applications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Application app = fromSnapshot(doc);
                            if (app != null) applications.add(app);
                        }
                        listener.onApplicationsLoaded(applications);
                    }
                });
    }

    /**
     * Load user's own applications from Room cache (stub — falls back to empty list).
     */
    public static void loadUserApplicationsFromRoom(Context context, String uid, ApplicationListener listener) {
        if (listener != null) listener.onApplicationsLoaded(new ArrayList<>());
    }

    private static void saveApplication(FirebaseFirestore db, Application app,
                                        String creatorId, String postTitle, SaveCallback callback) {
        db.collection(APPLICATIONS_COLLECTION)
                .add(app)
                .addOnSuccessListener(docRef -> {
                    callback.onSuccess(docRef.getId());
                    if (creatorId != null && !creatorId.isEmpty()) {
                        String notifTitle = "New Application Received";
                        String notifBody  = postTitle != null
                                ? (app.applicantName != null ? app.applicantName : "Someone")
                                  + " applied to \"" + postTitle + "\""
                                : "Someone applied to your post";
                        FcmNotifier.sendToUser(creatorId, notifTitle, notifBody);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deserialize Firestore document snapshot to Application object.
     */
    public static Application fromSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Application app = doc.toObject(Application.class);
        if (app != null) {
            app.applicationId = doc.getId();
        }
        return app;
    }
}
