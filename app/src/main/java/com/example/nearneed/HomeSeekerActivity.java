package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HomeSeekerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_home_seeker);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), true);

        // Connect FAB to CreatePostActivity
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), true);
    }
}
