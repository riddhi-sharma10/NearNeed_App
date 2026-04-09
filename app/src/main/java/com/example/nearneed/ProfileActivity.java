package com.example.nearneed;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = RoleManager.getRole(this);
        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            setContentView(R.layout.layout_profile_provider);
            SeekerNavbarController.bind(this, findViewById(android.R.id.content), false);
        } else {
            setContentView(R.layout.layout_profile_seeker);
            SeekerNavbarController.bind(this, findViewById(android.R.id.content), false);
        }

        ProfileModeSwitcher.bind(this, findViewById(android.R.id.content), role);
    }
}
