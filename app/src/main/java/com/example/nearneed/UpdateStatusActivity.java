package com.example.nearneed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.List;

public class UpdateStatusActivity extends AppCompatActivity {

    private String currentStatus = "pending";
    private String selectedStatus = "pending";
    private MaterialButton btnStatusPending, btnStatusOngoing, btnStatusCompleted, btnStatusCancelled;
    private MaterialButton btnSubmitStatus;
    private ImageView btnClose;
    private String bookingId;
    private String bookingTitle;

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

        // Initialize views
        btnStatusPending = findViewById(R.id.btnStatusProgress);
        btnStatusOngoing = findViewById(R.id.btnStatusOnHold);
        btnStatusCompleted = findViewById(R.id.btnStatusCompleted);
        btnStatusCancelled = findViewById(R.id.btnStatusCancelled);
        btnSubmitStatus = findViewById(R.id.btnSubmitStatus);
        btnClose = findViewById(R.id.btnClose);

        // Setup status button listeners
        setupStatusButtons();

        // Setup submit button
        btnSubmitStatus.setOnClickListener(v -> submitStatusUpdate());

        // Setup close button
        btnClose.setOnClickListener(v -> finish());
    }

    private void setupStatusButtons() {
        btnStatusPending.setOnClickListener(v -> selectStatus("pending", btnStatusPending));
        btnStatusOngoing.setOnClickListener(v -> selectStatus("ongoing", btnStatusOngoing));
        btnStatusCompleted.setOnClickListener(v -> selectStatus("completed", btnStatusCompleted));
        btnStatusCancelled.setOnClickListener(v -> selectStatus("cancelled", btnStatusCancelled));

        // Set initial state
        updateStatusButtonStyles();
    }

    private void selectStatus(String status, MaterialButton selectedButton) {
        selectedStatus = status;
        updateStatusButtonStyles();
    }

    private void updateStatusButtonStyles() {
        int mutedColor = ContextCompat.getColor(this, R.color.divider_subtle);
        int primaryColor = ContextCompat.getColor(this, R.color.sapphire_primary);
        int textMutedColor = ContextCompat.getColor(this, R.color.text_muted);

        // Reset all buttons
        btnStatusPending.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusOngoing.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusCompleted.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusCancelled.setStrokeColor(ColorStateList.valueOf(mutedColor));

        btnStatusPending.setTextColor(textMutedColor);
        btnStatusOngoing.setTextColor(textMutedColor);
        btnStatusCompleted.setTextColor(textMutedColor);
        btnStatusCancelled.setTextColor(textMutedColor);

        // Highlight selected button
        switch (selectedStatus) {
            case "pending":
                btnStatusPending.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusPending.setTextColor(primaryColor);
                break;
            case "ongoing":
                btnStatusOngoing.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusOngoing.setTextColor(primaryColor);
                break;
            case "completed":
                btnStatusCompleted.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusCompleted.setTextColor(primaryColor);
                break;
            case "cancelled":
                btnStatusCancelled.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusCancelled.setTextColor(primaryColor);
                break;
        }
    }



    private void submitStatusUpdate() {
        if (selectedStatus.equals(currentStatus)) {
            Toast.makeText(this, "Please select a different status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Persist in-memory state
        BookingStateManager.getInstance().setStatus(bookingId, selectedStatus);

        // If completed, navigate to payment flow
        if ("completed".equals(selectedStatus)) {
            navigateToPaymentFlow("");
            return;
        }

        // For cancelled / other statuses: send result back to caller
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
        intent.putExtra("provider_name", "Provider Name");
        intent.putExtra("service_amount", 500.0); // Default amount, can be customized
        intent.putExtra("completion_notes", completionNotes);
        startActivity(intent);
        finish();
    }

    private String formatStatusType(String statusType) {
        return statusType.replace("_", " ").substring(0, 1).toUpperCase() + statusType.replace("_", " ").substring(1);
    }
}
