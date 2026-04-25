package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class PersonProfileActivity extends AppCompatActivity {

    private ListenerRegistration profileListener;
    private String personUserId;
    private String fallbackName;
    private String fallbackReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                finish();
            }
        });

        String name = readExtra("PERSON_NAME", "NearNeed User");
        String email = readExtra("PERSON_EMAIL", "user@nearneed.app");
        String phone = readExtra("PERSON_PHONE", "+91 98765 43210");
        String gender = readExtra("PERSON_GENDER", "Not specified");
        String experience = readExtra("PERSON_EXPERIENCE", "3 years");
        String rating = readExtra("PERSON_RATING", "4.7");
        String reviews = readExtra("PERSON_REVIEWS", "100 reviews");
        String bio = readExtra("PERSON_BIO", "Active NearNeed member with positive community engagement.");

        personUserId = getIntent().getStringExtra("PERSON_USER_ID");
        fallbackName = name;
        fallbackReviews = reviews;

        TextView tvName = findViewById(R.id.tvName);
        tvName.setText(name);
        VerifiedBadgeHelper.apply(this, tvName, getIntent().getBooleanExtra("IS_VERIFIED", false));
        ((TextView) findViewById(R.id.tvEmail)).setText(email);
        ((TextView) findViewById(R.id.tvPhone)).setText(phone);
        ((TextView) findViewById(R.id.tvGender)).setText(gender);
        ((TextView) findViewById(R.id.tvExperience)).setText(experience);
        ((TextView) findViewById(R.id.tvRating)).setText(rating + " ★");
        ((TextView) findViewById(R.id.tvReviews)).setText(reviews);
        ((TextView) findViewById(R.id.tvBio)).setText(bio);

        ImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            ivProfile.setImageResource(avatarForGender(gender));
        }

        View reviewsChip = findViewById(R.id.llReviewsChip);
        View ratingRow = findViewById(R.id.llRatingRow);
        View.OnClickListener openReviews = v -> {
            Intent intent = new Intent(PersonProfileActivity.this, ReviewsActivity.class);
            intent.putExtra("PERSON_NAME",
                    tvName != null && tvName.getText() != null ? tvName.getText().toString() : "");
            startActivity(intent);
        };
        if (reviewsChip != null) {
            reviewsChip.setOnClickListener(openReviews);
        }
        if (ratingRow != null) {
            ratingRow.setOnClickListener(openReviews);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribeRealtimeProfile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }

    private void subscribeRealtimeProfile() {
        if (personUserId == null || personUserId.trim().isEmpty()) {
            return;
        }
        profileListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(personUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }
                    applyLiveProfile(snapshot);
                });
    }

    private void applyLiveProfile(DocumentSnapshot snapshot) {
        String name = getValue(snapshot, "name", fallbackName);
        String email = getValue(snapshot, "email", ((TextView) findViewById(R.id.tvEmail)).getText().toString());
        String phone = getValue(snapshot, "phone", ((TextView) findViewById(R.id.tvPhone)).getText().toString());
        String gender = getValue(snapshot, "gender", ((TextView) findViewById(R.id.tvGender)).getText().toString());
        String experience = getValue(snapshot, "experience", ((TextView) findViewById(R.id.tvExperience)).getText().toString());
        String rating = getValue(snapshot, "rating", ((TextView) findViewById(R.id.tvRating)).getText().toString().replace(" ★", ""));
        String reviews = getValue(snapshot, "reviews", fallbackReviews);
        String bio = getValue(snapshot, "address", ((TextView) findViewById(R.id.tvBio)).getText().toString());
        Boolean verified = snapshot.getBoolean("isVerified");
        String profileImage = getValue(snapshot, "profileImage", null);

        TextView tvName = findViewById(R.id.tvName);
        tvName.setText(name);
        VerifiedBadgeHelper.apply(this, tvName, verified != null && verified);

        ((TextView) findViewById(R.id.tvEmail)).setText(email);
        ((TextView) findViewById(R.id.tvPhone)).setText(phone);
        ((TextView) findViewById(R.id.tvGender)).setText(gender);
        ((TextView) findViewById(R.id.tvExperience)).setText(experience);
        ((TextView) findViewById(R.id.tvRating)).setText(rating + " ★");
        ((TextView) findViewById(R.id.tvReviews)).setText(reviews);
        ((TextView) findViewById(R.id.tvBio)).setText(bio);

        LinearLayout llVerified = findViewById(R.id.llVerifiedBadge);
        if (llVerified != null) {
            llVerified.setVisibility(Boolean.TRUE.equals(verified) ? View.VISIBLE : View.GONE);
        }

        ImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            if (profileImage != null && !profileImage.trim().isEmpty()) {
                Glide.with(this).load(profileImage).circleCrop().into(ivProfile);
            } else {
                ivProfile.setImageResource(avatarForGender(gender));
            }
        }
    }

    private String getValue(DocumentSnapshot snapshot, String key, String fallback) {
        String v = snapshot.getString(key);
        return v == null || v.trim().isEmpty() ? fallback : v;
    }

    private int avatarForGender(String gender) {
        if (gender == null) return R.drawable.avatar_alex;
        String lower = gender.toLowerCase();
        if (lower.contains("female")) return R.drawable.avatar_sarah;
        if (lower.contains("male"))   return R.drawable.avatar_david;
        return R.drawable.avatar_alex;
    }

    private String readExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
