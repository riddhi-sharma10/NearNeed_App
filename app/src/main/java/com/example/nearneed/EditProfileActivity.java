package com.example.nearneed;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etBio;
    private TextView tvBioCount;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Back arrow
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ── Photo picker ──
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        // Show the photo and hide the initials
                        com.google.android.material.imageview.ShapeableImageView ivPhoto =
                                findViewById(R.id.ivProfilePhoto);
                        android.widget.TextView tvInitials =
                                findViewById(R.id.tvProfileInitials);
                        if (ivPhoto != null) {
                            ivPhoto.setImageURI(selectedImage);
                            ivPhoto.setVisibility(android.view.View.VISIBLE);
                        }
                        if (tvInitials != null) {
                            tvInitials.setVisibility(android.view.View.GONE);
                        }
                        Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        android.view.View photoContainer = findViewById(R.id.layoutProfilePhoto);
        if (photoContainer != null) {
            photoContainer.setOnClickListener(v -> openImagePicker());
        }
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);
        if (tvChangePhoto != null) {
            tvChangePhoto.setOnClickListener(v -> openImagePicker());
        }

        // ── Gender chips (read-only – pre-select Male for demo) ──
        Chip chipMale = findViewById(R.id.chipMale);
        if (chipMale != null) chipMale.setChecked(true);

        // ── Bio character counter ──
        etBio = findViewById(R.id.etBio);
        tvBioCount = findViewById(R.id.tvBioCount);
        if (etBio != null && tvBioCount != null) {
            updateBioCount(etBio.getText() != null ? etBio.getText().toString().length() : 0);
            etBio.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    int len = s.length();
                    if (len > 150) {
                        etBio.setText(s.subSequence(0, 150));
                        etBio.setSelection(150);
                    } else {
                        updateBioCount(len);
                    }
                }
            });
        }

        // ── Forgot Password ──
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        // ── Save Changes ──
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateBioCount(int len) {
        if (tvBioCount != null) {
            tvBioCount.setText(len + "/150");
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_forgot_password);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ViewFlipper vf = dialog.findViewById(R.id.vfForgotPassword);
        TextInputLayout tilEmail = dialog.findViewById(R.id.tilForgotEmail);
        TextInputEditText etEmail = dialog.findViewById(R.id.etForgotEmail);

        EditText[] otpBoxes = {
            dialog.findViewById(R.id.fpOtpBox1),
            dialog.findViewById(R.id.fpOtpBox2),
            dialog.findViewById(R.id.fpOtpBox3),
            dialog.findViewById(R.id.fpOtpBox4),
            dialog.findViewById(R.id.fpOtpBox5),
            dialog.findViewById(R.id.fpOtpBox6)
        };

        // Auto-advance OTP boxes
        for (int i = 0; i < otpBoxes.length; i++) {
            final int idx = i;
            if (otpBoxes[i] == null) continue;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && idx < otpBoxes.length - 1 && otpBoxes[idx + 1] != null) {
                        otpBoxes[idx + 1].requestFocus();
                    } else if (s.length() == 0 && idx > 0 && otpBoxes[idx - 1] != null) {
                        otpBoxes[idx - 1].requestFocus();
                    }
                }
            });
        }

        // Step 1 – Send OTP (validate email)
        dialog.findViewById(R.id.btnSendOtp).setOnClickListener(v -> {
            String email = etEmail != null && etEmail.getText() != null
                    ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                if (tilEmail != null) tilEmail.setError("Email is required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (tilEmail != null) tilEmail.setError("Enter a valid email address");
                return;
            }
            if (tilEmail != null) tilEmail.setError(null);

            TextView tvSentTo = dialog.findViewById(R.id.tvOtpSentTo);
            if (tvSentTo != null) tvSentTo.setText("OTP sent to " + email);

            Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();
            vf.showNext();
            if (otpBoxes[0] != null) otpBoxes[0].requestFocus();
        });

        // Step 2 – Verify OTP (all 6 required)
        dialog.findViewById(R.id.btnVerifyOtp).setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (EditText box : otpBoxes) {
                if (box != null && box.getText() != null) {
                    otp.append(box.getText().toString().trim());
                }
            }
            if (otp.length() < 6) {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
                for (EditText box : otpBoxes) {
                    if (box != null && (box.getText() == null || box.getText().toString().isEmpty())) {
                        box.requestFocus();
                        break;
                    }
                }
                return;
            }
            dialog.dismiss();
            Intent intent = new Intent(this, CreateNewPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Resend OTP
        TextView tvResend = dialog.findViewById(R.id.tvResendOtp);
        if (tvResend != null) {
            tvResend.setOnClickListener(v -> {
                for (EditText box : otpBoxes) { if (box != null) box.setText(""); }
                if (otpBoxes[0] != null) otpBoxes[0].requestFocus();
                Toast.makeText(this, "OTP resent!", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }
}
