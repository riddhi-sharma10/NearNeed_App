package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeSeekerNoPostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_seeker_no_posts);

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), true);

        // Connect the FAB to potentially a Create Post Activity (standard behavior)
        FloatingActionButton fab = findViewById(R.id.fab_add_seeker);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreatePostActivity.class);
                startActivity(intent);
            });
        }
    }
}
