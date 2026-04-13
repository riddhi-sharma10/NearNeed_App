package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @deprecated Replaced by {@link HomeFragment}.
 * Layout logic has been migrated; this class is kept only for back-compat.
 */
@Deprecated
public class HomeProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure user is in the correct role for this activity
        if (RoleManager.ROLE_SEEKER.equals(RoleManager.getRole(this))) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home_provider);

        // Today's schedule → Calendar
        findViewById(R.id.viewCalendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarProviderActivity.class);
            startActivity(intent);
        });

        // Post community request
        findViewById(R.id.btnPostCommunityRequest).setOnClickListener(v -> {
            Intent intent = new Intent(this, CommunityPostActivity.class);
            startActivity(intent);
        });

        // Bind the unified navbar – Home tab active
        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
    }
}
