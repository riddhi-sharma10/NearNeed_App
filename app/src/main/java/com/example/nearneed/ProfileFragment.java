package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nearneed.databinding.FragmentProfileBinding;
import com.example.nearneed.databinding.LayoutProfileProviderBinding;
import com.example.nearneed.databinding.LayoutProfileSeekerBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initial State Initialization
        String currentRole = RoleManager.getRole(requireContext());

        // 2. Load initial view based on role
        loadViewForRole(currentRole, false);

        // 3. Setup Navbar Highlight
        setupNavbar();
    }

    private void loadViewForRole(String role, boolean animate) {
        binding.profileContentContainer.removeAllViews();
        View contentView;

        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            LayoutProfileProviderBinding providerBinding = LayoutProfileProviderBinding.inflate(getLayoutInflater(), binding.profileContentContainer, false);
            contentView = providerBinding.getRoot();
            setupProviderInteractions(providerBinding);
        } else {
            LayoutProfileSeekerBinding seekerBinding = LayoutProfileSeekerBinding.inflate(getLayoutInflater(), binding.profileContentContainer, false);
            contentView = seekerBinding.getRoot();
            setupSeekerInteractions(seekerBinding);
        }

        if (animate) {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(150);
            
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(150);
            fadeIn.setStartOffset(150);

            binding.profileContentContainer.startAnimation(fadeOut);
            contentView.startAnimation(fadeIn);
        }

        binding.profileContentContainer.addView(contentView);

        SeekerNavbarController.bind(requireActivity(), contentView, false);
        ProfileModeSwitcher.bind(requireActivity(), contentView, role);
        
        // Setup Navbar highlight on the parent navbar
        setupNavbar();
    }

    private void highlightProfileInNavbar(View root) {
        // Obsolete - functionality merged into setupNavbar
    }

    private void setupSeekerInteractions(LayoutProfileSeekerBinding seekerBinding) {
        // Edit Profile
        if (seekerBinding.btnEditProfile != null) {
            seekerBinding.btnEditProfile.setOnClickListener(v -> {
                Snackbar.make(requireView(), "Navigating to Edit Profile...", Snackbar.LENGTH_SHORT)
                        .setAction("DISMISS", view -> {})
                        .show();
            });
        }

        // My Posts
        if (seekerBinding.menuMyPosts != null) {
            seekerBinding.menuMyPosts.setOnClickListener(v -> {
                Snackbar.make(requireView(), "Loading Your Active Requests...", Snackbar.LENGTH_SHORT).show();
            });
        }

        // Settings
        if (seekerBinding.menuSettings != null) {
            seekerBinding.menuSettings.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Settings")
                        .setMessage("Advanced app preferences and account settings will be available in the next major update.")
                        .setPositiveButton("Got it", null)
                        .setIcon(android.R.drawable.ic_menu_preferences)
                        .show();
            });
        }

        // Help & Support
        if (seekerBinding.menuHelp != null) {
            seekerBinding.menuHelp.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Need Help?")
                        .setMessage("Our support team is available 24/7. Would you like to view the FAQ or contact support directly?")
                        .setPositiveButton("Contact Support", (dialog, which) -> {
                            Snackbar.make(requireView(), "Opening Support Chat...", Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("FAQ", null)
                        .show();
            });
        }

        // Logout
        if (seekerBinding.menuLogout != null) {
            seekerBinding.menuLogout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Log Out Securely")
                        .setMessage("Are you sure you want to log out of your NearNeed account? You will need to verify your phone number to log back in.")
                        .setPositiveButton("Log Out", (dialog, which) -> {
                            Snackbar.make(requireView(), "Logging out...", Snackbar.LENGTH_LONG).show();
                            // Implementation for triggering global logout 
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void setupProviderInteractions(LayoutProfileProviderBinding providerBinding) {
        // Edit Profile
        if (providerBinding.btnEditProfileProvider != null) {
            providerBinding.btnEditProfileProvider.setOnClickListener(v -> {
                Snackbar.make(requireView(), "Opening Provider Profile Editor...", Snackbar.LENGTH_SHORT).show();
            });
        }

        // My Jobs
        if (providerBinding.menuMyJobs != null) {
            providerBinding.menuMyJobs.setOnClickListener(v -> {
                Snackbar.make(requireView(), "Loading Your Active Jobs...", Snackbar.LENGTH_SHORT).show();
            });
        }

        // Logout
        if (providerBinding.menuLogoutProvider != null) {
            providerBinding.menuLogoutProvider.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                        .setTitle("Logout Requested")
                        .setMessage("Securely signing out will end your current session. Do you wish to continue?")
                        .setPositiveButton("Logout", (dialog, which) -> switchRole(RoleManager.ROLE_SEEKER)) // Just for demo
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void switchRole(String newRole) {
        // Update global role
        RoleManager.setRole(requireContext(), newRole);
        
        // Smooth UI update locally just in case
        loadViewForRole(newRole, true);

        // Restart MainActivity to refresh entire app with smooth transition
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Use fade animation for activity transition
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupNavbar() {
        if (binding == null) return;
        
        // The navbar is included in fragment_profile.xml
        View navbar = binding.getRoot().findViewById(R.id.included_navbar);
        if (navbar == null) return;

        // 1. Highlight Profile (Sapphire Blue)
        FrameLayout profileContainer = navbar.findViewById(R.id.nav_profile_container);
        if (profileContainer != null && profileContainer.getChildCount() > 0) {
            com.google.android.material.card.MaterialCardView profileCard = (com.google.android.material.card.MaterialCardView) profileContainer.getChildAt(0);
            profileCard.setCardBackgroundColor(android.graphics.Color.parseColor("#DBEAFE")); // brand_primary_light
            if (profileCard.getChildCount() > 0) {
                android.widget.ImageView profileIcon = (android.widget.ImageView) profileCard.getChildAt(0);
                profileIcon.setColorFilter(android.graphics.Color.parseColor("#1E3A8A")); // sapphire_primary
            }
        }

        // 2. Un-highlight Home (Muted Grey)
        FrameLayout homeContainer = navbar.findViewById(R.id.nav_home_container);
        if (homeContainer != null && homeContainer.getChildCount() > 0) {
            homeContainer.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), HomeSeekerActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                requireActivity().finish();
            });
            com.google.android.material.card.MaterialCardView homeCard = (com.google.android.material.card.MaterialCardView) homeContainer.getChildAt(0);
            homeCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
            if (homeCard.getChildCount() > 0) {
                android.widget.ImageView homeIcon = (android.widget.ImageView) homeCard.getChildAt(0);
                homeIcon.setColorFilter(android.graphics.Color.parseColor("#9CA3AF")); // text_muted
            }
        }

        FrameLayout chatContainer = navbar.findViewById(R.id.nav_chat_container);
        if (chatContainer != null) {
            chatContainer.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), MessagesActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                requireActivity().finish();
            });
        }

        FrameLayout profileContainerClick = navbar.findViewById(R.id.nav_profile_container);
        if (profileContainerClick != null) {
            profileContainerClick.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ProfileActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                requireActivity().finish();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
