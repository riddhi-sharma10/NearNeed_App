package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarProviderActivity extends AppCompatActivity {

    private BookingViewModel bookingViewModel;
    private ProviderScheduleAdapter scheduleAdapter;
    private CalendarDateAdapter dateAdapter;
    private RecyclerView rvCalendarDates, rvCalendarSchedule;
    private TextView tvMonthYear;
    private Date selectedDate;
    private View emptyStateContainer;
    private List<Booking> allBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_calendar_provider);

        initViews();
        setupCalendar();
        setupViewModel();
    }

    private void initViews() {
        rvCalendarDates = findViewById(R.id.rvCalendarDates);
        rvCalendarSchedule = findViewById(R.id.rvCalendarSchedule);
        tvMonthYear = findViewById(R.id.tvMonthYear); 
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddScheduleTop).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        // Bind the unified navbar – Bookings tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_BOOKINGS);
    }

    private void setupCalendar() {
        List<Date> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        selectedDate = cal.getTime(); // Default to today

        for (int i = 0; i < 30; i++) {
            dates.add(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        dateAdapter = new CalendarDateAdapter(dates, date -> {
            selectedDate = date;
            updateMonthYearDisplay(date);
            filterAndDisplayBookings();
        });

        rvCalendarDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCalendarDates.setAdapter(dateAdapter);

        updateMonthYearDisplay(selectedDate);

        scheduleAdapter = new ProviderScheduleAdapter();
        rvCalendarSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvCalendarSchedule.setAdapter(scheduleAdapter);
    }

    private void updateMonthYearDisplay(Date date) {
        if (tvMonthYear != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            tvMonthYear.setText(sdf.format(date));
        }
    }

    private void setupViewModel() {
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        bookingViewModel.observeUserBookings();

        bookingViewModel.getUserBookings().observe(this, bookings -> {
            this.allBookings = bookings;
            filterAndDisplayBookings();
        });
    }

    private void filterAndDisplayBookings() {
        if (allBookings == null) return;

        String currentUid = FirebaseAuth.getInstance().getUid();
        List<Booking> filtered = new ArrayList<>();
        
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);

        for (Booking b : allBookings) {
            if (currentUid != null && currentUid.equals(b.providerId)) {
                long scheduledTime = b.scheduledDate != null ? b.scheduledDate : (b.timestamp != null ? b.timestamp : 0L);
                if (scheduledTime == 0) continue;

                Calendar bookingCal = Calendar.getInstance();
                bookingCal.setTimeInMillis(scheduledTime);

                if (selectedCal.get(Calendar.YEAR) == bookingCal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.DAY_OF_YEAR) == bookingCal.get(Calendar.DAY_OF_YEAR)) {
                    filtered.add(b);
                }
            }
        }
        
        java.util.Collections.sort(filtered, (b1, b2) -> {
            long t1 = b1.scheduledDate != null ? b1.scheduledDate : (b1.timestamp != null ? b1.timestamp : 0L);
            long t2 = b2.scheduledDate != null ? b2.scheduledDate : (b2.timestamp != null ? b2.timestamp : 0L);
            return Long.compare(t1, t2);
        });

        scheduleAdapter.setBookings(filtered);
        
        if (filtered.isEmpty()) {
            rvCalendarSchedule.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            rvCalendarSchedule.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }
}
