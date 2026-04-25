package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CalendarProviderActivity extends AppCompatActivity {

    private static final String[] MONTHS = {
            "January 2024", "February 2024", "March 2024", "April 2024",
            "May 2024", "June 2024", "July 2024", "August 2024",
            "September 2024", "October 2024", "November 2024", "December 2024"
    };

    private static final int COLOR_ACTIVE = 0xFF1E3A8A;
    private static final int COLOR_INACTIVE = 0xFF94A3B8;
    private static final int COLOR_PRIMARY_TEXT = 0xFF111827;
    private static final float SCALE_SELECTED = 1.08f;
    private static final float SCALE_UNSELECTED = 0.98f;

    private int currentMonthIndex = 2;
    private View selectedDayView;

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

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvMonthTitle = findViewById(R.id.tvMonthTitle);
        ImageView btnMonthPrev = findViewById(R.id.btnMonthPrev);
        ImageView btnMonthNext = findViewById(R.id.btnMonthNext);
        updateMonthTitle(tvMonthTitle);

        btnMonthPrev.setOnClickListener(v -> {
            currentMonthIndex = (currentMonthIndex - 1 + MONTHS.length) % MONTHS.length;
            updateMonthTitle(tvMonthTitle);
            Toast.makeText(this, "Showing " + MONTHS[currentMonthIndex], Toast.LENGTH_SHORT).show();
        });

        btnMonthNext.setOnClickListener(v -> {
            currentMonthIndex = (currentMonthIndex + 1) % MONTHS.length;
            updateMonthTitle(tvMonthTitle);
            Toast.makeText(this, "Showing " + MONTHS[currentMonthIndex], Toast.LENGTH_SHORT).show();
        });

        setupDaySelectors();

        // Add schedule actions
        findViewById(R.id.btnAddFirstTask).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.btnAddScheduleTop).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Bind the unified navbar – Bookings tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_BOOKINGS);
    }

    private void updateMonthTitle(TextView tvMonthTitle) {
        if (tvMonthTitle != null) {
            tvMonthTitle.setText(MONTHS[currentMonthIndex]);
        }
    }

    private void setupDaySelectors() {
        bindDay(R.id.dayMon, R.id.tvDayMonLabel, R.id.tvDayMonNumber, true,
                "Monday, 23 March 2024");
        bindDay(R.id.dayTue, R.id.tvDayTueLabel, R.id.tvDayTueNumber, false,
                "Tuesday, 24 March 2024");
        bindDay(R.id.dayWed, R.id.tvDayWedLabel, R.id.tvDayWedNumber, false,
                "Wednesday, 25 March 2024");
        bindDay(R.id.dayThu, R.id.tvDayThuLabel, R.id.tvDayThuNumber, false,
                "Thursday, 26 March 2024");
        bindDay(R.id.dayFri, R.id.tvDayFriLabel, R.id.tvDayFriNumber, false,
                "Friday, 27 March 2024");
        bindDay(R.id.daySat, R.id.tvDaySatLabel, R.id.tvDaySatNumber, false,
                "Saturday, 28 March 2024");
    }

    private void bindDay(int dayViewId, int labelViewId, int numberViewId, boolean selected, String label) {
        View dayView = findViewById(dayViewId);
        TextView labelView = findViewById(labelViewId);
        TextView numberView = findViewById(numberViewId);

        if (dayView == null || labelView == null || numberView == null) {
            return;
        }

        dayView.setOnClickListener(v -> applyDaySelection(v, label));

        if (selected) {
            selectedDayView = dayView;
            applySelectedStyle(dayView, labelView, numberView, true);
        } else {
            applySelectedStyle(dayView, labelView, numberView, false);
        }
    }

    private void applyDaySelection(View clickedView, String label) {
        if (selectedDayView != null && selectedDayView != clickedView) {
            resetDayStyle(selectedDayView);
        }

        TextView labelView;
        TextView numberView;
        if (clickedView.getId() == R.id.dayMon) {
            labelView = findViewById(R.id.tvDayMonLabel);
            numberView = findViewById(R.id.tvDayMonNumber);
        } else if (clickedView.getId() == R.id.dayTue) {
            labelView = findViewById(R.id.tvDayTueLabel);
            numberView = findViewById(R.id.tvDayTueNumber);
        } else if (clickedView.getId() == R.id.dayWed) {
            labelView = findViewById(R.id.tvDayWedLabel);
            numberView = findViewById(R.id.tvDayWedNumber);
        } else if (clickedView.getId() == R.id.dayThu) {
            labelView = findViewById(R.id.tvDayThuLabel);
            numberView = findViewById(R.id.tvDayThuNumber);
        } else if (clickedView.getId() == R.id.dayFri) {
            labelView = findViewById(R.id.tvDayFriLabel);
            numberView = findViewById(R.id.tvDayFriNumber);
        } else {
            labelView = findViewById(R.id.tvDaySatLabel);
            numberView = findViewById(R.id.tvDaySatNumber);
        }

        if (labelView != null && numberView != null) {
            applySelectedStyle(clickedView, labelView, numberView, true);
        }
        selectedDayView = clickedView;
        Toast.makeText(this, "Selected " + label, Toast.LENGTH_SHORT).show();
    }

    private void applySelectedStyle(View dayView, TextView labelView, TextView numberView, boolean selected) {
        dayView.setBackgroundResource(selected ? R.drawable.bg_calendar_day_selected : R.drawable.bg_calendar_day_unselected);
        labelView.setTextColor(selected ? COLOR_ACTIVE : COLOR_INACTIVE);
        numberView.setTextColor(selected ? COLOR_PRIMARY_TEXT : COLOR_PRIMARY_TEXT);
        dayView.setSelected(selected);
        dayView.setScaleX(selected ? SCALE_SELECTED : SCALE_UNSELECTED);
        dayView.setScaleY(selected ? SCALE_SELECTED : SCALE_UNSELECTED);
        dayView.setTranslationY(selected ? -2f : 0f);
        dayView.setElevation(selected ? 4f : 0f);
    }

    private void resetDayStyle(View dayView) {
        int id = dayView.getId();
        if (id == R.id.dayMon) {
            applySelectedStyle(dayView, findViewById(R.id.tvDayMonLabel), findViewById(R.id.tvDayMonNumber), false);
        } else if (id == R.id.dayTue) {
            applySelectedStyle(dayView, findViewById(R.id.tvDayTueLabel), findViewById(R.id.tvDayTueNumber), false);
        } else if (id == R.id.dayWed) {
            applySelectedStyle(dayView, findViewById(R.id.tvDayWedLabel), findViewById(R.id.tvDayWedNumber), false);
        } else if (id == R.id.dayThu) {
            applySelectedStyle(dayView, findViewById(R.id.tvDayThuLabel), findViewById(R.id.tvDayThuNumber), false);
        } else if (id == R.id.dayFri) {
            applySelectedStyle(dayView, findViewById(R.id.tvDayFriLabel), findViewById(R.id.tvDayFriNumber), false);
        } else if (id == R.id.daySat) {
            applySelectedStyle(dayView, findViewById(R.id.tvDaySatLabel), findViewById(R.id.tvDaySatNumber), false);
        }
    }
}
