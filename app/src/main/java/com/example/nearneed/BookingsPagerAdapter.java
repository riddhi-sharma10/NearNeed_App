package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BookingsPagerAdapter extends FragmentStateAdapter {

    private final String userRole;

    public BookingsPagerAdapter(@NonNull Fragment fragment, String userRole) {
        super(fragment);
        this.userRole = userRole;
    }

    public String getUserRole() {
        return userRole;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (RoleManager.ROLE_PROVIDER.equals(userRole)) {
            switch (position) {
                case 0: return new ProviderUpcomingFragment();
                case 1: return new ProviderOngoingFragment();
                case 2: return new ProviderPastFragment();
                default: return new ProviderUpcomingFragment();
            }
        } else {
            // Default to Seeker
            switch (position) {
                case 0: return new SeekerUpcomingFragment();
                case 1: return new SeekerOngoingFragment();
                case 2: return new SeekerPastFragment();
                default: return new SeekerUpcomingFragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Upcoming, Ongoing, Past
    }
}
