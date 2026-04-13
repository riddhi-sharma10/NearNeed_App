package com.example.nearneed;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * ProfileModeSwitcher handles the Seeker / Provider toggle in the Profile screen.
 *
 * When the user taps the tab, it:
 *   1. Persists the new role via {@link RoleManager}.
 *   2. Routes through {@link MainActivity} (the role dispatcher) which clears the
 *      entire back stack and launches the correct Home screen for the new role.
 *
 * This means the whole app flow changes immediately and persistently – the user
 * won't accidentally be shown the wrong home screen until they toggle again.
 */
public final class ProfileModeSwitcher {

    private ProfileModeSwitcher() {
    }

    public static void bind(@NonNull Activity activity, @NonNull View root, @NonNull String currentRole) {
        TextView tabSeeker   = root.findViewById(R.id.tab_seeker);
        TextView tabProvider = root.findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setOnClickListener(v -> switchRole(activity, RoleManager.ROLE_SEEKER));
        }
        if (tabProvider != null) {
            tabProvider.setOnClickListener(v -> switchRole(activity, RoleManager.ROLE_PROVIDER));
        }

        updateTabs(root, currentRole);
    }

    /**
     * Persists the new role then hands off to MainActivity (the dispatcher).
     * FLAG_ACTIVITY_CLEAR_TASK ensures no stale activities remain in the back stack.
     */
    private static void switchRole(@NonNull Activity activity, @NonNull String newRole) {
        if (newRole.equals(RoleManager.getRole(activity))) {
            return; // Already in this role – nothing to do
        }

        // Persist choice
        RoleManager.setRole(activity, newRole);

        // Show toast notification
        String msg = RoleManager.ROLE_SEEKER.equals(newRole)
                ? "Switched to Seeker mode"
                : "Switched to Provider mode";
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

        // Dispatch: MainActivity will land the user on the correct home screen
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // finish() is not needed – CLEAR_TASK takes care of it
    }

    /**
     * Updates the visual state of the Seeker / Provider tabs to match {@code currentRole}.
     */
    public static void updateTabs(@NonNull View root, @NonNull String currentRole) {
        TextView tabSeeker   = root.findViewById(R.id.tab_seeker);
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
