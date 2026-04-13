package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BookingsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BookingsPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        // Read user role from SharedPreferences via RoleManager
        String currentRole = RoleManager.getRole(requireContext());

        // Initialize and set adapter
        adapter = new BookingsPagerAdapter(this, currentRole);
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Upcoming");
                    break;
                case 1:
                    tab.setText("Ongoing");
                    break;
                case 2:
                    tab.setText("Past");
                    break;
            }
        }).attach();
        
        // Select "Upcoming" by default (position 0)
        viewPager.setCurrentItem(0, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Check if role has changed while in background/profile fragment
        String newRole = RoleManager.getRole(requireContext());
        if (adapter != null && !adapter.getUserRole().equals(newRole)) {
            // Role changed, recreate adapter
            adapter = new BookingsPagerAdapter(this, newRole);
            viewPager.setAdapter(adapter);
            setupViewPager();
        }
    }
}
