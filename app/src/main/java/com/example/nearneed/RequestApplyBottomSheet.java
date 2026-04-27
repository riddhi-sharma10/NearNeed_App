package com.example.nearneed;

import android.content.res.ColorStateList;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

public class RequestApplyBottomSheet extends BottomSheetDialogFragment {

    private EditText etMessage;
    private Slider budgetSlider;
    private MaterialCardView cardCash, cardUpi;
    private MaterialButton btnSubmit;
    private TextView tvCharCount, tvBudgetValue;
    private String selectedPayment = "CASH";
    
    private String postId, postTitle, postType, creatorId;
    private ApplicationViewModel viewModel;

    public static RequestApplyBottomSheet newInstance(String postId, String title, String type, String creatorId) {
        RequestApplyBottomSheet fragment = new RequestApplyBottomSheet();
        Bundle args = new Bundle();
        args.putString("post_id", postId);
        args.putString("title", title);
        args.putString("type", type);
        args.putString("creator_id", creatorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_request_apply_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            postId = getArguments().getString("post_id");
            postTitle = getArguments().getString("title");
            postType = getArguments().getString("type");
            creatorId = getArguments().getString("creator_id");
        }

        viewModel = new ViewModelProvider(this).get(ApplicationViewModel.class);

        etMessage = view.findViewById(R.id.et_request_message);
        budgetSlider = view.findViewById(R.id.slider_request_budget);
        tvBudgetValue = view.findViewById(R.id.tv_request_budget_value);
        cardCash = view.findViewById(R.id.card_request_cash);
        cardUpi = view.findViewById(R.id.card_request_upi);
        btnSubmit = view.findViewById(R.id.btn_submit_request_application);
        tvCharCount = view.findViewById(R.id.tv_char_count);

        if (etMessage != null) {
            etMessage.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvCharCount != null) tvCharCount.setText(s.length() + "/200");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (budgetSlider != null && tvBudgetValue != null) {
            budgetSlider.addOnChangeListener((slider, value, fromUser) -> tvBudgetValue.setText("₹" + String.format("%.0f", value)));
        }

        if (cardCash != null) cardCash.setOnClickListener(v -> { selectedPayment = "CASH"; updatePaymentUI(); });
        if (cardUpi != null) cardUpi.setOnClickListener(v -> { selectedPayment = "UPI"; updatePaymentUI(); });
        if (btnSubmit != null) btnSubmit.setOnClickListener(v -> submitApplication());

        updatePaymentUI();

        if (postId != null) {
            ApplicationRepository.checkAlreadyApplied(postId, alreadyApplied -> {
                if (!alreadyApplied || btnSubmit == null) return;
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Already Applied");
                btnSubmit.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.text_muted)));
            });
        }
    }

    private void updatePaymentUI() {
        int sapphire = ContextCompat.getColor(requireContext(), R.color.sapphire_primary);
        int muted = ContextCompat.getColor(requireContext(), R.color.text_muted);
        
        if (cardCash != null) {
            cardCash.setStrokeWidth("CASH".equals(selectedPayment) ? 2 : 1);
            cardCash.setStrokeColor(ColorStateList.valueOf("CASH".equals(selectedPayment) ? sapphire : muted));
        }
        if (cardUpi != null) {
            cardUpi.setStrokeWidth("UPI".equals(selectedPayment) ? 2 : 1);
            cardUpi.setStrokeColor(ColorStateList.valueOf("UPI".equals(selectedPayment) ? sapphire : muted));
        }
    }

    private void submitApplication() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            etMessage.setError("Please write why you want to apply");
            return;
        }

        // ── Self-apply guard: same account cannot be both seeker and provider ────────────
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && currentUserId.equals(creatorId)) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Not Allowed")
                    .setMessage("You cannot apply to your own post. Please use a different account.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Applying...");

        String budget = (budgetSlider != null) ? "₹" + (int)budgetSlider.getValue() : "0";

        viewModel.submitApplication(postId, postTitle, postType, creatorId, message, budget, selectedPayment, new ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                dismiss();
                showSuccessDialog();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Application failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Apply Now");
            }
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Application Submitted!")
                .setMessage("Your application has been sent. The requester will review and get back to you.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
