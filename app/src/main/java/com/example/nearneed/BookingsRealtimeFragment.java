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
    private PostViewModel postViewModel;
    private ApplicationViewModel applicationViewModel;

    private String role;
    private String tab;
    private String filterType;

    private List<Booking> currentBookings = new ArrayList<>();
    private List<Post> currentPosts = new ArrayList<>();
    private List<Application> currentApplications = new ArrayList<>();

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
                // Handled in adapter
            }

            @Override
            public void onMessage(Booking booking) {
                if (getActivity() == null) return;
                Intent intent = new Intent(getActivity(), ChatActivity.class);
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
                intent.putExtra("SEEKER_ID", booking.seekerId);
                intent.putExtra("PROVIDER_ID", booking.providerId);
                startActivity(intent);
            }

            @Override
            public void onCancel(Booking booking) {
            }
        });
        rvBookings.setAdapter(adapter);

        setupViewModels();
    }

    private void setupViewModels() {
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        bookingViewModel.getUserBookings().observe(getViewLifecycleOwner(), bookings -> {
            currentBookings = bookings != null ? bookings : new ArrayList<>();
            updateUI();
        });
        bookingViewModel.observeUserBookings();

        if (RoleManager.ROLE_SEEKER.equals(role)) {
            postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            if (userId != null && getContext() != null) {
                postViewModel.getUserPosts().observe(getViewLifecycleOwner(), posts -> {
                    currentPosts = posts != null ? posts : new ArrayList<>();
                    updateUI();
                });
                postViewModel.observeUserPosts(getContext(), userId);
            }
        } else if (RoleManager.ROLE_PROVIDER.equals(role)) {
            applicationViewModel = new ViewModelProvider(this).get(ApplicationViewModel.class);
            applicationViewModel.getUserApplications().observe(getViewLifecycleOwner(), applications -> {
                currentApplications = applications != null ? applications : new ArrayList<>();
                updateUI();
            });
            applicationViewModel.observeUserApplications();
        }
    }

    private void updateUI() {
        List<BookingsAdapter.BookingWrapper> items = new ArrayList<>();

        // Add Bookings
        for (Booking b : currentBookings) {
            if (matchesFilterType(b) && matchesTab(b)) {
                items.add(new BookingsAdapter.BookingWrapper(b));
            }
        }

        // Add Posts (Seeker only)
        if (RoleManager.ROLE_SEEKER.equals(role)) {
            for (Post p : currentPosts) {
                if (matchesFilterType(p) && matchesTab(p)) {
                    // Avoid duplicate if booking already exists for this post
                    boolean exists = false;
                    for (Booking b : currentBookings) {
                        if (p.postId != null && p.postId.equals(b.postId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        items.add(new BookingsAdapter.BookingWrapper(p));
                    }
                }
            }
        }

        // Add Applications (Provider only)
        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            for (Application a : currentApplications) {
                if (matchesFilterType(a) && matchesTab(a)) {
                    // Avoid duplicate if booking already exists for this application
                    boolean exists = false;
                    for (Booking b : currentBookings) {
                        if (a.applicationId != null && a.applicationId.equals(b.applicationId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        items.add(new BookingsAdapter.BookingWrapper(a));
                    }
                }
            }
        }

        // Sort by timestamp descending
        java.util.Collections.sort(items, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));

        adapter.setItems(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesFilterType(Object obj) {
        if (filterType == null || filterType.trim().isEmpty()) {
            return true;
        }
        String normalizedFilter = filterType.trim().toLowerCase(Locale.ROOT);
        String postType = "";

        if (obj instanceof Booking) {
            postType = ((Booking) obj).postType != null ? ((Booking) obj).postType.toLowerCase(Locale.ROOT) : "";
        } else if (obj instanceof Post) {
            postType = ((Post) obj).type != null ? ((Post) obj).type.toLowerCase(Locale.ROOT) : "";
        } else if (obj instanceof Application) {
            postType = ((Application) obj).postType != null ? ((Application) obj).postType.toLowerCase(Locale.ROOT) : "";
        }

        if ("community".equals(normalizedFilter)) {
            return "community".equals(postType);
        }
        if ("gigs".equals(normalizedFilter) || "gig".equals(normalizedFilter)) {
            return "gig".equals(postType);
        }
        return true;
    }

    private boolean matchesTab(Object obj) {
        String status = "upcoming";
        if (obj instanceof Booking) {
            status = ((Booking) obj).status != null ? ((Booking) obj).status.trim().toLowerCase(Locale.ROOT) : "upcoming";
        } else if (obj instanceof Post) {
            status = "upcoming"; // Open posts are always upcoming in seeker view
        } else if (obj instanceof Application) {
            status = "upcoming"; // Pending applications are always upcoming in provider view
        }

        switch (tab) {
            case "upcoming":
                return "upcoming".equals(status) || "pending".equals(status) || "active".equals(status);
            case "ongoing":
                return "ongoing".equals(status) || "in_progress".equals(status) || "confirmed".equals(status);
            case "past":
                return "completed".equals(status) || "cancelled".equals(status) || "canceled".equals(status);
            default:
                return true;
        }
    }
}
