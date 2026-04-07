package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class AccountTypeActivity extends AppCompatActivity {

    private MaterialCardView cardSeeker;
    private MaterialCardView cardProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardSeeker = findViewById(R.id.cardSeeker);
        cardProvider = findViewById(R.id.cardProvider);
    }

    private void setupListeners() {
        cardSeeker.setOnClickListener(v -> navigateToProfileSetup("seeker"));
        cardProvider.setOnClickListener(v -> navigateToProfileSetup("provider"));
    }

    private void navigateToProfileSetup(String role) {
        // You can save the role to shared preferences or pass it via intent
        Intent intent = new Intent(this, ProfileInfoActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
    }
}
