package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PaymentFlowActivity extends AppCompatActivity {

    private String bookingId;
    private String serviceName;
    private String providerName;
    private double serviceAmount;
    private int userRating;
    private String completionNotes;

    private TextView tvServiceName, tvProviderName, tvServiceAmount, tvPlatformFee, tvTotalAmount;
    private RecyclerView rvPaymentMethods;
    private MaterialButton btnConfirmPayment;
    private int selectedPaymentMethodIndex = 0;

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

        setContentView(R.layout.activity_payment_flow);

        // Get data from intent
        extractIntentData();

        // Initialize views
        initializeViews();

        // Setup UI
        setupUI();

        // Setup payment methods
        setupPaymentMethods();

        // Setup buttons
        setupButtons();
    }

    private void extractIntentData() {
        bookingId = getIntent().getStringExtra("booking_id");
        serviceName = getIntent().getStringExtra("service_name");
        providerName = getIntent().getStringExtra("provider_name");
        serviceAmount = getIntent().getDoubleExtra("service_amount", 0);
        userRating = getIntent().getIntExtra("user_rating", 0);
        completionNotes = getIntent().getStringExtra("completion_notes");
    }

    private void initializeViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvServiceAmount = findViewById(R.id.tvServiceAmount);
        tvPlatformFee = findViewById(R.id.tvPlatformFee);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvPaymentMethods = findViewById(R.id.rvPaymentMethods);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupUI() {
        tvServiceName.setText(serviceName);
        tvProviderName.setText(providerName);

        // Calculate amounts
        double platformFee = serviceAmount * 0.05; // 5% platform fee
        double totalAmount = serviceAmount + platformFee;

        tvServiceAmount.setText("₹" + String.format("%.0f", serviceAmount));
        tvPlatformFee.setText("₹" + String.format("%.0f", platformFee));
        tvTotalAmount.setText("₹" + String.format("%.0f", totalAmount));
    }

    private void setupPaymentMethods() {
        List<PaymentMethod> paymentMethods = getSamplePaymentMethods();

        PaymentMethodAdapter adapter = new PaymentMethodAdapter(
            paymentMethods,
            position -> {
                selectedPaymentMethodIndex = position;
                // Update UI to reflect selection
            }
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPaymentMethods.setLayoutManager(layoutManager);
        rvPaymentMethods.setAdapter(adapter);
    }

    private List<PaymentMethod> getSamplePaymentMethods() {
        List<PaymentMethod> methods = new ArrayList<>();

        methods.add(new PaymentMethod(
            "1",
            "Credit Card",
            "CARD",
            "Visa - ****  ****  ****  4242",
            true,
            "ic_payment_wallet_blue"
        ));

        methods.add(new PaymentMethod(
            "2",
            "Google Pay",
            "WALLET",
            "nearneed@google.pay",
            false,
            "ic_payment_wallet_blue"
        ));

        methods.add(new PaymentMethod(
            "3",
            "Bank Transfer",
            "BANK_TRANSFER",
            "HDFC Bank - Savings Account",
            false,
            "ic_payment_wallet_blue"
        ));

        return methods;
    }

    private void setupButtons() {
        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Show loading state
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Processing Payment...");

        // Simulate payment processing
        new android.os.Handler().postDelayed(() -> {
            // Payment successful
            showPaymentSuccess();
        }, 2000);
    }

    private void showPaymentSuccess() {
        // Navigate to payment success/confirmation screen
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
}
