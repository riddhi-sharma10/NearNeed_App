package com.example.nearneed;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDistance, tvDescription;
    private MaterialButton btnApply;
    private String postId, postTitle, postType, creatorId;

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

        // Get data from intent (from NearbyRequestsAdapter)
        postId = getIntent().getStringExtra("post_id");
        postTitle = getIntent().getStringExtra("title");
        postType = getIntent().getStringExtra("type");
        creatorId = getIntent().getStringExtra("creator_id");
        String description = getIntent().getStringExtra("description");
        String distance = getIntent().getStringExtra("distance");

        // Set data
        tvTitle.setText(postTitle != null ? postTitle : "Request Details");
        tvDistance.setText(distance != null ? distance : "Nearby");
        tvDescription.setText(description != null ? description : "No description available");

        // Apply button click
        btnApply.setOnClickListener(v -> showApplySheet());
    }

    private void showApplySheet() {
        RequestApplyBottomSheet sheet = RequestApplyBottomSheet.newInstance(postId, postTitle, postType, creatorId);
        sheet.show(getSupportFragmentManager(), "apply_sheet");
    }
}
