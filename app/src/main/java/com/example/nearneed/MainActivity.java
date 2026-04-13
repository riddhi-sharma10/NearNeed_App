package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity acts as a central dispatcher that routes the user to the correct 
 * Home screen based on their persistent role (Seeker or Provider).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Dispatch based on saved role
        dispatchByRole();
    }

    private void dispatchByRole() {
        String role = RoleManager.getRole(this);
        Intent intent;

        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            intent = new Intent(this, HomeProviderActivity.class);
        } else {
            // Further logic for seeker (with or without posts) can be handled in HomeSeekerActivity 
            // or redirected here if needed.
            intent = new Intent(this, HomeSeekerActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}