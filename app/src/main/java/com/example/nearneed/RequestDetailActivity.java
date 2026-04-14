package com.example.nearneed;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDistance, tvDescription;
    private MaterialButton btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_request_title);
        tvDistance = findViewById(R.id.tv_request_distance);
        tvDescription = findViewById(R.id.tv_request_description);
        btnApply = findViewById(R.id.btn_apply_request);

        // Get data from intent
        String title = getIntent().getStringExtra("title");
        String distance = getIntent().getStringExtra("distance");
        String description = getIntent().getStringExtra("description");

        // Set data
        tvTitle.setText(title != null ? title : "Request Details");
        tvDistance.setText(distance != null ? distance : "Distance unknown");
        tvDescription.setText(description != null ? description : "No description available");

        // Apply button click
        btnApply.setOnClickListener(v -> showApplySheet());
    }

    /**
     * Shows the apply sheet with message, budget, and payment options.
     */
    private void showApplySheet() {
        RequestApplyBottomSheet sheet = new RequestApplyBottomSheet();
        sheet.show(getSupportFragmentManager(), "apply_sheet");
    }
}
