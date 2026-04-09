package com.example.nearneed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public final class ProfileModeSwitcher {

    private ProfileModeSwitcher() {
    }

    public static void bind(@NonNull Activity activity, @NonNull View root, @NonNull String currentRole) {
        TextView tabSeeker = root.findViewById(R.id.tab_seeker);
        TextView tabProvider = root.findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setOnClickListener(v -> switchRole(activity, RoleManager.ROLE_SEEKER));
        }

        if (tabProvider != null) {
            tabProvider.setOnClickListener(v -> switchRole(activity, RoleManager.ROLE_PROVIDER));
        }

        updateTabs(root, currentRole);
    }

    private static void switchRole(@NonNull Activity activity, @NonNull String role) {
        if (role.equals(RoleManager.getRole(activity))) {
            return;
        }

        RoleManager.setRole(activity, role);
        Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        activity.finish();
    }

    public static void updateTabs(@NonNull View root, @NonNull String currentRole) {
        TextView tabSeeker = root.findViewById(R.id.tab_seeker);
        TextView tabProvider = root.findViewById(R.id.tab_provider);

        boolean seekerActive = RoleManager.ROLE_SEEKER.equals(currentRole);

        if (tabSeeker != null) {
            if (seekerActive) {
                tabSeeker.setBackgroundResource(R.drawable.bg_seeker_tab_active);
                tabSeeker.setTextColor(ContextCompat.getColor(root.getContext(), R.color.brand_primary));
            } else {
                tabSeeker.setBackground(null);
                tabSeeker.setTextColor(ContextCompat.getColor(root.getContext(), R.color.text_muted));
            }
        }

        if (tabProvider != null) {
            if (!seekerActive) {
                tabProvider.setBackgroundResource(R.drawable.bg_seeker_tab_active);
                tabProvider.setTextColor(ContextCompat.getColor(root.getContext(), R.color.brand_primary));
            } else {
                tabProvider.setBackground(null);
                tabProvider.setTextColor(ContextCompat.getColor(root.getContext(), R.color.text_muted));
            }
        }
    }
}
