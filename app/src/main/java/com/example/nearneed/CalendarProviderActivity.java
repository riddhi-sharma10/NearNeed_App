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
        window.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_calendar_provider);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnAddFirstTask).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnAddScheduleTop).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddScheduleActivity.class);
            startActivity(intent);
        });

        setupNavbar();
    }

    private void setupNavbar() {
        android.view.View navbar = findViewById(R.id.floatingNavbar);
        if (navbar == null) {
            navbar = findViewById(android.R.id.content);
        }

        android.view.View iconHome = findViewById(R.id.nav_home_container);
        android.view.View iconCalendar = findViewById(R.id.nav_bookings_container);
        android.view.View iconChat = findViewById(R.id.nav_chat_container);
        android.view.View iconProfile = findViewById(R.id.nav_profile_container);

        if (iconHome != null) iconHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeProviderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        if (iconCalendar != null) iconCalendar.setOnClickListener(v -> {
            // Already here
        });

        if (iconChat != null) iconChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
        });

        if (iconProfile != null) iconProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
