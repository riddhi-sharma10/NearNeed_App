package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;

public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_ROLE_CHANGED_AT = "role_changed_at";

    public static final String ROLE_SEEKER = "SEEKER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    /**
     * Get the current user role. Default is SEEKER.
     * @param context Application context
     * @return Current role (SEEKER or PROVIDER)
     */
    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, ROLE_SEEKER);
    }

    /**
     * Alias for getRole() - getCurrentRole provides more semantic naming
     * @param context Application context
     * @return Current role (SEEKER or PROVIDER)
     */
    public static String getCurrentRole(Context context) {
        return getRole(context);
    }

    /**
     * Set the user role and persist it
     * @param context Application context
     * @param role Role to set (ROLE_SEEKER or ROLE_PROVIDER)
     */
    public static void setRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_ROLE_CHANGED_AT, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if user is in Provider mode
     * @param context Application context
     * @return true if role is PROVIDER, false otherwise
     */
    public static boolean isProvider(Context context) {
        return ROLE_PROVIDER.equals(getRole(context));
    }

    /**
     * Check if user is in Seeker mode
     * @param context Application context
     * @return true if role is SEEKER, false otherwise
     */
    public static boolean isSeeker(Context context) {
        return ROLE_SEEKER.equals(getRole(context));
    }

    /**
     * Get timestamp of last role change
     * @param context Application context
     * @return Timestamp in milliseconds, or 0 if never changed
     */
    public static long getRoleChangedAt(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_ROLE_CHANGED_AT, 0);
    }

    /**
     * Reset role to default (SEEKER)
     * @param context Application context
     */
    public static void resetRole(Context context) {
        setRole(context, ROLE_SEEKER);
    }

    /**
     * Toggle between SEEKER and PROVIDER roles
     * @param context Application context
     */
    public static void toggleRole(Context context) {
        String currentRole = getRole(context);
        String newRole = ROLE_SEEKER.equals(currentRole) ? ROLE_PROVIDER : ROLE_SEEKER;
        setRole(context, newRole);
    }
}
