package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PersonProfileActivity extends AppCompatActivity {

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

        ((TextView) findViewById(R.id.tvName)).setText(name);
        ((TextView) findViewById(R.id.tvEmail)).setText(email);
        ((TextView) findViewById(R.id.tvPhone)).setText(phone);
        ((TextView) findViewById(R.id.tvGender)).setText(gender);
        ((TextView) findViewById(R.id.tvExperience)).setText(experience);
        ((TextView) findViewById(R.id.tvRating)).setText(rating + " ★");
        ((TextView) findViewById(R.id.tvReviews)).setText(reviews);
        ((TextView) findViewById(R.id.tvBio)).setText(bio);

        ImageView ivProfile = findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            String lowerGender = gender.toLowerCase();
            if (lowerGender.contains("female")) {
                ivProfile.setImageResource(R.drawable.avatar_sarah);
            } else if (lowerGender.contains("male")) {
                ivProfile.setImageResource(R.drawable.avatar_david);
            } else {
                ivProfile.setImageResource(R.drawable.avatar_alex);
            }
        }
    }

    private String readExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
