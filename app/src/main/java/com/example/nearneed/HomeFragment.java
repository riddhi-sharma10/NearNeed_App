package com.example.nearneed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * HomeFragment – single, role-aware home screen.
 *
 * Layout selection:
 *   PROVIDER  → activity_home_provider.xml
 *   SEEKER (hasPosts)   → activity_home_seeker.xml
 *   SEEKER (!hasPosts)  → activity_home_seeker_no_posts.xml
 *
 * No XML files are renamed or modified; they are inflated dynamically.
 */
public class HomeFragment extends Fragment {

    // ──────────────────────────────────────────────────────────────────────────
    // State
    // ──────────────────────────────────────────────────────────────────────────

    private String currentRole;

    /**
     * Determines whether the current seeker has any active posts.
     *
     * Replace this stub with a real backend/local DB call when ready.
     * Set to {@code false} to show the empty-state layout.
     */
    private boolean hasPosts = true; // ← toggle for demo / connect to real data

    /**
     * Switches role and navigates to appropriate home screen.
     * Reuses ProfileModeSwitcher logic.
     */
    private void switchRole(String newRole) {
        if (newRole.equals(RoleManager.getRole(requireContext()))) {
            return; // Already in this role
        }

        // Persist choice
        RoleManager.setRole(requireContext(), newRole);

        // Show toast notification
        String msg = RoleManager.ROLE_SEEKER.equals(newRole)
                ? "Switched to Seeker mode"
                : "Switched to Provider mode";
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity (dispatcher)
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 1. Determine current role
        currentRole = RoleManager.getRole(requireContext());

        // 2. Pick the correct layout
        int layoutId = resolveLayout();

        // 3. Inflate it (no setContentView – this is a Fragment)
        View view = inflater.inflate(layoutId, container, false);

        // 4. Wire up UI logic from the original Activity classes
        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            setupProviderUI(view);
        } else {
            setupSeekerUI(view);
        }

        return view;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Layout resolution
    // ──────────────────────────────────────────────────────────────────────────

