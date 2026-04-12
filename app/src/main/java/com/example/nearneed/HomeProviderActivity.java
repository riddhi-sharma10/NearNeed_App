package com.example.nearneed;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_provider);
        
        findViewById(R.id.viewCalendar).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CalendarProviderActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CommunityPostActivity.class);
            startActivity(intent);
        });

        setupNavbar();
    }

    private void setupNavbar() {
        android.view.View navbar = findViewById(R.id.floatingNavbar);
        if (navbar == null) {
            // If the include doesn't have the ID, try finding containers directly
            navbar = findViewById(android.R.id.content);
        }

        android.view.View iconHome = findViewById(R.id.nav_home_container);
        android.view.View iconCalendar = findViewById(R.id.nav_bookings_container);
        android.view.View iconChat = findViewById(R.id.nav_chat_container);
        android.view.View iconProfile = findViewById(R.id.nav_profile_container);

        if (iconHome != null) iconHome.setOnClickListener(v -> {
            // Already here
        });

        if (iconCalendar != null) iconCalendar.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CalendarProviderActivity.class);
            startActivity(intent);
        });

        if (iconChat != null) iconChat.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, MessagesActivity.class);
            startActivity(intent);
        });

        if (iconProfile != null) iconProfile.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}
