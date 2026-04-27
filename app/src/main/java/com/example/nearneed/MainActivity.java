package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity acts as a central dispatcher that routes the user to the correct 
 * Home screen based on their persistent role (Seeker or Provider).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Trigger one-time master reset if needed
        checkAndPerformReset();
    }

    private void checkAndPerformReset() {
        android.content.SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isResetDone = prefs.getBoolean("db_master_reset_v3", false);

        if (!isResetDone) {
            Log.d("MainActivity", "Triggering Master Database Reset...");
            new DataSeeder().resetAndSeed(new DataSeeder.SeederCallback() {
                @Override
                public void onSuccess() {
                    prefs.edit().putBoolean("db_master_reset_v3", true).apply();
                    UserPrefs.clear(MainActivity.this);
                    Log.d("MainActivity", "Database Reset Complete.");
                    dispatchByRole();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MainActivity", "Database Reset Failed", e);
                    Toast.makeText(MainActivity.this, "Database Sync Failed. Check Connection.", Toast.LENGTH_LONG).show();
                    dispatchByRole(); // Continue anyway to avoid bricking app
                }
            });
        } else {
            dispatchByRole();
        }
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