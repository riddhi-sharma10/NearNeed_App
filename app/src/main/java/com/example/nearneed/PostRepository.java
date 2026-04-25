package com.example.nearneed;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for managing Post (Gig/Community) data with Firestore.
 * Firestore structure: posts/{postId}
 */
public class PostRepository {

    private static final String POSTS_COLLECTION = "posts";

    public interface PostListener {
        void onPostsLoaded(java.util.List<Post> posts);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess(String postId);
        void onFailure(Exception e);
    }

    /**
     * Observe current user's own posts in real-time.
     * Returns a ListenerRegistration for cleanup in onStop().
     */
    public static ListenerRegistration observeUserPosts(Context context, String userId, PostListener listener) {
        if (userId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        java.util.List<PostEntity> entities = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = fromSnapshot(doc);
                            if (post != null) {
                                posts.add(post);
                                entities.add(PostEntity.fromPost(post));
                            }
                        }
                        
                        // Async cache in Room
                        new Thread(() -> {
                            AppDatabase.getDatabase(context).postDao().insertAll(entities);
                        }).start();

                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Load posts from local Room cache for immediate display.
     */
    public static void loadPostsFromRoom(Context context, String userId, PostListener listener) {
        new Thread(() -> {
            java.util.List<PostEntity> entities = AppDatabase.getDatabase(context).postDao().getUserPosts(userId);
            java.util.List<Post> posts = new java.util.ArrayList<>();
            for (PostEntity entity : entities) {
                posts.add(entity.toPost());
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onPostsLoaded(posts);
            });
        }).start();
    }

    /**
     * Observe nearby posts (within radius) in real-time, optionally filtered by type.
     * Returns a ListenerRegistration for cleanup in onStop().
     * Note: Firestore doesn't support spatial queries directly; this fetches all posts
     * and client-side filters by distance. For production, consider Firestore's
     * geographic queries or Google Maps API.
     */
    public static ListenerRegistration observeNearbyPosts(Context context, double userLat, double userLng, 
                                                           double radiusKm, PostListener listener) {
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .whereEqualTo("status", "active")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = fromSnapshot(doc);
                            if (post != null && post.lat != null && post.lng != null) {
                                // Client-side distance filter
                                double distance = calculateDistance(userLat, userLng, post.lat, post.lng);
                                if (distance <= radiusKm) {
                                    post.distance = String.format("%.1f km away", distance);
                                    posts.add(post);
                                }
                            }
                        }
                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Fetch all active posts (used by adapters that don't need real-time updates).
     */
    public static void fetchAllActivePosts(PostListener listener) {
        if (listener == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(POSTS_COLLECTION)
                .whereEqualTo("status", "active")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    java.util.List<Post> posts = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Post post = fromSnapshot(doc);
                        if (post != null) {
                            posts.add(post);
                        }
                    }
                    listener.onPostsLoaded(posts);
                })
                .addOnFailureListener(listener::onError);
    }

    /**
     * Create a new post.
     */
    public static void createPost(Post post, SaveCallback callback) {
        if (post == null || callback == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        post.userId = currentUserId;
        post.createdAt = System.currentTimeMillis();
        post.status = "active";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference docRef = db.collection(POSTS_COLLECTION).document();
        docRef.set(post)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(docRef.getId());
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Update an existing post.
     */
    public static void updatePost(String postId, Map<String, Object> updates, SaveCallback callback) {
        if (postId == null || updates == null || callback == null) return;

        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(POSTS_COLLECTION)
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(postId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete a post.
     */
    public static void deletePost(String postId, SaveCallback callback) {
        if (postId == null || callback == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(POSTS_COLLECTION)
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(postId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deserialize Firestore document snapshot to Post object.
     */
    public static Post fromSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        Post post = doc.toObject(Post.class);
        if (post != null) {
            post.postId = doc.getId();
        }
        return post;
    }

    /**
     * Calculate distance between two geographic points (in kilometers).
     * Uses Haversine formula.
     */
    private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
