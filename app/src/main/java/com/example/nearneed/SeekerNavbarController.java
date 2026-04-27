package com.example.nearneed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

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
                if (activity instanceof MessagesActivity) return;
                
                String role = RoleManager.getRole(activity);
                if (RoleManager.ROLE_SEEKER.equals(role)) {
                    openAcceptedProviderChat(activity);
                } else {
                    // Providers and others go to full list
                    Intent intent = new Intent(activity, MessagesActivity.class);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

        // Real-time Chat Badge Sync
        View chatBadge = root.findViewById(R.id.nav_chat_badge);
        if (chatBadge != null) {
            NotificationCenter.listenChatUnreadCount(count -> {
                activity.runOnUiThread(() -> {
                    if (activity.isFinishing()) return;
                    if (count > 0) {
                        chatBadge.setVisibility(View.VISIBLE);
                    } else {
                        chatBadge.setVisibility(View.GONE);
                    }
                });
            });
        }
    }

    /**
     * Logic for Seeker to open direct chat with accepted provider or fall back to list
     */
    private static void openAcceptedProviderChat(Activity activity) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("seekerId", currentUid)
                .whereIn("status", Arrays.asList("upcoming", "in_progress", "confirmed"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty() && activity instanceof AppCompatActivity) {
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        String providerId = doc.getString("providerId");
                        String providerName = doc.getString("providerName");
                        
                        if (providerId != null) {
                            ChatBottomSheet.newInstance(providerId, providerName != null ? providerName : "Provider")
                                    .show(((AppCompatActivity)activity).getSupportFragmentManager(), "ChatBottomSheet");
                        } else {
                            activity.startActivity(new Intent(activity, MessagesActivity.class));
                        }
                    } else {
                        activity.startActivity(new Intent(activity, MessagesActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    activity.startActivity(new Intent(activity, MessagesActivity.class));
                });
    }

    // Legacy method for backward compatibility
    public static void bind(@NonNull Activity activity, @NonNull View root, boolean homeActive) {
        bind(activity, root, homeActive ? TAB_HOME : TAB_PROFILE);
    }
}
