package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class PostOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_post_options);

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnContinueGig = findViewById(R.id.btnContinueGig);
        MaterialButton btnContinueCommunity = findViewById(R.id.btnContinueCommunity);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnContinueGig != null) {
            btnContinueGig.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
                intent.putExtra("post_type", "gig");
                startActivity(intent);
            });
        }

        if (btnContinueCommunity != null) {
            btnContinueCommunity.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
                intent.putExtra("post_type", "community");
                startActivity(intent);
            });
        }

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
    }
}
