package com.example.nearneed;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BookingsPagerAdapter extends FragmentStateAdapter {

    private final String userRole;
    private String filterType; // "gigs", "community", or null for all

    public BookingsPagerAdapter(@NonNull Fragment fragment, String userRole) {
        super(fragment);
        this.userRole = userRole;
        this.filterType = null;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return filterType;
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
            // Default to Seeker - pass filter type to fragments
            Fragment fragment;
            switch (position) {
                case 0: fragment = new SeekerUpcomingFragment(); break;
                case 1: fragment = new SeekerOngoingFragment(); break;
                case 2: fragment = new SeekerPastFragment(); break;
                default: fragment = new SeekerUpcomingFragment();
            }

            // Pass filter type to fragment
            if (filterType != null) {
                Bundle args = new Bundle();
                args.putString("filter_type", filterType);
                fragment.setArguments(args);
            }

            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Upcoming, Ongoing, Past
    }
}
