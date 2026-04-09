package com.example.nearneed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class SeekerNavbarController {

    private SeekerNavbarController() {
    }

    public static void bind(@NonNull Activity activity, @NonNull View root, boolean homeActive) {
        FrameLayout homeContainer = root.findViewById(R.id.nav_home_container);
        FrameLayout profileContainer = root.findViewById(R.id.nav_profile_container);
        FrameLayout chatContainer = root.findViewById(R.id.nav_chat_container);

        View homeActiveBg = root.findViewById(R.id.nav_home_active_bg);
        View profileActiveBg = root.findViewById(R.id.nav_profile_active_bg);

        ImageView homeIcon = root.findViewById(R.id.nav_home_icon);
        ImageView profileIcon = root.findViewById(R.id.nav_profile_icon);

        if (homeContainer != null) {
            homeContainer.setOnClickListener(v -> {
                if (!(activity instanceof HomeSeekerActivity) && !(activity instanceof HomeSeekerNoPostsActivity)) {
                    Intent intent = new Intent(activity, HomeSeekerActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    activity.finish();
                }
            });
        }

        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    RoleManager.setRole(activity, RoleManager.ROLE_SEEKER);
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    activity.finish();
                }
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

        if (homeActiveBg != null) {
            homeActiveBg.setVisibility(homeActive ? View.VISIBLE : View.GONE);
        }
        if (profileActiveBg != null) {
            profileActiveBg.setVisibility(homeActive ? View.GONE : View.VISIBLE);
        }

        if (homeIcon != null) {
            int homeColor = ContextCompat.getColor(activity, homeActive ? R.color.sapphire_primary : R.color.text_muted);
            homeIcon.setColorFilter(homeColor, PorterDuff.Mode.SRC_IN);
        }
        if (profileIcon != null) {
            int profileColor = ContextCompat.getColor(activity, homeActive ? R.color.text_muted : R.color.sapphire_primary);
            profileIcon.setColorFilter(profileColor, PorterDuff.Mode.SRC_IN);
        }
    }
}
