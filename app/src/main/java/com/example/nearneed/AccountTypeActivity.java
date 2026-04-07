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
        cardSeeker.setOnClickListener(v -> completeRegistration("seeker"));
        cardProvider.setOnClickListener(v -> completeRegistration("provider"));
    }

    private void completeRegistration(String role) {
        // You can save the role to shared preferences or pass it via intent
        // Registration is now complete, proceed to Dashboard
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finishAffinity(); // Clear the onboarding activities stack
    }
}
