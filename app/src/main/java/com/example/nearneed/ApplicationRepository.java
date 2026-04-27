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
     * Fetches the applicant's profile from Firestore before saving so seekers
     * see real name, rating, location and photo in the applicants list.
     */
    public static void submitApplication(String postId, String postTitle, String postType,
                                         String creatorId, String message, String budget,
                                         String paymentMethod, SaveCallback callback) {
        if (postId == null || callback == null) return;

        String applicantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final Double[] budgetValue = {null};
        if (budget != null) {
            try { budgetValue[0] = Double.parseDouble(budget.replace("\u20b9", "").trim()); }
            catch (NumberFormatException ignored) {}
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch applicant profile first, then save application with real details
        db.collection("users").document(applicantId).get()
                .addOnSuccessListener(doc -> {
                    Application app = buildApp(postId, applicantId, message, postTitle, postType,
                            creatorId, budgetValue[0], paymentMethod);
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) name = doc.getString("fullName");
                        app.applicantName     = name != null ? name : "";

                        String photo = doc.getString("photoUrl");
                        if (photo == null) photo = doc.getString("profileImageUrl");
                        app.applicantPhotoUrl = photo;

                        Object ratingObj = doc.get("rating");
                        if (ratingObj instanceof Number)
                            app.applicantRating = ((Number) ratingObj).doubleValue();

                        String loc = doc.getString("city");
                        if (loc == null) loc = doc.getString("address");
                        app.applicantLocation = loc;

                        String phone = doc.getString("phone");
                        if (phone == null) phone = doc.getString("phoneNumber");
                        app.applicantPhone = phone;
                    }
                    db.collection(APPLICATIONS_COLLECTION).add(app)
                            .addOnSuccessListener(ref -> {
                                callback.onSuccess(ref.getId());
                                if (creatorId != null && !creatorId.isEmpty()) {
                                    String name = (app.applicantName != null && !app.applicantName.isEmpty())
                                            ? app.applicantName : "Someone";
                                    String body = postTitle != null
                                            ? name + " applied to \"" + postTitle + "\""
                                            : name + " applied to your post";
                                    FcmNotifier.sendToUser(creatorId, "New Application Received", body);
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(e -> {
                    // Profile fetch failed — save with blank fields (graceful degradation)
                    Application app = buildApp(postId, applicantId, message, postTitle, postType,
                            creatorId, budgetValue[0], paymentMethod);
                    db.collection(APPLICATIONS_COLLECTION).add(app)
                            .addOnSuccessListener(ref -> {
                                callback.onSuccess(ref.getId());
                                if (creatorId != null && !creatorId.isEmpty()) {
                                    FcmNotifier.sendToUser(creatorId, "New Application Received",
                                            "Someone applied to your post");
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                });
    }

    /** Helper to build an Application object with shared fields. */
    private static Application buildApp(String postId, String applicantId, String message,
                                        String postTitle, String postType, String creatorId,
                                        Double budget, String paymentMethod) {
        Application app  = new Application(postId, applicantId, message);
        app.postTitle    = postTitle;
        app.postType     = postType;
        app.creatorId    = creatorId;
        app.appliedAt    = System.currentTimeMillis();
        app.proposedBudget = budget;
        app.paymentMethod  = paymentMethod;
        return app;
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
                    if ("accepted".equals(status) || "rejected".equals(status)) {
                        db.collection(APPLICATIONS_COLLECTION).document(applicationId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc == null || !doc.exists()) return;
                                    String applicantId = doc.getString("applicantId");
                                    String postTitle   = doc.getString("postTitle");
                                    if (applicantId == null || applicantId.isEmpty()) return;
                                    String title, body;
                                    if ("accepted".equals(status)) {
                                        title = "Application Accepted!";
                                        body  = postTitle != null
                                                ? "You were accepted for \"" + postTitle + "\""
                                                : "Your application was accepted!";
                                    } else {
                                        title = "Application Update";
                                        body  = postTitle != null
                                                ? "Your application for \"" + postTitle + "\" was not selected"
                                                : "Your application was not selected";
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
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snapshot != null) {
                        List<Application> applications = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Application app = fromSnapshot(doc);
                            if (app != null) applications.add(app);
                        }
                        
                        // Client-side sort by timestamp descending (newest first)
                        java.util.Collections.sort(applications, (a1, a2) -> {
                            Long t1 = a1.timestamp != null ? a1.timestamp : 0L;
                            Long t2 = a2.timestamp != null ? a2.timestamp : 0L;
                            return t2.compareTo(t1);
                        });
                        
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
