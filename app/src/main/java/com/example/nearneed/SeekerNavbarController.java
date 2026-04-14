package com.example.nearneed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class SeekerNavbarController {

    // Tab constants
    public static final int TAB_HOME     = 0;
    public static final int TAB_MAP      = 1;
    public static final int TAB_BOOKINGS = 2;
    public static final int TAB_CHAT     = 3;
    public static final int TAB_PROFILE  = 4;

    private SeekerNavbarController() {
    }

    // New primary method with tab constants
    public static void bind(@NonNull Activity activity, @NonNull View root, int activeTab) {
        FrameLayout homeContainer = root.findViewById(R.id.nav_home_container);
        FrameLayout mapContainer = root.findViewById(R.id.nav_map_container);
        FrameLayout bookingsContainer = root.findViewById(R.id.nav_bookings_container);
        FrameLayout chatContainer = root.findViewById(R.id.nav_chat_container);
        FrameLayout profileContainer = root.findViewById(R.id.nav_profile_container);

        View homeActiveBg = root.findViewById(R.id.nav_home_active_bg);
        View mapActiveBg = root.findViewById(R.id.nav_map_active_bg);
        View bookingsActiveBg = root.findViewById(R.id.nav_bookings_active_bg);
        View chatActiveBg = root.findViewById(R.id.nav_chat_active_bg);
        View profileActiveBg = root.findViewById(R.id.nav_profile_active_bg);

        ImageView homeIcon = root.findViewById(R.id.nav_home_icon);
        ImageView mapIcon = root.findViewById(R.id.nav_map_icon);
        ImageView bookingsIcon = root.findViewById(R.id.nav_bookings_icon);
        ImageView chatIcon = root.findViewById(R.id.nav_chat_icon);
        ImageView profileIcon = root.findViewById(R.id.nav_profile_icon);

        // Hide all active backgrounds first
        if (homeActiveBg != null) homeActiveBg.setVisibility(View.GONE);
        if (mapActiveBg != null) mapActiveBg.setVisibility(View.GONE);
        if (bookingsActiveBg != null) bookingsActiveBg.setVisibility(View.GONE);
        if (chatActiveBg != null) chatActiveBg.setVisibility(View.GONE);
        if (profileActiveBg != null) profileActiveBg.setVisibility(View.GONE);

        // Set default colors
        int mutedColor = ContextCompat.getColor(activity, R.color.text_muted);
        int activeColor = ContextCompat.getColor(activity, R.color.sapphire_primary);

        if (homeIcon != null) homeIcon.setColorFilter(mutedColor, PorterDuff.Mode.SRC_IN);
        if (mapIcon != null) mapIcon.setColorFilter(mutedColor, PorterDuff.Mode.SRC_IN);
        if (bookingsIcon != null) bookingsIcon.setColorFilter(mutedColor, PorterDuff.Mode.SRC_IN);
        if (chatIcon != null) chatIcon.setColorFilter(mutedColor, PorterDuff.Mode.SRC_IN);
        if (profileIcon != null) profileIcon.setColorFilter(mutedColor, PorterDuff.Mode.SRC_IN);

        // Highlight active tab
        switch (activeTab) {
            case TAB_HOME:
                if (homeActiveBg != null) homeActiveBg.setVisibility(View.VISIBLE);
                if (homeIcon != null) homeIcon.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
                break;
            case TAB_MAP:
                if (mapActiveBg != null) mapActiveBg.setVisibility(View.VISIBLE);
                if (mapIcon != null) mapIcon.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
                break;
            case TAB_BOOKINGS:
                if (bookingsActiveBg != null) bookingsActiveBg.setVisibility(View.VISIBLE);
                if (bookingsIcon != null) bookingsIcon.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
                break;
            case TAB_CHAT:
                if (chatActiveBg != null) chatActiveBg.setVisibility(View.VISIBLE);
                if (chatIcon != null) chatIcon.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
                break;
            case TAB_PROFILE:
                if (profileActiveBg != null) profileActiveBg.setVisibility(View.VISIBLE);
                if (profileIcon != null) profileIcon.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
                break;
        }

        // Set listeners
        if (homeContainer != null) {
            homeContainer.setOnClickListener(v -> {
                String role = RoleManager.getRole(activity);
                if (RoleManager.ROLE_PROVIDER.equals(role)) {
                    if (!(activity instanceof HomeProviderActivity)) {
                        Intent intent = new Intent(activity, HomeProviderActivity.class);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        activity.finish();
                    }
                } else {
                    if (!(activity instanceof HomeSeekerActivity) && !(activity instanceof HomeSeekerNoPostsActivity)) {
                        Intent intent = new Intent(activity, HomeSeekerActivity.class);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        activity.finish();
                    }
                }
            });
        }

        if (mapContainer != null) {
            mapContainer.setOnClickListener(v -> {
                if (!(activity instanceof MapsActivity)) {
                    Intent intent = new Intent(activity, MapsActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        if (bookingsContainer != null) {
            bookingsContainer.setOnClickListener(v -> {
                if (!(activity instanceof BookingsActivity)) {
                    Intent intent = new Intent(activity, BookingsActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    // Don't finish activity for stack preservation, or finish depending on current?
                    // Home usually finishes itself, but since Bookings is a top-level tab, let's follow the chat/profile pattern which doesn't finish Home.
                }
            });
        }

        if (chatContainer != null) {
            chatContainer.setOnClickListener(v -> {
                if (!(activity instanceof MessagesActivity)) {
                    Intent intent = new Intent(activity, MessagesActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    // Don't finish Home activity so user can come back easily
                }
            });
        }

        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    // Don't finish Home activity
                }
            });
        }
    }

    // Legacy method for backward compatibility
    public static void bind(@NonNull Activity activity, @NonNull View root, boolean homeActive) {
        bind(activity, root, homeActive ? TAB_HOME : TAB_PROFILE);
    }
}
