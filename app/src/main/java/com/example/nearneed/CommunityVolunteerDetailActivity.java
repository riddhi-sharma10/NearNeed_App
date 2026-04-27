package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class CommunityVolunteerDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDescription, tvLocation;
    private MaterialButton btnVolunteer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_volunteer_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_community_volunteer_title);
        tvDescription = findViewById(R.id.tv_community_volunteer_description);

        tvLocation = findViewById(R.id.tv_volunteer_location);
        btnVolunteer = findViewById(R.id.btn_volunteer_community);

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");

        String location = getIntent().getStringExtra("location");
        int slots = getIntent().getIntExtra("slots", 0);

        // Set data
        tvTitle.setText(title != null ? title : "Community Need");
        tvDescription.setText(description != null ? description : "No description available");
        tvLocation.setText(location != null ? location : "Location unknown");

        // View Volunteers button click
        btnVolunteer.setOnClickListener(v -> openVolunteersList(slots, title));
    }

    /**
     * Opens the volunteers list where seeker can accept/reject applicants.
     */
    private void openVolunteersList(int maxSlots, String postTitle) {
        Intent intent = new Intent(this, VolunteersActivity.class);
        intent.putExtra("post_id", getIntent().getStringExtra("post_id"));
        intent.putExtra("max_slots", maxSlots);
        intent.putExtra("post_title", postTitle);
        intent.putExtra("is_seeker", true); // Flag to show accept/reject buttons
        startActivity(intent);
    }
}
