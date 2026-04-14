package com.example.nearneed;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class UpdateStatusActivity extends AppCompatActivity {

    private String currentStatus = "in_progress";
    private String selectedStatus = "in_progress";
    private MaterialButton btnStatusProgress, btnStatusOnHold, btnStatusCompleted, btnStatusCancelled;
    private MaterialButton btnSubmitStatus;
    private TextInputEditText etCompletionNotes;
    private MaterialCardView btnUploadPhoto;
    private MaterialCardView btnClose;
    private RecyclerView rvStatusHistory;
    private StatusHistoryAdapter statusHistoryAdapter;

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

        // Initialize views
        btnStatusProgress = findViewById(R.id.btnStatusProgress);
        btnStatusOnHold = findViewById(R.id.btnStatusOnHold);
        btnStatusCompleted = findViewById(R.id.btnStatusCompleted);
        btnStatusCancelled = findViewById(R.id.btnStatusCancelled);
        btnSubmitStatus = findViewById(R.id.btnSubmitStatus);
        etCompletionNotes = findViewById(R.id.etCompletionNotes);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnClose = findViewById(R.id.btnClose);
        rvStatusHistory = findViewById(R.id.rvStatusHistory);

        // Setup status button listeners
        setupStatusButtons();

        // Setup submit button
        btnSubmitStatus.setOnClickListener(v -> submitStatusUpdate());

        // Setup close button
        btnClose.setOnClickListener(v -> finish());

        // Setup photo upload
        btnUploadPhoto.setOnClickListener(v -> handlePhotoUpload());

        // Setup status history
        setupStatusHistory();

        // Load sample status history
        loadStatusHistory();
    }

    private void setupStatusButtons() {
        btnStatusProgress.setOnClickListener(v -> selectStatus("in_progress", btnStatusProgress));
        btnStatusOnHold.setOnClickListener(v -> selectStatus("on_hold", btnStatusOnHold));
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
        btnStatusProgress.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusOnHold.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusCompleted.setStrokeColor(ColorStateList.valueOf(mutedColor));
        btnStatusCancelled.setStrokeColor(ColorStateList.valueOf(mutedColor));

        btnStatusProgress.setTextColor(textMutedColor);
        btnStatusOnHold.setTextColor(textMutedColor);
        btnStatusCompleted.setTextColor(textMutedColor);
        btnStatusCancelled.setTextColor(textMutedColor);

        // Highlight selected button
        switch (selectedStatus) {
            case "in_progress":
                btnStatusProgress.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusProgress.setTextColor(primaryColor);
                break;
            case "on_hold":
                btnStatusOnHold.setStrokeColor(ColorStateList.valueOf(primaryColor));
                btnStatusOnHold.setTextColor(primaryColor);
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

    private void handlePhotoUpload() {
        // TODO: Implement photo upload using file picker or camera
        Toast.makeText(this, "Photo upload feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void setupStatusHistory() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvStatusHistory.setLayoutManager(layoutManager);
    }

    private void loadStatusHistory() {
        List<Status> statusHistory = new ArrayList<>();

        // Add sample status history in reverse chronological order
        statusHistory.add(new Status(
            "1", "booking_1", "in_progress",
            "Work started on the assigned task",
            null,
            System.currentTimeMillis() - 3600000,
            "Provider Name"
        ));

        statusHistory.add(new Status(
            "2", "booking_1", "not_started",
            "Booking confirmed and scheduled",
            null,
            System.currentTimeMillis() - 86400000,
            "System"
        ));

        statusHistoryAdapter = new StatusHistoryAdapter(statusHistory);
        rvStatusHistory.setAdapter(statusHistoryAdapter);
    }

    private void submitStatusUpdate() {
        String notes = etCompletionNotes.getText().toString().trim();

        if (selectedStatus.equals(currentStatus)) {
            Toast.makeText(this, "Please select a different status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate that completed or on-hold statuses have notes
        if (("completed".equals(selectedStatus) || "on_hold".equals(selectedStatus)) && notes.isEmpty()) {
            Toast.makeText(this, "Please add completion notes", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Submit status update to backend/database
        Toast.makeText(this, "Status updated to " + formatStatusType(selectedStatus), Toast.LENGTH_SHORT).show();

        // Update current status
        currentStatus = selectedStatus;

        // Reset form
        etCompletionNotes.setText("");

        finish();
    }

    private String formatStatusType(String statusType) {
        return statusType.replace("_", " ").substring(0, 1).toUpperCase() + statusType.replace("_", " ").substring(1);
    }
}
