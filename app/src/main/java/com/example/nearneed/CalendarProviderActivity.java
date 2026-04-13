package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CalendarProviderActivity extends AppCompatActivity {

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
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Add schedule actions
        findViewById(R.id.btnAddFirstTask).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnAddScheduleTop).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        // Bind the unified navbar – Bookings tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_BOOKINGS);
    }
}
