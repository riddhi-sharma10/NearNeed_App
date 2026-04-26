package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

public class AccountTypeActivity extends AppCompatActivity {

    private ImageButton btnBack;
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
        btnBack = findViewById(R.id.btnBack);
        cardSeeker = findViewById(R.id.cardSeeker);
        cardProvider = findViewById(R.id.cardProvider);
        btnSeekerAction = findViewById(R.id.btnSeekerAction);
        btnProviderAction = findViewById(R.id.btnProviderAction);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        cardSeeker.setOnClickListener(v -> completeRegistration("seeker"));
        btnSeekerAction.setOnClickListener(v -> completeRegistration("seeker"));
        
        cardProvider.setOnClickListener(v -> completeRegistration("provider"));
        btnProviderAction.setOnClickListener(v -> completeRegistration("provider"));
    }

    private void completeRegistration(String role) {
        String finalRole = "seeker".equals(role) ? RoleManager.ROLE_SEEKER : RoleManager.ROLE_PROVIDER;
        RoleManager.setRole(this, finalRole);

        // Save role to Firestore so other users can find providers
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("role", finalRole);
        UserProfileRepository.saveCurrentUserProfile(updates, null);

        if ("seeker".equals(role)) {
            Intent intent = new Intent(this, HomeSeekerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, CommunityPreferencesActivity.class);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
        }
    }

}
