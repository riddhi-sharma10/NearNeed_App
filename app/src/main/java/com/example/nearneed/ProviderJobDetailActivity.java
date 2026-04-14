package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class ProviderJobDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvBudget, tvDescription, tvDistance, tvDuration;
    private MaterialButton btnApplyGig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_job_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_provider_job_title);
        tvCategory = findViewById(R.id.tv_provider_job_category);
        tvBudget = findViewById(R.id.tv_provider_job_budget);
        tvDescription = findViewById(R.id.tv_provider_job_description);
        tvDistance = findViewById(R.id.tv_provider_job_distance);
        tvDuration = findViewById(R.id.tv_provider_job_duration);
        btnApplyGig = findViewById(R.id.btn_apply_provider_gig);

        // Get data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String category = intent.getStringExtra("category");
        String budget = intent.getStringExtra("budget");
        String description = intent.getStringExtra("description");
        String distance = intent.getStringExtra("distance");
        String duration = intent.getStringExtra("duration");
        String type = intent.getStringExtra("type");

        // Set data to views
        tvTitle.setText(title != null ? title : "Job Details");
        tvCategory.setText(category != null ? category : "Category");
        tvBudget.setText(budget != null ? budget : "Budget not specified");
        tvDescription.setText(description != null ? description : "No description available");
        tvDistance.setText(distance != null ? distance : "Distance unknown");
        tvDuration.setText(duration != null ? duration : "Duration not specified");

        // Apply button logic
        btnApplyGig.setOnClickListener(v -> {
            new RequestApplyBottomSheet().show(getSupportFragmentManager(), "provider_gig_apply_sheet");
        });
    }
}
