package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoadingActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_CLASS = "extra_target_class";
    public static final String EXTRA_STATUS_MESSAGES = "extra_status_messages";
    public static final String EXTRA_STATUS_MESSAGE = "extra_status_message";
    public static final String EXTRA_DURATION_MS = "extra_duration_ms";

    private ProgressBar progressBar;
    private TextView tvStatus;
    private int progressStatus = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String[] statusMessages;
    private long durationMs = 3000; // Default 3 seconds for splash
    private String mTargetClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.loadingProgress);
        tvStatus = findViewById(R.id.tvStatus);

        // Get transition data from Intent
        String targetClassNameRaw = getIntent().getStringExtra(EXTRA_TARGET_CLASS);
        statusMessages = getIntent().getStringArrayExtra(EXTRA_STATUS_MESSAGES);
        String singleMessage = getIntent().getStringExtra(EXTRA_STATUS_MESSAGE);
        durationMs = getIntent().getLongExtra(EXTRA_DURATION_MS, 3000L);

        // Handle target class from intent even if using old keys (compatibility with some existing code)
        if (targetClassNameRaw == null) {
            targetClassNameRaw = getIntent().getStringExtra("TARGET_CLASS");
        }

        mTargetClassName = targetClassNameRaw;

        ImageView ivLogo = findViewById(R.id.ivLoadingLogo);
        startLogoPopupAnimation(ivLogo);
        startPulseAnimation();

        if (statusMessages == null) {
            if (singleMessage != null) {
                statusMessages = new String[] { singleMessage };
            } else if (getIntent().getStringExtra("STATUS_MESSAGE") != null) {
                statusMessages = new String[] { getIntent().getStringExtra("STATUS_MESSAGE") };
            } else {
                statusMessages = new String[] {
                    "Connecting to area...",
                    "Finding neighbors...",
                    "Broadcasting...",
                    "Finalizing..."
                };
            }
        }

        long stepDelay = durationMs / 100;

        // Simulated progress logic
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;

                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    updateStatusText(progressStatus);
                });

                try {
                    Thread.sleep(stepDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // After completion, transition to target
            handler.postDelayed(() -> {
                try {
                    // Default target is WelcomeActivity (Splash -> Startup)
                    String finalTarget = (mTargetClassName != null) ? mTargetClassName : WelcomeActivity.class.getName();
                    Class<?> targetClass = Class.forName(finalTarget);
                    Intent nextIntent = new Intent(LoadingActivity.this, targetClass);

                    // Forward all other extras
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        nextIntent.putExtras(extras);
                    }

                    startActivity(nextIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } catch (ClassNotFoundException e) {
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    finish();
                }
            }, 500);

        }).start();
    }

    private void startPulseAnimation() {
        View ripple1 = findViewById(R.id.ripple1);
        View ripple2 = findViewById(R.id.ripple2);

        // First wave pulse
        setupPulse(ripple1, 0);
        // Second wave pulse (1s offset)
        setupPulse(ripple2, 1200);
    }

    private void setupPulse(View view, long delay) {
        // Core Animation: Scale outwards while fading
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.35f, 1.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.35f, 1.3f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.8f, 0f);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, alpha);
        pulseSet.setDuration(2400); // Pulse duration
        pulseSet.setStartDelay(delay);
        pulseSet.start();
    }

    private void startLogoPopupAnimation(View logo) {
        if (logo == null) return;

        // Initial state before popping
        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setAlpha(0f);

        // Core Popup Animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1.15f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1.15f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1.0f);

        AnimatorSet popupSet = new AnimatorSet();
        popupSet.playTogether(scaleX, scaleY, alpha);
        popupSet.setDuration(1200);
        popupSet.setInterpolator(new OvershootInterpolator(1.8f));
        popupSet.start();

        // Subtle continuous pulse after pop
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(logo, "scaleX", 1.0f, 1.06f, 1.0f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(logo, "scaleY", 1.0f, 1.06f, 1.0f);
        pulseX.setRepeatCount(ValueAnimator.INFINITE);
        pulseY.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet subtlePulseSet = new AnimatorSet();
        subtlePulseSet.playTogether(pulseX, pulseY);
        subtlePulseSet.setDuration(3500);
        subtlePulseSet.setStartDelay(1200);
        subtlePulseSet.start();
    }

    private void updateStatusText(int progress) {
        if (statusMessages.length == 0) return;
        int index = (progress * statusMessages.length) / 101;
        if (index < statusMessages.length) {
            tvStatus.setText(statusMessages[index]);
        }
    }
}
