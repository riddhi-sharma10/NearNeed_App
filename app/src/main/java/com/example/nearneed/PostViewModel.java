package com.example.nearneed;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing Post data lifecycle.
 * Centralizes all post operations and ensures listeners are cleaned up properly.
 */
public class PostViewModel extends ViewModel {

    private MutableLiveData<List<Post>> userPosts;
    private MutableLiveData<List<Post>> nearbyPosts;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    
    private ListenerRegistration userPostsListener;
    private ListenerRegistration nearbyPostsListener;

    /**
     * Get live data for user's own posts.
     */
    public LiveData<List<Post>> getUserPosts() {
        if (userPosts == null) {
            userPosts = new MutableLiveData<>();
            userPosts.setValue(new ArrayList<>());
        }
        return userPosts;
    }

    /**
     * Get live data for nearby posts.
     */
    public LiveData<List<Post>> getNearbyPosts() {
        if (nearbyPosts == null) {
            nearbyPosts = new MutableLiveData<>();
            nearbyPosts.setValue(new ArrayList<>());
        }
        return nearbyPosts;
    }

    /**
     * Get error messages as live data.
     */
    public LiveData<String> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new MutableLiveData<>();
        }
        return errorMessage;
    }

    /**
     * Get loading state.
     */
    public LiveData<Boolean> getIsLoading() {
        if (isLoading == null) {
            isLoading = new MutableLiveData<>(false);
        }
        return isLoading;
    }

    /**
     * Observe user's own posts in real-time.
     */
    public void observeUserPosts(Context context, String userId) {
        // Remove old listener if exists
        if (userPostsListener != null) {
            userPostsListener.remove();
        }

        // Offline First: Load from Room first
        PostRepository.loadPostsFromRoom(context, userId, new PostRepository.PostListener() {
            @Override
            public void onPostsLoaded(List<Post> posts) {
                if (userPosts.getValue() == null || userPosts.getValue().isEmpty()) {
                    userPosts.setValue(posts);
                }
            }
            @Override
            public void onError(Exception e) {}
        });

        isLoading.setValue(true);
        userPostsListener = PostRepository.observeUserPosts(context, userId, new PostRepository.PostListener() {
            @Override
            public void onPostsLoaded(List<Post> posts) {
                userPosts.setValue(posts);
                isLoading.setValue(false);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error loading posts: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Observe nearby posts in real-time.
     */
    public void observeNearbyPosts(Context context, double latitude, double longitude, double radiusKm) {
        // Remove old listener if exists
        if (nearbyPostsListener != null) {
            nearbyPostsListener.remove();
        }

        isLoading.setValue(true);
        nearbyPostsListener = PostRepository.observeNearbyPosts(context, latitude, longitude, radiusKm, new PostRepository.PostListener() {
            @Override
            public void onPostsLoaded(List<Post> posts) {
                nearbyPosts.setValue(posts);
                isLoading.setValue(false);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error loading nearby posts: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Create a new post.
     */
    public void createPost(Post post, PostRepository.SaveCallback callback) {
        isLoading.setValue(true);
        PostRepository.createPost(post, new PostRepository.SaveCallback() {
            @Override
            public void onSuccess(String postId) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(postId);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to create post: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Update an existing post.
     */
    public void updatePost(String postId, java.util.Map<String, Object> updates, PostRepository.SaveCallback callback) {
        isLoading.setValue(true);
        PostRepository.updatePost(postId, updates, new PostRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to update post: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Delete a post.
     */
    public void deletePost(String postId, PostRepository.SaveCallback callback) {
        isLoading.setValue(true);
        PostRepository.deletePost(postId, new PostRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to delete post: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Clean up listeners when ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (userPostsListener != null) {
            userPostsListener.remove();
            userPostsListener = null;
        }
        if (nearbyPostsListener != null) {
            nearbyPostsListener.remove();
            nearbyPostsListener = null;
        }
    }
}
