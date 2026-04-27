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
     * Returns a unique cat avatar url from Cataas for the given userId.
     * By appending the userId as a query parameter (even if cataas ignores it),
     * Glide uses the full URL as the cache key. This ensures that the random cat
     * fetched for this user is cached and remains consistent across the app.
     */
    public static String getCatAvatarUrl(String userId) {
        if (userId == null || userId.isEmpty()) userId = "default";
        return "https://cataas.com/cat?type=square&v=" + userId;
    }
}
