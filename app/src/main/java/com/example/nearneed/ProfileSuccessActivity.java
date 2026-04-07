package com.example.nearneed;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_success);

        View successContainer = findViewById(R.id.successContainer);
        MaterialButton btnDone = findViewById(R.id.btnDone);

        // Apply Pop-in effect
        applyPopAnimation(successContainer);

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
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
