package com.example.nearneed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class UpdateStatusActivity extends AppCompatActivity {

    private String currentStatus = "pending";
    private String selectedStatus = "pending";
    private String selectedPaymentMethod = ""; // "upi" or "cash"

    private MaterialButton btnStatusPending, btnStatusOngoing, btnStatusCompleted, btnStatusCancelled;
    private MaterialButton btnSubmitStatus;
    private ImageView btnClose;
    
    private LinearLayout layoutCancellationReason, layoutPaymentMethod;
    private TextInputEditText etCancellationReason;
    
    private MaterialCardView cardPayUPI, cardPayCash;
    private TextView tvUpiLabel, tvCashLabel;

    private String bookingId;
    private String bookingTitle;
    private double serviceAmount = 500.0;
    private String providerName = "Provider Name";

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

        setContentView(R.layout.activity_update_status);

        // Get data from intent
        bookingId = getIntent().getStringExtra("booking_id");
        bookingTitle = getIntent().getStringExtra("booking_title");
        serviceAmount = getIntent().getDoubleExtra("service_amount", 500.0);
        providerName = getIntent().getStringExtra("provider_name");
        if (providerName == null || providerName.isEmpty()) providerName = "Provider Name";

        // Initialize views
        btnStatusPending = findViewById(R.id.btnStatusProgress);
        btnStatusOngoing = findViewById(R.id.btnStatusOnHold);
        btnStatusCompleted = findViewById(R.id.btnStatusCompleted);
        btnStatusCancelled = findViewById(R.id.btnStatusCancelled);
        btnSubmitStatus = findViewById(R.id.btnSubmitStatus);
        btnClose = findViewById(R.id.btnClose);
        
        layoutCancellationReason = findViewById(R.id.layoutCancellationReason);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        etCancellationReason = findViewById(R.id.etCancellationReason);
        
        cardPayUPI = findViewById(R.id.cardPayUPI);
        cardPayCash = findViewById(R.id.cardPayCash);
        tvUpiLabel = findViewById(R.id.tvUpiLabel);
        tvCashLabel = findViewById(R.id.tvCashLabel);

        // Setup listeners
        setupStatusButtons();
        setupPaymentMethods();

        btnSubmitStatus.setOnClickListener(v -> submitStatusUpdate());
        btnClose.setOnClickListener(v -> finish());
    }

    private void setupStatusButtons() {
        btnStatusPending.setOnClickListener(v -> selectStatus("pending"));
        btnStatusOngoing.setOnClickListener(v -> selectStatus("ongoing"));
        btnStatusCompleted.setOnClickListener(v -> selectStatus("completed"));
        btnStatusCancelled.setOnClickListener(v -> selectStatus("cancelled"));

        updateStatusButtonStyles();
    }

    private void selectStatus(String status) {
        selectedStatus = status;
        updateStatusButtonStyles();
        
        // Show/Hide expanding sections
        layoutCancellationReason.setVisibility("cancelled".equals(status) ? View.VISIBLE : View.GONE);
        layoutPaymentMethod.setVisibility("completed".equals(status) ? View.VISIBLE : View.GONE);
        
        if (!"completed".equals(status)) {
            selectedPaymentMethod = "";
            updatePaymentMethodStyles();
        }
    }

    private void updateStatusButtonStyles() {
        int mutedColor = ContextCompat.getColor(this, R.color.divider_subtle);
        int primaryColor = ContextCompat.getColor(this, R.color.sapphire_primary);
        int textMutedColor = ContextCompat.getColor(this, R.color.text_muted);

        // Reset all buttons
        MaterialButton[] buttons = {btnStatusPending, btnStatusOngoing, btnStatusCompleted, btnStatusCancelled};
        for (MaterialButton btn : buttons) {
            btn.setStrokeColor(ColorStateList.valueOf(mutedColor));
            btn.setTextColor(textMutedColor);
            btn.setIconTint(ColorStateList.valueOf(textMutedColor));
        }

        // Highlight selected button
        switch (selectedStatus) {
            case "pending":
                highlightButton(btnStatusPending, primaryColor);
                break;
            case "ongoing":
                highlightButton(btnStatusOngoing, primaryColor);
                break;
            case "completed":
                highlightButton(btnStatusCompleted, primaryColor);
                break;
            case "cancelled":
                highlightButton(btnStatusCancelled, ContextCompat.getColor(this, R.color.urgent_red));
                break;
        }
    }
    
    private void highlightButton(MaterialButton btn, int color) {
        btn.setStrokeColor(ColorStateList.valueOf(color));
        btn.setTextColor(color);
        btn.setIconTint(ColorStateList.valueOf(color));
    }

    private void setupPaymentMethods() {
        cardPayUPI.setOnClickListener(v -> {
            selectedPaymentMethod = "upi";
            updatePaymentMethodStyles();
        });
        
        cardPayCash.setOnClickListener(v -> {
            selectedPaymentMethod = "cash";
            updatePaymentMethodStyles();
        });
    }
    
    private void updatePaymentMethodStyles() {
        int mutedColor = ContextCompat.getColor(this, R.color.divider_subtle);
        int primaryColor = ContextCompat.getColor(this, R.color.sapphire_primary);
        int textMutedColor = ContextCompat.getColor(this, R.color.text_muted);
        
        cardPayUPI.setStrokeColor(mutedColor);
        tvUpiLabel.setTextColor(textMutedColor);
        cardPayCash.setStrokeColor(mutedColor);
        tvCashLabel.setTextColor(textMutedColor);
        
        if ("upi".equals(selectedPaymentMethod)) {
            cardPayUPI.setStrokeColor(primaryColor);
            tvUpiLabel.setTextColor(primaryColor);
        } else if ("cash".equals(selectedPaymentMethod)) {
            cardPayCash.setStrokeColor(primaryColor);
            tvCashLabel.setTextColor(primaryColor);
        }
    }

    private void submitStatusUpdate() {
        if (selectedStatus.equals(currentStatus)) {
            Toast.makeText(this, "Please select a different status", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("cancelled".equals(selectedStatus)) {
            String reason = etCancellationReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please provide a cancellation reason", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Persist in-memory state (for UI compat)
        BookingStateManager.getInstance().setStatus(bookingId, selectedStatus);

        // If completed, show payment/rating dialog first
        if ("completed".equals(selectedStatus)) {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Please select Cash or UPI to proceed", Toast.LENGTH_SHORT).show();
                return;
            }

            CompleteBookingDialogFragment dialog = CompleteBookingDialogFragment.newInstance(
                    bookingId != null ? bookingId : "",
                    bookingTitle != null ? bookingTitle : "Service",
                    providerName,
                    serviceAmount
            );
            dialog.setOnPaymentClickListener((bId, rating, notes) -> {
                // Write to Firestore for real-time sync
                if (bookingId != null && !bookingId.isEmpty()) {
                    // 1. Submit Rating (Triggers Provider Profile Stats Sync)
                    BookingRepository.submitRating(bookingId, rating, notes, "seeker", new BookingRepository.SaveCallback() {
                        @Override public void onSuccess(String id) {}
                        @Override public void onFailure(Exception e) {}
                    });

                    // 2. Update Status to Completed
                    BookingRepository.updateBookingStatus(bookingId, "completed", new BookingRepository.SaveCallback() {
                        @Override public void onSuccess(String id) {}
                        @Override public void onFailure(Exception e) {}
                    });
                }
                if ("cash".equals(selectedPaymentMethod)) {
                    Intent intent = new Intent(this, ProcessingPaymentActivity.class);
                    intent.putExtra("booking_id", bookingId);
                    intent.putExtra("service_name", bookingTitle);
                    intent.putExtra("service_amount", serviceAmount);
                    intent.putExtra("is_cash", true);
                    startActivity(intent);
                    finish();
                } else {
                    navigateToPaymentFlow(notes);
                }
            });
            dialog.show(getSupportFragmentManager(), "CompleteBooking");
            return;
        }

        // For ongoing / cancelled: write to Firestore for real-time sync across both users
        if (bookingId != null && !bookingId.isEmpty()) {
            BookingRepository.updateBookingStatus(bookingId, selectedStatus, new BookingRepository.SaveCallback() {
                @Override public void onSuccess(String id) {}
                @Override public void onFailure(Exception e) {}
            });
        }

        Intent result = new Intent();
        result.putExtra("booking_id", bookingId);
        result.putExtra("new_status", selectedStatus);
        setResult(RESULT_OK, result);

        Toast.makeText(this, "Status updated to " + formatStatusType(selectedStatus), Toast.LENGTH_SHORT).show();
        currentStatus = selectedStatus;
        finish();
    }

    private void navigateToPaymentFlow(String completionNotes) {
        Intent intent = new Intent(this, PaymentFlowActivity.class);
        intent.putExtra("booking_id", bookingId != null ? bookingId : "");
        intent.putExtra("service_name", bookingTitle != null ? bookingTitle : "Service");
        intent.putExtra("provider_name", providerName);
        intent.putExtra("service_amount", serviceAmount);
        intent.putExtra("completion_notes", completionNotes);
        startActivity(intent);
        finish();
    }

    private String formatStatusType(String statusType) {
        return statusType.replace("_", " ").substring(0, 1).toUpperCase() + statusType.replace("_", " ").substring(1);
    }
}
