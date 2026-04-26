package com.example.nearneed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class IdVerifiedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_verified);

        TextView tvName = findViewById(R.id.tvUserNameVerified);
        TextView tvDetails = findViewById(R.id.tvUserDetailsVerified);
        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        View card = findViewById(android.R.id.content); // Use content for pop animation

        // Sync real name
        String name = UserPrefs.getName(this);
        String location = UserPrefs.getLocation(this);
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        }
        if (location != null && !location.isEmpty()) {
            tvDetails.setText("Verified Neighbor • " + location);
        }

        // Pop Animation
        applyPopAnimation(findViewById(R.id.btnContinue).getRootView());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileSuccessActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void applyPopAnimation(View view) {
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        view.setAlpha(0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(600);
        set.setInterpolator(new OvershootInterpolator(1.2f));
        set.start();
    }
}
