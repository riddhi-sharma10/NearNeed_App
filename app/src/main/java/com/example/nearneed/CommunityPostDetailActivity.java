package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class CommunityPostDetailActivity extends AppCompatActivity {

    private boolean isProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tv_community_title);
        TextView tvDescription = findViewById(R.id.tv_community_description);
        TextView tvPostedBy = findViewById(R.id.tv_posted_by);
        TextView tvPostedTime = findViewById(R.id.tv_posted_time);
        TextView tvLocation = findViewById(R.id.tv_community_location);
        TextView tvSlotsAvailable = findViewById(R.id.tv_slots_available);
        MaterialButton btnVolunteer = findViewById(R.id.btn_volunteer);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String postedBy = intent.getStringExtra("postedBy");
        String postedTime = intent.getStringExtra("postedTime");
        String location = intent.getStringExtra("location");
        String slots = intent.getStringExtra("slots");
        boolean posterVerified = intent.getBooleanExtra("IS_POSTER_VERIFIED", false);
        isProvider = RoleManager.isProvider(this);

        tvTitle.setText(title != null ? title : "Community Need");
        tvDescription.setText(description != null ? description : "No description available");
        tvPostedBy.setText(postedBy != null ? postedBy : "Posted by someone");
        VerifiedBadgeHelper.apply(this, tvPostedBy, posterVerified);
        tvPostedTime.setText(postedTime != null ? postedTime : "Recently");
        tvLocation.setText(location != null ? location : "Location unknown");
        tvSlotsAvailable.setText(slots != null ? slots : "Volunteers needed");

        btnVolunteer.setText(isProvider ? "Volunteer" : "View Applicants");

        btnVolunteer.setOnClickListener(v -> {
            if (isProvider) {
                new CommunityVolunteerBottomSheet()
                    .show(getSupportFragmentManager(), "community_volunteer_sheet");
                return;
            }

            String postTitle = tvTitle.getText().toString();
            int maxSlots = parseMaxSlots(tvSlotsAvailable.getText().toString());

            Intent volunteersIntent = new Intent(CommunityPostDetailActivity.this, VolunteersActivity.class);
            volunteersIntent.putExtra("post_title", postTitle);
            volunteersIntent.putExtra("max_slots", maxSlots);
            volunteersIntent.putExtra("is_seeker", true);
            startActivity(volunteersIntent);
        });
    }

    private int parseMaxSlots(String slotsText) {
        if (slotsText == null) {
            return 5;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(slotsText);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {
                // Fall back to default below.
            }
        }
        return 5;
    }
}
