package com.example.nearneed;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Centralized database constants to prevent typos (e.g. "Users" vs "users")
 * and manage fallback logic consistently across the app.
 */
public class DbConstants {

    // Collections
    public static final String COL_USERS = "users";
    public static final String COL_POSTS = "posts";
    public static final String COL_BOOKINGS = "bookings";
    public static final String COL_APPLICATIONS = "applications";
    public static final String COL_NOTIFICATIONS = "notifications";

    // Common User Fields
    public static final String FIELD_USER_NAME = "name";
    public static final String FIELD_USER_FULL_NAME = "fullName";
    public static final String FIELD_USER_LOCATION = "location";
    public static final String FIELD_USER_PHOTO = "photoUrl";

    /**
     * Safely extracts the user's display name, handling the historical mix-up
     * between "name" and "fullName" fields in the database.
     */
    public static String getSafeName(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        
        String name = snapshot.getString(FIELD_USER_NAME);
        if (name == null || name.isEmpty()) {
            name = snapshot.getString(FIELD_USER_FULL_NAME);
        }
        return name;
    }

    /**
     * Generates a deterministic Cataas URL based on the userId so that each
     * user gets a consistent, unique cat avatar when no profile picture is set.
     */
    public static String getCatAvatarUrl(String userId) {
        if (userId == null) userId = "default";
        int slot = Math.abs(userId.hashCode() % 50);
        // Using ?uid= ensures Glide caches it uniquely per user.
        return "https://cataas.com/cat/cute?uid=" + slot;
    }
}
