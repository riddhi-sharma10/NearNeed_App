package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageButton;

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

        rvReviews.setAdapter(new ReviewsAdapter(reviews));
    }
}
