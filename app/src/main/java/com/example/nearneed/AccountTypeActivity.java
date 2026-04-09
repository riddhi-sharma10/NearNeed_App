package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

public class AccountTypeActivity extends AppCompatActivity {

    private MaterialCardView cardSeeker, cardProvider;
    private MaterialButton btnSeekerAction, btnProviderAction;

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
        btnSeekerAction = findViewById(R.id.btnSeekerAction);
        btnProviderAction = findViewById(R.id.btnProviderAction);
    }

    private void setupListeners() {
        cardSeeker.setOnClickListener(v -> completeRegistration("seeker"));
        btnSeekerAction.setOnClickListener(v -> completeRegistration("seeker"));
        
        cardProvider.setOnClickListener(v -> completeRegistration("provider"));
        btnProviderAction.setOnClickListener(v -> completeRegistration("provider"));
    }

    private void completeRegistration(String role) {
        if ("seeker".equals(role)) {
            RoleManager.setRole(this, RoleManager.ROLE_SEEKER);
            // Direct navigation to the Seeker No Posts page as requested
            Intent intent = new Intent(this, HomeSeekerNoPostsActivity.class);
            startActivity(intent);
        } else {
            RoleManager.setRole(this, RoleManager.ROLE_PROVIDER);
            // Standard flow for providers
            Intent intent = new Intent(this, CommunityPreferencesActivity.class);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
        }
    }
}
