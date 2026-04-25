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

    public interface ApplicationCallback {
        void onApplicationFetched(Application application);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess(String applicationId);
        void onFailure(Exception e);
    }

    /**
     * Fetch a single application by ID.
     */
    public static void getApplication(String applicationId, ApplicationCallback callback) {
        if (applicationId == null || callback == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Application app = fromSnapshot(snapshot);
                        callback.onApplicationFetched(app);
                    } else {
                        callback.onApplicationFetched(null);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Submit an application to a post (Gig or Community).
     */
    public static void submitApplication(String postId, String postTitle, String postType, 
                                         String creatorId, String message, SaveCallback callback) {
        if (postId == null || callback == null) return;

        String applicantId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String applicationId = UUID.randomUUID().toString();

        Application app = new Application(postId, postTitle, postType, applicantId, creatorId);
        app.applicationId = applicationId;
        app.message = message;
        app.appliedAt = System.currentTimeMillis();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .set(app)
                .addOnSuccessListener(aVoid -> callback.onSuccess(applicationId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Observe applications for a post (for post creator / seeker).
     * Returns ListenerRegistration for cleanup.
     */
    public static ListenerRegistration observeApplicationsForPost(Context context, String postId, ApplicationListener listener) {
        if (postId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("postId", postId)
                .orderBy("appliedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Application> applications = new ArrayList<>();
                        List<ApplicationEntity> entities = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Application app = fromSnapshot(doc);
                            if (app != null) {
                                applications.add(app);
                                entities.add(ApplicationEntity.fromApplication(app));
                            }
                        }

                        // Async cache in Room
                        new Thread(() -> {
                            AppDatabase.getDatabase(context).applicationDao().insertAll(entities);
                        }).start();

                        listener.onApplicationsLoaded(applications);
                    }
                });
    }

    /**
     * Load applications from Room for offline access.
     */
    public static void loadApplicationsFromRoom(Context context, String postId, ApplicationListener listener) {
        new Thread(() -> {
            List<ApplicationEntity> entities = AppDatabase.getDatabase(context).applicationDao().getApplicationsForPost(postId);
            List<Application> applications = new ArrayList<>();
            for (ApplicationEntity entity : entities) {
                applications.add(entity.toApplication());
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onApplicationsLoaded(applications);
            });
        }).start();
    }

    /**
     * Observe applications submitted by current user.
     * Returns ListenerRegistration for cleanup.
     */
    public static ListenerRegistration observeUserApplications(Context context, ApplicationListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(APPLICATIONS_COLLECTION)
                .whereEqualTo("applicantId", currentUserId)
                .orderBy("appliedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Application> applications = new ArrayList<>();
                        List<ApplicationEntity> entities = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Application app = fromSnapshot(doc);
                            if (app != null) {
                                applications.add(app);
                                entities.add(ApplicationEntity.fromApplication(app));
                            }
                        }

                        // Async cache in Room
                        new Thread(() -> {
                            AppDatabase.getDatabase(context).applicationDao().insertAll(entities);
                        }).start();

                        listener.onApplicationsLoaded(applications);
                    }
                });
    }

    /**
     * Load user's submitted applications from Room.
     */
    public static void loadUserApplicationsFromRoom(Context context, String userId, ApplicationListener listener) {
        new Thread(() -> {
            List<ApplicationEntity> entities = AppDatabase.getDatabase(context).applicationDao().getUserApplications(userId);
            List<Application> applications = new ArrayList<>();
            for (ApplicationEntity entity : entities) {
                applications.add(entity.toApplication());
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onApplicationsLoaded(applications);
            });
        }).start();
    }

    /**
     * Accept an application (for post creator / provider).
     */
    public static void acceptApplication(String applicationId, SaveCallback callback) {
        if (applicationId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("providerStatus", "accepted");
        updates.put("acceptedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(applicationId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Reject an application.
     */
    public static void rejectApplication(String applicationId, SaveCallback callback) {
        if (applicationId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("providerStatus", "rejected");
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(applicationId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Withdraw an application (applicant action).
     */
    public static void withdrawApplication(String applicationId, SaveCallback callback) {
        if (applicationId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("seekerStatus", "withdrawn");
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(applicationId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Mark application as completed.
     */
    public static void completeApplication(String applicationId, SaveCallback callback) {
        if (applicationId == null || callback == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(APPLICATIONS_COLLECTION)
                .document(applicationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(applicationId))
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
