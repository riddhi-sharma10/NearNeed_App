package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingsRealtimeFragment extends Fragment {

    private static final String ARG_ROLE = "arg_role";
    private static final String ARG_TAB = "arg_tab";
    private static final String ARG_FILTER_TYPE = "arg_filter_type";

    private RecyclerView rvBookings;
    private TextView tvEmpty;
    private BookingsAdapter adapter;
    private BookingViewModel bookingViewModel;

    private String role;
    private String tab;
    private String filterType;

    public static BookingsRealtimeFragment newInstance(String role, String tab, @Nullable String filterType) {
        BookingsRealtimeFragment fragment = new BookingsRealtimeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        args.putString(ARG_TAB, tab);
        if (filterType != null) {
            args.putString(ARG_FILTER_TYPE, filterType);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings_realtime, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        role = args != null ? args.getString(ARG_ROLE, RoleManager.ROLE_SEEKER) : RoleManager.ROLE_SEEKER;
        tab = args != null ? args.getString(ARG_TAB, "upcoming") : "upcoming";
        filterType = args != null ? args.getString(ARG_FILTER_TYPE) : null;

        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingsAdapter(role, new BookingsAdapter.OnBookingActionListener() {
            @Override
            public void onUpdateStatus(Booking booking) {
                // Adapter handles navigation to UpdateStatusActivity itself.
            }

            @Override
            public void onMessage(Booking booking) {
                Intent intent = new Intent(requireActivity(), ChatActivity.class);
                String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                
                String otherUserId;
                String otherUserName;
                
                if (currentUserId != null && currentUserId.equals(booking.seekerId)) {
                    otherUserId = booking.providerId;
                    otherUserName = booking.providerName;
                } else {
                    otherUserId = booking.seekerId;
                    otherUserName = booking.seekerName;
                }
                
                intent.putExtra("CHAT_NAME", otherUserName != null ? otherUserName : "NearNeed User");
                intent.putExtra("CHAT_USER_ID", otherUserId);
                startActivity(intent);
            }

            @Override
            public void onCancel(Booking booking) {
                // Not used in current card UI.
            }
        });
        rvBookings.setAdapter(adapter);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        bookingViewModel.getUserBookings().observe(getViewLifecycleOwner(), bookings -> {
            List<Booking> filtered = filterBookings(bookings != null ? bookings : new ArrayList<>());
            adapter.setBookings(filtered);
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        });

        bookingViewModel.observeUserBookings();
    }

    private List<Booking> filterBookings(List<Booking> source) {
        List<Booking> filtered = new ArrayList<>();
        for (Booking booking : source) {
            if (!matchesFilterType(booking)) {
                continue;
            }
            if (!matchesTab(booking)) {
                continue;
            }
            filtered.add(booking);
        }
        return filtered;
    }

    private boolean matchesFilterType(Booking booking) {
        if (filterType == null || filterType.trim().isEmpty()) {
            return true;
        }
        String normalizedFilter = filterType.trim().toLowerCase(Locale.ROOT);
        String postType = booking.postType != null ? booking.postType.toLowerCase(Locale.ROOT) : "";

        if ("community".equals(normalizedFilter)) {
            return "community".equals(postType);
        }
        if ("gigs".equals(normalizedFilter) || "gig".equals(normalizedFilter)) {
            return "gig".equals(postType);
        }
        return true;
    }

    private boolean matchesTab(Booking booking) {
        String status = booking.status != null ? booking.status.trim().toLowerCase(Locale.ROOT) : "upcoming";
        boolean isPastStatus = "completed".equals(status) || "cancelled".equals(status) || "canceled".equals(status);
        
        long scheduledTime = booking.scheduledDate != null ? booking.scheduledDate : 0L;
        
        // Handle cases with no date (default to upcoming if not status-past)
        if (scheduledTime == 0) {
            if ("past".equals(tab)) return isPastStatus;
            if (isPastStatus) return false;
            return "upcoming".equals(tab);
        }

        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar scheduled = java.util.Calendar.getInstance();
        scheduled.setTimeInMillis(scheduledTime);

        boolean isToday = now.get(java.util.Calendar.YEAR) == scheduled.get(java.util.Calendar.YEAR) &&
                          now.get(java.util.Calendar.DAY_OF_YEAR) == scheduled.get(java.util.Calendar.DAY_OF_YEAR);
        
        boolean isFuture = scheduled.after(now) && !isToday;
        boolean isBeforeToday = scheduled.before(now) && !isToday;

        if ("past".equals(tab)) {
            return isPastStatus || isBeforeToday;
        }
        
        // For Ongoing and Upcoming, status must NOT be past
        if (isPastStatus) return false;

        if ("ongoing".equals(tab)) {
            return isToday;
        }
        
        if ("upcoming".equals(tab)) {
            return isFuture;
        }

        return true;
    }
}
