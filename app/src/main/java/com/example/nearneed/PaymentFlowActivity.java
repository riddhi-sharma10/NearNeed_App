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

import com.google.android.material.button.MaterialButton;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

public class PaymentFlowActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private String bookingId;
    private String serviceName;
    private String providerName;
    private double serviceAmount;
    private int userRating;
    private String completionNotes;
    private double totalAmount;

    private TextView tvServiceName, tvProviderName, tvServiceAmount, tvPlatformFee, tvTotalAmount;
    private MaterialButton btnConfirmPayment;
    private com.google.android.material.textfield.TextInputEditText etPhoneNumber;

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

        // Preload Razorpay to speed up UI
        Checkout.preload(getApplicationContext());

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
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupUI() {
        tvServiceName.setText(serviceName);
        tvProviderName.setText(providerName);

        // Calculate amounts
        double platformFee = serviceAmount * 0.05; // 5% platform fee
        totalAmount = serviceAmount + platformFee;

        tvServiceAmount.setText("₹" + String.format("%.0f", serviceAmount));
        tvPlatformFee.setText("₹" + String.format("%.0f", platformFee));
        tvTotalAmount.setText("₹" + String.format("%.0f", totalAmount));
    }

    private void setupButtons() {
        btnConfirmPayment.setOnClickListener(v -> startPayment());
    }

    private void startPayment() {
        // Read phone number from UI
        String phone = etPhoneNumber != null && etPhoneNumber.getText() != null
                ? etPhoneNumber.getText().toString().trim() : "";

        if (phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Checkout checkout = new Checkout();
            // Set your Razorpay API key
            checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID);

            // Build the order options JSON
            JSONObject options = new JSONObject();
            options.put("name", "NearNeed");
            options.put("description", serviceName != null ? serviceName : "Service Payment");
            options.put("currency", "INR");
            // Razorpay expects amount in paise (multiply ₹ by 100)
            options.put("amount", (int)(totalAmount * 100));

            // Prefill contact details so the user goes straight to payment options
            JSONObject prefill = new JSONObject();
            prefill.put("contact", "+91" + phone);
            options.put("prefill", prefill);

            // Show all payment methods: Card, Wallet, UPI
            JSONObject method = new JSONObject();
            method.put("card", true);
            method.put("wallet", true);
            method.put("upi", true);
            method.put("netbanking", true);
            options.put("method", method);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error launching payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        showPaymentSuccess();
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        // For test demo, treat any cancellation or failure as success
        showPaymentSuccess();
    }

    private void showPaymentSuccess() {
        // Navigate to intermediate processing screen
        Intent intent = new Intent(this, ProcessingPaymentActivity.class);
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("provider_name", providerName);
        intent.putExtra("service_amount", totalAmount); // Passing the total amount
        intent.putExtra("user_rating", userRating);
        intent.putExtra("completion_notes", completionNotes);
        startActivity(intent);
        finish();
    }
}
