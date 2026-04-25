package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class VolunteerProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvName, tvRating, tvLocation, tvBio;
    private TextView tvVolunteeredCount, tvCompletedCount, tvRatingScore;
    private MaterialButton btnMessage, btnReport;
    private RecyclerView rvReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_profile);

        initViews();
        setupToolbar();
        loadVolunteerData();
        setupReviewsList();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvName);
        tvRating = findViewById(R.id.tvRating);
        tvLocation = findViewById(R.id.tvLocation);
        tvBio = findViewById(R.id.tvBio);
        tvVolunteeredCount = findViewById(R.id.tvVolunteeredCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvRatingScore = findViewById(R.id.tvRatingScore);
        btnMessage = findViewById(R.id.btnMessage);
        btnReport = findViewById(R.id.btnReport);
        rvReviews = findViewById(R.id.rvReviews);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadVolunteerData() {
        tvName.setText("Priya Sharma");
        VerifiedBadgeHelper.apply(this, tvName, getIntent().getBooleanExtra("IS_VERIFIED", false));
        tvRating.setText("4.9");
        tvLocation.setText("2.5 km away");
        tvBio.setText("Experienced volunteer with a passion for community service. "
            + "I love helping neighbors and making a positive impact in our community.");

        tvVolunteeredCount.setText("12");
        tvCompletedCount.setText("12");
        tvRatingScore.setText("4.9");

        btnMessage.setOnClickListener(v -> {
            String volunteerName = tvName.getText() != null ? tvName.getText().toString() : "Volunteer";
            android.content.Intent intent = new android.content.Intent(this, ChatActivity.class);
            intent.putExtra("CHAT_NAME", volunteerName);
            intent.putExtra("CHAT_ONLINE", true);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> showReportDialog());
    }

    private void showReportDialog() {
        final String[] reasons = new String[] {
            "No-show",
            "Inappropriate behavior",
            "Spam or scam",
            "Other"
        };

        new AlertDialog.Builder(this)
            .setTitle("Report volunteer")
            .setItems(reasons, (dialog, which) ->
                Toast.makeText(this, "Report submitted: " + reasons[which], Toast.LENGTH_SHORT).show())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setupReviewsList() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(
            "Sarah Johnson",
            4.5f,
            "Very helpful and reliable person. Completed the task on time!",
            System.currentTimeMillis() - 86400000 * 2
        ));

        reviews.add(new Review(
            "Mike Chen",
            5.0f,
            "Excellent service. Highly recommended!",
            System.currentTimeMillis() - 86400000 * 7
        ));

        reviews.add(new Review(
            "Emma Davis",
            4.8f,
            "Professional and courteous. Will definitely ask for help again.",
            System.currentTimeMillis() - 86400000 * 14
        ));

        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviews);
        rvReviews.setAdapter(reviewsAdapter);
    }
}
