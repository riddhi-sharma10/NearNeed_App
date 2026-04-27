package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class RequestDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDistance, tvDescription, tvPosterName, tvType, tvVolunteersCount;
    private MaterialButton btnApply;
    private android.view.View volunteersCard;
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
        tvPosterName = findViewById(R.id.tv_poster_name);
        tvType = findViewById(R.id.tv_request_type);
        tvVolunteersCount = findViewById(R.id.tv_volunteers_count);
        volunteersCard = findViewById(R.id.card_volunteers_needed);
        btnApply = findViewById(R.id.btn_apply_request);
        android.widget.ImageView ivPlaceholder = findViewById(R.id.iv_request_placeholder);

        // Get data from intent (from NearbyRequestsAdapter)
        postId = getIntent().getStringExtra("post_id");
        postTitle = getIntent().getStringExtra("title");
        postType = getIntent().getStringExtra("type");
        creatorId = getIntent().getStringExtra("creator_id");
        String description = getIntent().getStringExtra("description");
        String location = getIntent().getStringExtra("location");
        String category = getIntent().getStringExtra("category");
        int slots = getIntent().getIntExtra("slots", 0);

        boolean isCommunity = "COMMUNITY".equalsIgnoreCase(postType);

        // Set data
        tvTitle.setText(postTitle != null ? postTitle : "Request Details");
        tvDistance.setText(location != null ? location : "Nearby");
        tvDescription.setText(description != null ? description : "No description available");

        // Handle Poster Name
        if (creatorId != null) {
            tvPosterName.setOnClickListener(v -> {
                Intent intent = new Intent(this, PersonProfileActivity.class);
                intent.putExtra("user_id", creatorId);
                startActivity(intent);
            });
            fetchPosterName(creatorId);
        }

        // Community specific UI
        if (isCommunity) {
            tvType.setVisibility(android.view.View.VISIBLE);
            volunteersCard.setVisibility(android.view.View.VISIBLE);
            tvVolunteersCount.setText(slots > 0 ? slots + " volunteers needed" : "Volunteers needed");
            
            btnApply.setText("Volunteer");
            btnApply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(this, R.color.brand_success)));
        }

        // Set placeholder image based on category
        if (ivPlaceholder != null) {
            int placeholderRes = R.drawable.img_neighborhood; // Default
            if (category != null) {
                String cat = category.toLowerCase();
                if (cat.contains("plumbing") || cat.contains("tap") || cat.contains("leak")) {
                    placeholderRes = R.drawable.img_plumbing;
                } else if (cat.contains("garden") || cat.contains("mow") || cat.contains("lawn")) {
                    placeholderRes = R.drawable.img_lawn_mowing;
                } else if (cat.contains("dog") || cat.contains("pet") || cat.contains("walk")) {
                    placeholderRes = R.drawable.img_dog_walking;
                }
            }
            ivPlaceholder.setImageResource(placeholderRes);
        }

        // Apply button click
        btnApply.setOnClickListener(v -> showApplySheet());
    }

    private void fetchPosterName(String userId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null) tvPosterName.setText(name);
                    }
                });
    }

    private void showApplySheet() {
        RequestApplyBottomSheet sheet = RequestApplyBottomSheet.newInstance(postId, postTitle, postType, creatorId);
        sheet.show(getSupportFragmentManager(), "apply_sheet");
    }
}
