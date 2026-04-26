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

public class ProfileSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_success);

        View successContainer = findViewById(R.id.successContainer);
        TextView tvUserNamePreview = findViewById(R.id.tvUserNamePreview);
        MaterialButton btnDone = findViewById(R.id.btnDone);

        // Sync real name
        String name = UserPrefs.getName(this);
        if (name != null && !name.isEmpty()) {
            tvUserNamePreview.setText(name);
        }

        // Apply Pop-in effect
        applyPopAnimation(successContainer);

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountTypeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void applyPopAnimation(View view) {
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(800);
        set.setInterpolator(new OvershootInterpolator(1.4f));
        set.start();
    }
}
