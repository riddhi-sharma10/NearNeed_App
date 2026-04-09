package com.example.nearneed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class MessagesNavbarController {

    private MessagesNavbarController() {
    }

    public static void bind(@NonNull Activity activity, @NonNull View root) {
        FrameLayout homeContainer = root.findViewById(R.id.nav_home_container);
        FrameLayout mapContainer = root.findViewById(R.id.nav_map_container);
        FrameLayout bookingsContainer = root.findViewById(R.id.nav_bookings_container);
        FrameLayout chatContainer = root.findViewById(R.id.nav_chat_container);
        FrameLayout profileContainer = root.findViewById(R.id.nav_profile_container);

        ImageView homeIcon = root.findViewById(R.id.nav_home_icon);
        ImageView mapIcon = root.findViewById(R.id.nav_map_icon);
        ImageView bookingsIcon = root.findViewById(R.id.nav_bookings_icon);
        ImageView chatIcon = root.findViewById(R.id.nav_chat_icon);
        ImageView profileIcon = root.findViewById(R.id.nav_profile_icon);

        if (homeContainer != null) {
            homeContainer.setOnClickListener(v -> {
                Intent intent = new Intent(activity, HomeSeekerActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                activity.finish();
            });
        }

        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                RoleManager.setRole(activity, RoleManager.ROLE_SEEKER);
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                activity.finish();
            });
        }

        if (chatContainer != null) {
            chatContainer.setOnClickListener(v -> {
                if (!(activity instanceof MessagesActivity)) {
                    Intent intent = new Intent(activity, MessagesActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    activity.finish();
                }
            });
        }

        if (mapContainer != null) {
            mapContainer.setOnClickListener(v -> android.widget.Toast.makeText(activity, "Map coming soon", android.widget.Toast.LENGTH_SHORT).show());
        }

        if (bookingsContainer != null) {
            bookingsContainer.setOnClickListener(v -> android.widget.Toast.makeText(activity, "Bookings coming soon", android.widget.Toast.LENGTH_SHORT).show());
        }

        if (homeIcon != null) {
            homeIcon.setColorFilter(ContextCompat.getColor(activity, R.color.text_muted), PorterDuff.Mode.SRC_IN);
        }
        if (mapIcon != null) {
            mapIcon.setColorFilter(ContextCompat.getColor(activity, R.color.text_muted), PorterDuff.Mode.SRC_IN);
        }
        if (bookingsIcon != null) {
            bookingsIcon.setColorFilter(ContextCompat.getColor(activity, R.color.text_muted), PorterDuff.Mode.SRC_IN);
        }
        if (chatIcon != null) {
            chatIcon.setColorFilter(ContextCompat.getColor(activity, R.color.sapphire_primary), PorterDuff.Mode.SRC_IN);
            View chatActiveBg = root.findViewById(R.id.nav_chat_active_bg);
            if (chatActiveBg != null) {
                chatActiveBg.setVisibility(View.VISIBLE);
            }
        }
        if (profileIcon != null) {
            profileIcon.setColorFilter(ContextCompat.getColor(activity, R.color.text_muted), PorterDuff.Mode.SRC_IN);
        }
    }
}
