package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserPrefs {

    private static final String PREFS = "UserProfile";
    public static final String KEY_NAME = "profile_name";
    public static final String KEY_PHOTO_URI = "profile_photo_uri";
    public static final String KEY_LOCATION = "profile_location";
    public static final String KEY_VERIFIED = "profile_verified";

    private UserPrefs() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void saveName(Context ctx, String name) {
        prefs(ctx).edit().putString(KEY_NAME, name).apply();
    }

    public static String getName(Context ctx) {
        return prefs(ctx).getString(KEY_NAME, "");
    }

    public static void savePhotoUri(Context ctx, String uri) {
        prefs(ctx).edit().putString(KEY_PHOTO_URI, uri).apply();
    }

    public static String getPhotoUri(Context ctx) {
        return prefs(ctx).getString(KEY_PHOTO_URI, null);
    }

    public static void saveLocation(Context ctx, String location) {
        prefs(ctx).edit().putString(KEY_LOCATION, location).apply();
    }

    public static String getLocation(Context ctx) {
        return prefs(ctx).getString(KEY_LOCATION, null);
    }

    public static void saveVerified(Context ctx, boolean verified) {
        prefs(ctx).edit().putBoolean(KEY_VERIFIED, verified).apply();
    }

    public static boolean isVerified(Context ctx) {
        return prefs(ctx).getBoolean(KEY_VERIFIED, false);
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }
}
