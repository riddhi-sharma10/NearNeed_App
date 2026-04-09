package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class PostedSuccessfullyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_successfully);

        ImageView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> navigateHome());

        findViewById(R.id.btnViewPost).setOnClickListener(v -> navigateHome());
        findViewById(R.id.btnBackToHome).setOnClickListener(v -> navigateHome());
    }

    private void navigateHome() {
        // Clear activity stack and return to the main Seeker Home screen (showing the new post)
        Intent intent = new Intent(this, HomeSeekerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
