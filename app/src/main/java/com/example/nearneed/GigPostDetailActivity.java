package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class GigPostDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvBudget, tvDescription, tvDistance, tvDuration, tvAddress;
    private android.widget.ImageView ivGigHero;
    private MaterialButton btnViewApplicants;
    private String postId;

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

        ivGigHero = findViewById(R.id.iv_gig_hero);
        tvTitle = findViewById(R.id.tv_gig_title);
        tvCategory = findViewById(R.id.tv_gig_category);
        tvBudget = findViewById(R.id.tv_gig_budget);
        tvDescription = findViewById(R.id.tv_gig_description);
        tvDistance = findViewById(R.id.tv_gig_distance);
        tvDuration = findViewById(R.id.tv_gig_duration);
        tvAddress = findViewById(R.id.tv_gig_address);
        btnViewApplicants = findViewById(R.id.btn_view_applicants);

        // Get data from intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("post_id");
        String title = intent.getStringExtra("title");
        String category = intent.getStringExtra("category");
        String description = intent.getStringExtra("description");
        String address = intent.getStringExtra("address");

        // Set initial data
        tvTitle.setText(title != null ? title : "Gig Details");
        tvCategory.setText(category != null ? category : "Category");
        tvDescription.setText(description != null ? description : "No description available");
        tvAddress.setText(address != null ? address : "Address not specified");

        updateHeroImage(category);

        // Real-time observation for accepted booking/budget
        if (postId != null) {
            BookingRepository.observeBookingsForPost(postId, new BookingRepository.BookingListener() {
                @Override
                public void onBookingsLoaded(java.util.List<Booking> bookings) {
                    if (bookings != null && !bookings.isEmpty()) {
                        Booking accepted = null;
                        for (Booking b : bookings) {
                            if (!"cancelled".equalsIgnoreCase(b.status)) {
                                accepted = b;
                                break;
                            }
                        }
                        
                        if (accepted != null && tvBudget != null) {
                            String price = (accepted.amount != null) ? "₹" + accepted.amount.intValue() : "Price agreed";
                            tvBudget.setText(price);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {}
            });
        }

        btnViewApplicants.setOnClickListener(v -> {
            if (postId == null || postId.trim().isEmpty()) return;

            Intent applicantsIntent = new Intent(GigPostDetailActivity.this, ResponsesActivity.class);
            applicantsIntent.putExtra("post_id", postId);
            applicantsIntent.putExtra("post_title", tvTitle.getText().toString());
            applicantsIntent.putExtra("is_gig", true);
            startActivity(applicantsIntent);
        });
    }

    private void updateHeroImage(String category) {
        if (category == null || ivGigHero == null) return;
        
        int resId = R.drawable.welcome_bg_optimized; // Default
        
        String lower = category.toLowerCase();
        if (lower.contains("plumbing")) {
            resId = R.drawable.img_gig_hero_plumbing;
        } else if (lower.contains("cleaning")) {
            resId = R.drawable.img_gig_hero_cleaning;
        } else if (lower.contains("electrical")) {
            resId = R.drawable.img_gig_hero_electrical;
        }
        
        ivGigHero.setImageResource(resId);
    }
}
