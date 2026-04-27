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
import com.google.android.material.chip.ChipGroup;
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

        // ── Gender selection listener ──
        ChipGroup cgGender = findViewById(R.id.cgGender);
        if (cgGender != null) {
            cgGender.setOnCheckedChangeListener((group, checkedId) -> {
                // Ensure at least one chip is checked, or keep track of selection
            });
        }
        
        TextInputEditText etDob = findViewById(R.id.etDob);
        if (etDob != null) {
            etDob.setOnClickListener(v -> showDatePicker());
        }

        // ── Save Changes ──
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> saveChanges());
    }
    
    private void showDatePicker() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this, (view, year, month, day) -> {
            java.util.Calendar selected = java.util.Calendar.getInstance();
            selected.set(year, month, day);
            
            java.util.Calendar eighteenYearsAgo = java.util.Calendar.getInstance();
            eighteenYearsAgo.add(java.util.Calendar.YEAR, -18);

            TextInputEditText etDob = findViewById(R.id.etDob);
            if (selected.after(eighteenYearsAgo)) {
                Toast.makeText(this, "You must be at least 18 years old to use NearNeed.", Toast.LENGTH_LONG).show();
                if (etDob != null) etDob.setText("");
            } else {
                if (etDob != null) {
                    etDob.setText(String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
                }
            }
        }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH));

        // Disable dates less than 18 years ago in the picker UI
        java.util.Calendar maxDate = java.util.Calendar.getInstance();
        maxDate.add(java.util.Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        dialog.show();
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
    }

    private void loadFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .get()
            .addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) return;

                String name = snapshot.getString("fullName");
                TextInputEditText etFullName = findViewById(R.id.etFullName);
                if (name == null || name.isEmpty()) { name = snapshot.getString("name"); }
                if (name != null && etFullName != null) {
                    etFullName.setText(name);
                }

                String phone = snapshot.getString("phone");
                if (phone == null || phone.isEmpty()) {
                    phone = user.getPhoneNumber();
                }
                TextInputEditText etPhone = findViewById(R.id.etPhone);
                if (phone != null && etPhone != null) {
                    etPhone.setText(phone);
                }

                String dob = snapshot.getString("dob");
                TextInputEditText etDob = findViewById(R.id.etDob);
                if (dob != null && etDob != null) {
                    etDob.setText(dob);
                }

                String gender = snapshot.getString("gender");
                if (gender != null) {
                    if (gender.equalsIgnoreCase("Male")) {
                        Chip c = findViewById(R.id.chipMale);
                        if (c != null) c.setChecked(true);
                    } else if (gender.equalsIgnoreCase("Female")) {
                        Chip c = findViewById(R.id.chipFemale);
                        if (c != null) c.setChecked(true);
                    } else {
                        Chip c = findViewById(R.id.chipOther);
                        if (c != null) c.setChecked(true);
                    }
                }

                String bio = snapshot.getString("bio");
                if (bio != null && etBio != null) {
                    etBio.setText(bio);
                    updateBioCount(bio.length());
                }

                String photoUrl = snapshot.getString("profileImageUrl");
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
            
        TextInputEditText etPhone = findViewById(R.id.etPhone);
        String phone = etPhone != null && etPhone.getText() != null
            ? etPhone.getText().toString().trim() : "";

        TextInputEditText etDob = findViewById(R.id.etDob);
        String dob = etDob != null && etDob.getText() != null
            ? etDob.getText().toString().trim() : "";

        String gender = "Other";
        Chip chipMale = findViewById(R.id.chipMale);
        Chip chipFemale = findViewById(R.id.chipFemale);
        if (chipMale != null && chipMale.isChecked()) gender = "Male";
        else if (chipFemale != null && chipFemale.isChecked()) gender = "Female";

        String bio = etBio != null && etBio.getText() != null
            ? etBio.getText().toString().trim() : "";

        if (pendingPhotoUri != null) {
            uploadPhotoThenSave(user.getUid(), name, phone, dob, gender, bio);
        } else {
            persistToFirestore(user.getUid(), name, phone, dob, gender, bio, null);
        }
    }

    private void uploadPhotoThenSave(String uid, String name, String phone, String dob, String gender, String bio) {
        StorageReference ref = FirebaseStorage.getInstance()
            .getReference("profiles/" + uid + "/profile.jpg");

        ref.putFile(pendingPhotoUri)
            .addOnSuccessListener(task ->
                ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    UserPrefs.savePhotoUri(this, url);
                    persistToFirestore(uid, name, phone, dob, gender, bio, url);
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to get photo URL", Toast.LENGTH_SHORT).show()))
            .addOnFailureListener(e ->
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void persistToFirestore(String uid, String name, String phone, String dob, String gender, String bio, String photoUrl) {
        Map<String, Object> data = new HashMap<>();
        if (!name.isEmpty()) {
            data.put("fullName", name);
            UserPrefs.saveName(this, name);
        }
        data.put("phone", phone);
        data.put("dob", dob);
        data.put("gender", gender);
        data.put("bio", bio);
        if (photoUrl != null) {
            data.put("profileImageUrl", photoUrl);
        }

        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
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
}
