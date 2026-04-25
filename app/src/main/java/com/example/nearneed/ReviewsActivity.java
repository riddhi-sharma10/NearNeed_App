package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tvReviewsTitle);
        String personName = getIntent().getStringExtra("PERSON_NAME");
        if (tvTitle != null && personName != null && !personName.trim().isEmpty()) {
            tvTitle.setText(personName + " Reviews");
        }

        RecyclerView rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(
            "Sarah Johnson",
            4.5f,
            "Very helpful and reliable person. Completed the task on time!",
            System.currentTimeMillis() - 86400000L * 2
        ));
        reviews.add(new Review(
            "Mike Chen",
            5.0f,
            "Excellent service. Highly recommended!",
            System.currentTimeMillis() - 86400000L * 7
        ));
        reviews.add(new Review(
            "Emma Davis",
            4.8f,
            "Professional and courteous. Will definitely ask for help again.",
            System.currentTimeMillis() - 86400000L * 14
        ));
        reviews.add(new Review(
            "Olivia Brown",
            4.9f,
            "Quick response and clean work. Very satisfied with the service.",
            System.currentTimeMillis() - 86400000L * 21
        ));
        reviews.add(new Review(
            "Noah Wilson",
            4.6f,
            "Friendly, punctual, and easy to communicate with throughout.",
            System.currentTimeMillis() - 86400000L * 30
        ));
        reviews.add(new Review(
            "Ava Martinez",
            5.0f,
            "One of the best experiences I have had on NearNeed.",
            System.currentTimeMillis() - 86400000L * 41
        ));
        reviews.add(new Review(
            "Liam Patel",
            4.7f,
            "Good quality and very professional approach. Recommended.",
            System.currentTimeMillis() - 86400000L * 52
        ));

        rvReviews.setAdapter(new ReviewsAdapter(reviews));
    }
}
