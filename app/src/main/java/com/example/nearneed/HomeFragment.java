package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    }
}