    private int resolveLayout() {
        if (RoleManager.ROLE_PROVIDER.equals(currentRole)) {
            return R.layout.activity_home_provider;
        }
        // Seeker: always use the single layout with empty state visibility toggle
        return R.layout.activity_home_seeker;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Provider UI  (migrated from HomeProviderActivity)
    // ──────────────────────────────────────────────────────────────────────────

    private void setupProviderUI(View view) {
        // Calendar / Schedule navigation
        View viewCalendar = view.findViewById(R.id.viewCalendar);
        if (viewCalendar != null) {
            viewCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CalendarProviderActivity.class);
                startActivity(intent);
            });
        }

        // Community request posting
        View btnCommunityPost = view.findViewById(R.id.btnPostCommunityRequest);
        if (btnCommunityPost != null) {
            btnCommunityPost.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), CommunityPostActivity.class);
                startActivity(intent);
            });
        }

        // Location picker
        View locationSection = view.findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v ->
                LocationPickerHelper.show(requireActivity(), displayText -> {
                    android.widget.TextView tvDelivery = view.findViewById(R.id.tvDeliveryLocation);
                    if (tvDelivery != null) {
                        tvDelivery.setText(displayText);
                        // Also save to SharedPreferences
                        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("LocationPrefs", android.content.Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("delivery_location", displayText);
                        editor.apply();
                    }
                }));
        }

        // Load saved location into header
        android.widget.TextView tvDelivery = view.findViewById(R.id.tvDeliveryLocation);
        if (tvDelivery != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("LocationPrefs", android.content.Context.MODE_PRIVATE);
            String saved = prefs.getString("delivery_location", null);
            if (saved != null) {
                tvDelivery.setText(saved);
            }
        }

        // Earnings card → My Earnings
        View earningsCard = view.findViewById(R.id.earningsCard);
        if (earningsCard != null) {
            earningsCard.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), MyEarningsActivity.class);
                startActivity(intent);
            });
        }

        // View All Requests → Map Activity
        View viewAllRequestsContainer = view.findViewById(R.id.viewAllRequestsContainer);
        if (viewAllRequestsContainer != null) {
            viewAllRequestsContainer.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), MapsActivity.class);
                startActivity(intent);
            });
        }

        // Setup role toggle
        setupRoleToggle(view);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Seeker UI  (migrated from HomeSeekerActivity + HomeSeekerNoPostsActivity)
    // ──────────────────────────────────────────────────────────────────────────

    private void setupSeekerUI(View view) {
        // Toggle empty state visibility based on posts
        View postsContainer = view.findViewById(R.id.posts_content_container);
        View emptyState = view.findViewById(R.id.empty_state_container);

        if (postsContainer != null) {
            postsContainer.setVisibility(hasPosts ? View.VISIBLE : View.GONE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(hasPosts ? View.GONE : View.VISIBLE);
        }

        // Wire "Post Now" button in empty state
        View btnPostNowEmpty = view.findViewById(R.id.btn_post_now_empty);
        if (btnPostNowEmpty != null) {
            btnPostNowEmpty.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PostOptionsActivity.class);
                startActivity(intent);
            });
        }

        // FAB / Add button – "Post a New Request" (present in both cases)
        View btnAdd = view.findViewById(R.id.fab_add_seeker);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PostOptionsActivity.class);
                startActivity(intent);
            });
        }

        // Location picker
        View locationSection = view.findViewById(R.id.locationSection);
        if (locationSection != null) {
            locationSection.setOnClickListener(v ->
                LocationPickerHelper.show(requireActivity(), displayText -> {
                    TextView tvDelivery = view.findViewById(R.id.tvDeliveryLocation);
                    if (tvDelivery != null) {
                        tvDelivery.setText(displayText);
                        // Also save to SharedPreferences
                        SharedPreferences prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("delivery_location", displayText);
                        editor.apply();
                    }
                }));
        }

        // Load saved location into header
        TextView tvDelivery = view.findViewById(R.id.tvDeliveryLocation);
        if (tvDelivery != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE);
            String saved = prefs.getString("delivery_location", null);
            if (saved != null) {
                tvDelivery.setText(saved);
            }
        }

        // Setup role toggle
        setupRoleToggle(view);
    }

    /**
     * Sets up the role toggle (Seeker/Provider) in the home screen.
     * Initializes click listeners and visual state.
     */
    private void setupRoleToggle(View view) {
        TextView tabSeeker = view.findViewById(R.id.tab_seeker);
        TextView tabProvider = view.findViewById(R.id.tab_provider);

        if (tabSeeker != null) {
            tabSeeker.setOnClickListener(v -> switchRole(RoleManager.ROLE_SEEKER));
        }

        if (tabProvider != null) {
            tabProvider.setOnClickListener(v -> switchRole(RoleManager.ROLE_PROVIDER));
        }

        // Update visual state
        updateToggleAppearance(view);
    }

    /**
     * Updates the visual state of the toggle to reflect current role.
     */
    private void updateToggleAppearance(View view) {
        TextView tabSeeker = view.findViewById(R.id.tab_seeker);
        TextView tabProvider = view.findViewById(R.id.tab_provider);
        boolean seekerActive = RoleManager.ROLE_SEEKER.equals(currentRole);

        if (tabSeeker != null) {
            if (seekerActive) {
                tabSeeker.setBackgroundResource(R.drawable.bg_seeker_tab_active);
                tabSeeker.setTextColor(ContextCompat.getColor(view.getContext(), R.color.brand_primary));
            } else {
                tabSeeker.setBackground(null);
                tabSeeker.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_muted));
            }
        }

        if (tabProvider != null) {
            if (!seekerActive) {
                tabProvider.setBackgroundResource(R.drawable.bg_seeker_tab_active);
                tabProvider.setTextColor(ContextCompat.getColor(view.getContext(), R.color.brand_primary));
            } else {
                tabProvider.setBackground(null);
                tabProvider.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_muted));
            }
        }
    }
}
