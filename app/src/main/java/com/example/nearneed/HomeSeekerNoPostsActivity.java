package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeSeekerNoPostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_seeker_no_posts);

        // Connect the FAB to potentially a Create Post Activity (standard behavior)
        // Connect the Add Button to PostOptionsActivity
        View btnAdd = findViewById(R.id.fab_add_seeker);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, PostOptionsActivity.class);
                startActivity(intent);
            });
        }

        SeekerNavbarController.bind(this, findViewById(android.R.id.content), SeekerNavbarController.TAB_HOME);
    }
}
