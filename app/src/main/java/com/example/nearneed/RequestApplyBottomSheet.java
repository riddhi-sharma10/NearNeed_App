package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RequestApplyBottomSheet extends BottomSheetDialogFragment {

    private EditText etMessage;
    private EditText etBudget;
    private MaterialCardView cardCash, cardUpi;
    private MaterialButton btnSubmit;
    private TextView tvCharCount;
    private String selectedPayment = "CASH";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_request_apply_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMessage = view.findViewById(R.id.et_request_message);
        etBudget = view.findViewById(R.id.et_request_budget);
        cardCash = view.findViewById(R.id.card_request_cash);
        cardUpi = view.findViewById(R.id.card_request_upi);
        btnSubmit = view.findViewById(R.id.btn_submit_request_application);
        tvCharCount = view.findViewById(R.id.tv_char_count);

        // Character counter for message
        if (etMessage != null) {
            etMessage.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int length = s.length();
                    if (tvCharCount != null) {
                        tvCharCount.setText(length + "/200");
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Cash card click
        if (cardCash != null) {
            cardCash.setOnClickListener(v -> {
                selectedPayment = "CASH";
                updatePaymentUI();
            });
        }

        // UPI card click
        if (cardUpi != null) {
            cardUpi.setOnClickListener(v -> {
                selectedPayment = "UPI";
                updatePaymentUI();
            });
        }

        // Submit button
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitApplication());
        }

        // Set initial UI state
        updatePaymentUI();
    }

    /**
     * Update payment method UI selection state.
     */
    private void updatePaymentUI() {
        if (cardCash != null) {
            if ("CASH".equals(selectedPayment)) {
                cardCash.setStrokeWidth(2);
                cardCash.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.sapphire_primary));
            } else {
                cardCash.setStrokeWidth(1);
                cardCash.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            }
        }

        if (cardUpi != null) {
            if ("UPI".equals(selectedPayment)) {
                cardUpi.setStrokeWidth(2);
                cardUpi.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.sapphire_primary));
            } else {
                cardUpi.setStrokeWidth(1);
                cardUpi.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            }
        }
    }

    /**
     * Submit the application with validation.
     */
    private void submitApplication() {
        String message = etMessage != null ? etMessage.getText().toString().trim() : "";
        String budget = etBudget != null ? etBudget.getText().toString().trim() : "";

        // Validation
        if (message.isEmpty()) {
            if (etMessage != null) {
                etMessage.setError("Please write why you want to apply (max 200 characters)");
            }
            return;
        }

        if (budget.isEmpty()) {
            if (etBudget != null) {
                etBudget.setError("Please enter your approximate budget");
            }
            return;
        }

        // All validations passed
        dismiss();
        showSuccessDialog();
    }

    /**
     * Show success dialog after application submission.
     */
    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Application Submitted!")
                .setMessage("Your application has been sent. The requester will review and get back to you.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
