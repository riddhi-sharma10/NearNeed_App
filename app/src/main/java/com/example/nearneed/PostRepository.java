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
     */
    public static ListenerRegistration observeUserPosts(Context context, String userId, PostListener listener) {
        if (userId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .whereEqualTo("createdBy", userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = fromSnapshot(doc);
                            if (post != null) {
                                posts.add(post);
                            }
                        }
                        
                        // Client-side sort by timestamp descending
                        java.util.Collections.sort(posts, (p1, p2) -> {
                            Long t1 = p1.timestamp != null ? p1.timestamp : 0L;
                            Long t2 = p2.timestamp != null ? p2.timestamp : 0L;
                            return t2.compareTo(t1);
                        });
                        
                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Observe all active posts in real-time.
     */
    public static ListenerRegistration observeAllActivePosts(PostListener listener) {
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = fromSnapshot(doc);
                            if (post != null) {
                                posts.add(post);
                            }
                        }
                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Observe nearby posts (within radius) in real-time.
     */
    public static ListenerRegistration observeNearbyPosts(Context context, double userLat, double userLng, 
                                                           double radiusKm, PostListener listener) {
        if (listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Post post = fromSnapshot(doc);
                            if (post != null && post.latitude != null && post.longitude != null) {
                                // Client-side distance filter
                                double distance = calculateDistance(userLat, userLng, post.latitude, post.longitude);
                                if (distance <= radiusKm) {
                                    posts.add(post);
                                }
                            }
                        }
                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Observe a single post by id in real-time.
     */
    public static ListenerRegistration observePostById(String postId, PostListener listener) {
        if (postId == null || postId.trim().isEmpty() || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(POSTS_COLLECTION)
                .document(postId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        java.util.List<Post> posts = new java.util.ArrayList<>();
                        Post post = fromSnapshot(snapshot);
                        if (post != null) {
                            posts.add(post);
                        }
                        listener.onPostsLoaded(posts);
                    }
                });
    }

    /**
     * Create a new post.
     */
    public static void createPost(Post post, SaveCallback callback) {
        if (post == null || callback == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        post.createdBy = currentUserId;
        post.timestamp = System.currentTimeMillis();

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
        updates.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .document(postId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess(postId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete a post.
     */
    public static void deletePost(String postId, SaveCallback callback) {
        if (postId == null || callback == null) return;
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .document(postId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess(postId))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deserialize Firestore document snapshot to Post object.
     */
    public static Post fromSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;

        try {
            Post post = doc.toObject(Post.class);
            if (post != null) {
                post.postId = doc.getId();
                if (post.latitude == null && doc.contains("lat")) post.latitude = doc.getDouble("lat");
                if (post.longitude == null && doc.contains("lng")) post.longitude = doc.getDouble("lng");
            }
            return post;
        } catch (Exception e) {
            android.util.Log.e("PostRepository", "Error deserializing post: " + doc.getId(), e);
            return null;
        }
    }

    /**
     * Calculate distance between two geographic points (in kilometers).
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
