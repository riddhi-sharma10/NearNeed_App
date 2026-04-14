package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CommunityVolunteerBottomSheet extends BottomSheetDialogFragment {

    private EditText etMessage;
    private MaterialButton btnSubmit;
    private TextView tvCharCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_community_volunteer_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMessage = view.findViewById(R.id.et_volunteer_message);
        btnSubmit = view.findViewById(R.id.btn_submit_volunteer);
        tvCharCount = view.findViewById(R.id.tv_volunteer_char_count);

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

        // Submit button
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitVolunteer());
        }
    }

    /**
     * Submit the volunteer application with validation.
     */
    private void submitVolunteer() {
        String message = etMessage != null ? etMessage.getText().toString().trim() : "";

        // Validation - only message is required
        if (message.isEmpty()) {
            if (etMessage != null) {
                etMessage.setError("Please write why you want to volunteer (max 200 characters)");
            }
            return;
        }

        // All validations passed
        dismiss();
        showSuccessDialog();
    }

    /**
     * Show success dialog after volunteer application submission.
     */
    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thank You!")
                .setMessage("Your volunteer request has been sent. The organizer will review and get back to you soon.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
