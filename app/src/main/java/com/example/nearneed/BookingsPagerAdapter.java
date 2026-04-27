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
        String tab;
        switch (position) {
            case 0:
                tab = "upcoming";
                break;
            case 1:
                tab = "ongoing";
                break;
            case 2:
                tab = "past";
                break;
            default:
                tab = "upcoming";
                break;
        }
        return BookingsRealtimeFragment.newInstance(userRole, tab, filterType);
    }

    @Override
    public int getItemCount() {
        return 3; // Upcoming, Ongoing, Past
    }
}
