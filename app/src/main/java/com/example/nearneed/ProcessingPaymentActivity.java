package com.example.nearneed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ProcessingPaymentActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView ivSuccessCheck;
    private TextView tvStatusText, tvSubStatusText;

    private String bookingId;
    private String serviceName;
    private String providerName;
    private double serviceAmount;
    private int userRating;
    private String completionNotes;
    private boolean isCashPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Premium transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        window.getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_processing_payment);

        progressBar = findViewById(R.id.progressBar);
        ivSuccessCheck = findViewById(R.id.ivSuccessCheck);
        tvStatusText = findViewById(R.id.tvStatusText);
        tvSubStatusText = findViewById(R.id.tvSubStatusText);

        extractIntentData();

        if (isCashPayment) {
            tvStatusText.setText("Confirming Booking...");
        }

        startProcessingSimulation();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        bookingId = intent.getStringExtra("booking_id");
        serviceName = intent.getStringExtra("service_name");
        providerName = intent.getStringExtra("provider_name");
        serviceAmount = intent.getDoubleExtra("service_amount", 0);
        userRating = intent.getIntExtra("user_rating", 0);
        completionNotes = intent.getStringExtra("completion_notes");
        isCashPayment = intent.getBooleanExtra("is_cash", false);
    }

    private void startProcessingSimulation() {
        // Simulate processing delay (2 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showSuccessAnimation();
        }, 2000);
    }

    private void showSuccessAnimation() {
        // Hide progress bar
        progressBar.setVisibility(View.GONE);
        
        // Show checkmark
        ivSuccessCheck.setVisibility(View.VISIBLE);
        ivSuccessCheck.setScaleX(0f);
        ivSuccessCheck.setScaleY(0f);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivSuccessCheck, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivSuccessCheck, "scaleY", 0f, 1f);
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        
        OvershootInterpolator interpolator = new OvershootInterpolator(2f);
        scaleX.setInterpolator(interpolator);
        scaleY.setInterpolator(interpolator);
        
        scaleX.start();
        scaleY.start();

        // Change text
        tvStatusText.setText("Payment Successful!");
        tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.brand_success_vibrant));
        tvSubStatusText.setText("Redirecting to receipt...");

        // Wait another 1.5 seconds then navigate to actual success screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToFinalSuccessScreen();
        }, 1500);
    }

    private void navigateToFinalSuccessScreen() {
        Intent intent = new Intent(this, PaymentSuccessActivity.class);
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("provider_name", providerName);
        intent.putExtra("service_amount", serviceAmount);
        intent.putExtra("user_rating", userRating);
        intent.putExtra("completion_notes", completionNotes);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent backing out during processing
    }
}
