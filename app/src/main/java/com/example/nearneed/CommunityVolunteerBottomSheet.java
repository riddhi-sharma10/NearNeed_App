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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CommunityVolunteerBottomSheet extends BottomSheetDialogFragment {

    private EditText etMessage;
    private MaterialButton btnSubmit;
    private TextView tvCharCount;

    private String postId, postTitle, postType, creatorId;
    private ApplicationViewModel viewModel;

    public static CommunityVolunteerBottomSheet newInstance(String postId, String title,
                                                             String type, String creatorId) {
        CommunityVolunteerBottomSheet fragment = new CommunityVolunteerBottomSheet();
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
        return inflater.inflate(R.layout.layout_community_volunteer_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            postId    = getArguments().getString("post_id");
            postTitle = getArguments().getString("title");
            postType  = getArguments().getString("type");
            creatorId = getArguments().getString("creator_id");
        }

        viewModel = new ViewModelProvider(this).get(ApplicationViewModel.class);

        etMessage  = view.findViewById(R.id.et_volunteer_message);
        btnSubmit  = view.findViewById(R.id.btn_submit_volunteer);
        tvCharCount = view.findViewById(R.id.tv_volunteer_char_count);

        if (etMessage != null) {
            etMessage.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvCharCount != null) tvCharCount.setText(s.length() + "/200");
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitVolunteer());
        }

        if (postId != null) {
            ApplicationRepository.checkAlreadyApplied(postId, alreadyApplied -> {
                if (!alreadyApplied || btnSubmit == null) return;
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Already Volunteered");
                btnSubmit.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.text_muted)));
            });
        }
    }

    private void submitVolunteer() {
        String message = etMessage != null ? etMessage.getText().toString().trim() : "";
        if (message.isEmpty()) {
            if (etMessage != null) etMessage.setError("Please write why you want to volunteer");
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        viewModel.submitApplication(postId, postTitle, postType, creatorId, message,
                null, null, new ApplicationRepository.SaveCallback() {
                    @Override
                    public void onSuccess(String id) {
                        dismiss();
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Thank You!")
                                .setMessage("Your volunteer request has been sent. The organizer will review and get back to you soon.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    }
                });
    }
}
