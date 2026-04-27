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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etBio;
    private TextView tvBioCount;
    private ShapeableImageView ivProfilePhoto;
    private TextView tvProfileInitials;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri pendingPhotoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvProfileInitials = findViewById(R.id.tvProfileInitials);
        etBio = findViewById(R.id.etBio);
        tvBioCount = findViewById(R.id.tvBioCount);

        preloadLocalData();
        loadFromFirestore();

        // ── Photo picker ──
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        pendingPhotoUri = selectedImage;
                        if (ivProfilePhoto != null) {
                            ivProfilePhoto.setImageURI(selectedImage);
                            ivProfilePhoto.setVisibility(View.VISIBLE);
                        }
                        if (tvProfileInitials != null) {
                            tvProfileInitials.setVisibility(View.GONE);
                        }
                    }
                }
            }
        );
        View photoContainer = findViewById(R.id.layoutProfilePhoto);
        if (photoContainer != null) {
            photoContainer.setOnClickListener(v -> openImagePicker());
        }
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);
        if (tvChangePhoto != null) {
            tvChangePhoto.setOnClickListener(v -> openImagePicker());
        }

        // ── Gender chips (read-only) ──
        Chip chipMale = findViewById(R.id.chipMale);
        if (chipMale != null) chipMale.setChecked(true);

        // ── Bio character counter ──
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
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> saveChanges());
    }

    private void preloadLocalData() {
        String cachedPhoto = UserPrefs.getPhotoUri(this);
        if (cachedPhoto != null && ivProfilePhoto != null) {
            Glide.with(this).load(cachedPhoto).circleCrop().into(ivProfilePhoto);
            ivProfilePhoto.setVisibility(View.VISIBLE);
            if (tvProfileInitials != null) tvProfileInitials.setVisibility(View.GONE);
        }

        String cachedName = UserPrefs.getName(this);
        TextInputEditText etFullName = findViewById(R.id.etFullName);
        if (etFullName != null && cachedName != null && !cachedName.isEmpty()) {
            etFullName.setText(cachedName);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        if (etEmail != null && user != null && user.getEmail() != null) {
            etEmail.setText(user.getEmail());
        }
    }

    private void loadFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
            .collection("Users").document(user.getUid())
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) return;

                String bio = snapshot.getString("bio");
                if (bio != null && etBio != null) {
                    etBio.setText(bio);
                    updateBioCount(bio.length());
                }

                String photoUrl = snapshot.getString("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty() && ivProfilePhoto != null
                        && pendingPhotoUri == null) {
                    Glide.with(this).load(photoUrl).circleCrop().into(ivProfilePhoto);
                    ivProfilePhoto.setVisibility(View.VISIBLE);
                    if (tvProfileInitials != null) tvProfileInitials.setVisibility(View.GONE);
                    UserPrefs.savePhotoUri(this, photoUrl);
                }
            });
    }

    private void saveChanges() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputEditText etFullName = findViewById(R.id.etFullName);
        String name = etFullName != null && etFullName.getText() != null
            ? etFullName.getText().toString().trim() : "";
        String bio = etBio != null && etBio.getText() != null
            ? etBio.getText().toString().trim() : "";

        if (pendingPhotoUri != null) {
            uploadPhotoThenSave(user.getUid(), name, bio);
        } else {
            persistToFirestore(user.getUid(), name, bio, null);
        }
    }

    private void uploadPhotoThenSave(String uid, String name, String bio) {
        StorageReference ref = FirebaseStorage.getInstance()
            .getReference("profile_photos/" + uid + ".jpg");

        ref.putFile(pendingPhotoUri)
            .addOnSuccessListener(task ->
                ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    UserPrefs.savePhotoUri(this, url);
                    persistToFirestore(uid, name, bio, url);
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to get photo URL", Toast.LENGTH_SHORT).show()))
            .addOnFailureListener(e ->
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void persistToFirestore(String uid, String name, String bio, String photoUrl) {
        Map<String, Object> data = new HashMap<>();
        if (!name.isEmpty()) {
            data.put("fullName", name);
            UserPrefs.saveName(this, name);
        }
        data.put("bio", bio);
        if (photoUrl != null) {
            data.put("photoUrl", photoUrl);
        }

        FirebaseFirestore.getInstance()
            .collection("Users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
