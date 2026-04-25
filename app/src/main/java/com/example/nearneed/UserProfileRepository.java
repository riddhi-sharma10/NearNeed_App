package com.example.nearneed;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

public final class UserProfileRepository {

    public interface ProfileListener {
        void onProfileChanged(@Nullable UserProfile profile);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(@NonNull Exception error);
    }

    private static final String USERS_COLLECTION = "Users";

    private UserProfileRepository() {
    }

    @Nullable
    private static FirebaseUser currentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    private static DocumentReference currentUserDoc() {
        FirebaseUser user = currentUser();
        if (user == null) {
            return null;
        }
        return FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(user.getUid());
    }

    @Nullable
    public static ListenerRegistration observeCurrentUserProfile(@NonNull Context context,
                                                                 @NonNull ProfileListener listener) {
        DocumentReference doc = currentUserDoc();
        if (doc == null) {
            listener.onProfileChanged(null);
            return null;
        }

        return doc.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) {
                listener.onProfileChanged(null);
                return;
            }

            UserProfile profile = fromSnapshot(snapshot);
            cache(context, profile);
            listener.onProfileChanged(profile);
        });
    }

    public static void fetchCurrentUserProfile(@NonNull Context context,
                                               @NonNull ProfileListener listener) {
        DocumentReference doc = currentUserDoc();
        if (doc == null) {
            listener.onProfileChanged(null);
            return;
        }

        doc.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) {
                        listener.onProfileChanged(null);
                        return;
                    }

                    UserProfile profile = fromSnapshot(snapshot);
                    cache(context, profile);
                    listener.onProfileChanged(profile);
                })
                .addOnFailureListener(e -> listener.onProfileChanged(null));
    }

    public static void saveCurrentUserProfile(@NonNull Map<String, Object> updates,
                                              @Nullable SaveCallback callback) {
        DocumentReference doc = currentUserDoc();
        if (doc == null) {
            if (callback != null) {
                callback.onFailure(new IllegalStateException("No authenticated user"));
            }
            return;
        }

        doc.set(updates, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    @Nullable
    public static UserProfile fromSnapshot(@NonNull DocumentSnapshot snapshot) {
        UserProfile profile = new UserProfile();
        profile.fullName = snapshot.getString("fullName");
        profile.location = snapshot.getString("location");
        profile.photoUrl = snapshot.getString("photoUrl");
        profile.bio = snapshot.getString("bio");
        profile.dob = snapshot.getString("dob");
        profile.email = snapshot.getString("email");
        profile.phone = snapshot.getString("phone");
        profile.isVerified = snapshot.getBoolean("isVerified");
        profile.lat = snapshot.getDouble("lat");
        profile.lng = snapshot.getDouble("lng");
        return profile;
    }

    private static void cache(@NonNull Context context, @Nullable UserProfile profile) {
        if (profile == null) {
            return;
        }

        // SharedPreferences cache (Legacy)
        if (profile.fullName != null && !profile.fullName.isEmpty()) {
            UserPrefs.saveName(context, profile.fullName);
        }
        if (profile.location != null && !profile.location.isEmpty()) {
            UserPrefs.saveLocation(context, profile.location);
        }
        if (profile.photoUrl != null && !profile.photoUrl.isEmpty()) {
            UserPrefs.savePhotoUri(context, profile.photoUrl);
        }
        if (profile.isVerified != null) {
            UserPrefs.saveVerified(context, profile.isVerified);
        }

        // Room cache (Modern)
        FirebaseUser user = currentUser();
        if (user != null) {
            new Thread(() -> {
                UserEntity entity = new UserEntity();
                entity.userId = user.getUid();
                entity.name = profile.fullName;
                entity.phone = profile.phone;
                entity.address = profile.location;
                entity.profileImageUrl = profile.photoUrl;
                entity.bio = profile.bio;
                // Add more fields if needed
                AppDatabase.getDatabase(context).userDao().insert(entity);
            }).start();
        }
    }

    /**
     * Load profile from Room for instant UI updates.
     */
    public static void loadProfileFromRoom(@NonNull Context context, @NonNull String userId, @NonNull ProfileListener listener) {
        new Thread(() -> {
            UserEntity entity = AppDatabase.getDatabase(context).userDao().getUser(userId);
            if (entity != null) {
                UserProfile profile = entity.toProfile();
                // Map back fields if names differ
                profile.fullName = entity.name;
                profile.photoUrl = entity.profileImageUrl;
                profile.location = entity.address;
                
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onProfileChanged(profile);
                });
            }
        }).start();
    }
}
