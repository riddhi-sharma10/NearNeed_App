package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;

public class RoleManager {
    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "user_role";
    
    public static final String ROLE_SEEKER = "SEEKER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, ROLE_SEEKER); // Default to SEEKER
    }

    public static void setRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }
}
