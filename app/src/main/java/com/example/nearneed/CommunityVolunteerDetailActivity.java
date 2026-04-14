package com.example.nearneed;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class CommunityVolunteerDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDescription, tvPostedBy, tvLocation;
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
        tvPostedBy = findViewById(R.id.tv_volunteer_posted_by);
        tvLocation = findViewById(R.id.tv_volunteer_location);
        btnVolunteer = findViewById(R.id.btn_volunteer_community);

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String postedBy = getIntent().getStringExtra("postedBy");
        String location = getIntent().getStringExtra("location");

        // Set data
        tvTitle.setText(title != null ? title : "Community Need");
        tvDescription.setText(description != null ? description : "No description available");
        tvPostedBy.setText(postedBy != null ? postedBy : "Posted by someone");
        tvLocation.setText(location != null ? location : "Location unknown");

        // Volunteer button click
        btnVolunteer.setOnClickListener(v -> showVolunteerSheet());
    }

    /**
     * Shows the volunteer sheet with message input only (no budget/payment).
     */
    private void showVolunteerSheet() {
        CommunityVolunteerBottomSheet sheet = new CommunityVolunteerBottomSheet();
        sheet.show(getSupportFragmentManager(), "volunteer_sheet");
    }
}
