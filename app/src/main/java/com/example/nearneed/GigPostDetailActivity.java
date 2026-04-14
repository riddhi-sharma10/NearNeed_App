package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class GigPostDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvBudget, tvDescription, tvDistance, tvDuration;
    private MaterialButton btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gig_post_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_gig_title);
        tvCategory = findViewById(R.id.tv_gig_category);
        tvBudget = findViewById(R.id.tv_gig_budget);
        tvDescription = findViewById(R.id.tv_gig_description);
        tvDistance = findViewById(R.id.tv_gig_distance);
        tvDuration = findViewById(R.id.tv_gig_duration);
        btnApply = findViewById(R.id.btn_apply_gig);

        // Get data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String category = intent.getStringExtra("category");
        String budget = intent.getStringExtra("budget");
        String description = intent.getStringExtra("description");
        String distance = intent.getStringExtra("distance");
        String duration = intent.getStringExtra("duration");

        // Set data to views
        tvTitle.setText(title != null ? title : "Gig Details");
        tvCategory.setText(category != null ? category : "Category");
        tvBudget.setText(budget != null ? budget : "Budget not specified");
        tvDescription.setText(description != null ? description : "No description available");
        tvDistance.setText(distance != null ? distance : "Distance unknown");
        tvDuration.setText(duration != null ? duration : "Duration not specified");

        btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "Opening payment flow...", Toast.LENGTH_SHORT).show();
            // In a real app, this would navigate to PaymentFlowActivity or apply flow
            finish();
        });
    }
}
