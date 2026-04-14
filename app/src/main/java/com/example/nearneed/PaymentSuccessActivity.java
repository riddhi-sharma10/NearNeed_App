package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PaymentSuccessActivity extends AppCompatActivity {

    private String bookingId;
    private String serviceName;
    private double serviceAmount;

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

        setContentView(R.layout.activity_payment_success);

        // Get data from intent
        extractIntentData();

        // Initialize views
        initializeViews();

        // Setup UI
        setupUI();

        // Setup buttons
        setupButtons();
    }

    private void extractIntentData() {
        bookingId = getIntent().getStringExtra("booking_id");
        serviceName = getIntent().getStringExtra("service_name");
        serviceAmount = getIntent().getDoubleExtra("service_amount", 0);
    }

    private void initializeViews() {
        TextView tvTransactionId = findViewById(R.id.tvTransactionId);
        TextView tvAmount = findViewById(R.id.tvAmount);
        TextView tvDateTime = findViewById(R.id.tvDateTime);
        MaterialButton btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);
        MaterialButton btnBackToHome = findViewById(R.id.btnBackToHome);

        // Generate transaction ID
        String transactionId = "#TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        tvTransactionId.setText(transactionId);

        // Set amount
        tvAmount.setText("₹" + String.format("%.0f", serviceAmount));

        // Set date and time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
        tvDateTime.setText(sdf.format(new Date()));

        // Setup button listeners
        btnDownloadReceipt.setOnClickListener(v -> downloadReceipt());
        btnBackToHome.setOnClickListener(v -> backToHome());
    }

    private void setupUI() {
        // Additional UI setup if needed
    }

    private void setupButtons() {
        // Additional button setup if needed
    }

    private void downloadReceipt() {
        // TODO: Implement receipt download functionality
        Toast.makeText(this, "Receipt download feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void backToHome() {
        // Navigate back to home and clear the back stack
        Intent intent = new Intent(this, HomeSeekerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Don't allow back navigation from success screen
        backToHome();
    }
}
