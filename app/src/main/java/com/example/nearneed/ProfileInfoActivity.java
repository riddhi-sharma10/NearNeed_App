package com.example.nearneed;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileInfoActivity extends AppCompatActivity {

    private EditText etFullName, etBio, etDob;
    private TextView tvBioCount;
    private MaterialButton btnContinue;
    private ImageButton btnBack;
    private ImageView ivProfilePicture;
    private RelativeLayout flProfilePhoto;
    private ChipGroup cgGender;
    private android.widget.ScrollView scrollView;

    private Uri selectedPhotoUri = null;
    private String uploadedPhotoUrl = null;
    private boolean isUploading = false;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    uploadedPhotoUrl = null;
                    Glide.with(this).load(uri).circleCrop().into(ivProfilePicture);
                    UserPrefs.savePhotoUri(this, uri.toString());
                    startPhotoUpload(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        initViews();
        restoreSavedData();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etBio = findViewById(R.id.etBio);
        tvBioCount = findViewById(R.id.tvBioCount);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        flProfilePhoto = findViewById(R.id.flProfilePhoto);
        cgGender = findViewById(R.id.cgGender);
        etDob = findViewById(R.id.etDob);
        scrollView = findViewById(R.id.scrollView);
    }

    private void restoreSavedData() {
        etFullName.setText("");
        // Photo and fields start blank — UserPrefs was cleared before entering this activity.
        // Do not load from Firestore either, as that belongs to a previous registration.
    }

    private void startPhotoUpload(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        isUploading = true;
        btnContinue.setEnabled(false);
        btnContinue.setText("Uploading photo...");

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("profiles/" + user.getUid() + "/profile.jpg");

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                    ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                uploadedPhotoUrl = downloadUri.toString();
                                UserPrefs.savePhotoUri(this, uploadedPhotoUrl);
                                isUploading = false;
                                btnContinue.setEnabled(true);
                                btnContinue.setText("Continue");
                            })
                            .addOnFailureListener(e -> {
                                isUploading = false;
                                btnContinue.setEnabled(true);
                                btnContinue.setText("Continue");
                            })
                )
                .addOnFailureListener(e -> {
                    isUploading = false;
                    btnContinue.setEnabled(true);
                    btnContinue.setText("Continue");
                });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        flProfilePhoto.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        etDob.setOnClickListener(v -> showDatePicker());

        etBio.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.postDelayed(() -> scrollView.smoothScrollTo(0, etBio.getBottom() + 100), 300);
            }
        });

        etBio.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvBioCount.setText((s != null ? s.length() : 0) + "/150");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnContinue.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            String dob = etDob.getText().toString().trim();
            if (dob.isEmpty()) {
                Toast.makeText(this, "Please enter your Date of Birth. You must be 18+.", Toast.LENGTH_LONG).show();
                return;
            }
            if (isUploading) {
                Toast.makeText(this, "Please wait, uploading photo…", Toast.LENGTH_SHORT).show();
                return;
            }

            btnContinue.setEnabled(false);
            btnContinue.setText("Saving...");

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Not authenticated.", Toast.LENGTH_SHORT).show();
                btnContinue.setEnabled(true);
                btnContinue.setText("Continue");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("bio", etBio.getText().toString().trim());
            data.put("dob", etDob.getText().toString().trim());
            if (uploadedPhotoUrl != null) {
                data.put("profileImageUrl", uploadedPhotoUrl);
            }

            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        UserPrefs.saveName(this, name);
                        startActivity(new Intent(this, ProfileSetupActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnContinue.setEnabled(true);
                        btnContinue.setText("Continue");
                    });
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            
            Calendar eighteenYearsAgo = Calendar.getInstance();
            eighteenYearsAgo.add(Calendar.YEAR, -18);

            if (selected.after(eighteenYearsAgo)) {
                Toast.makeText(this, "You must be at least 18 years old to use NearNeed.", Toast.LENGTH_LONG).show();
                etDob.setText("");
            } else {
                etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        // Disable dates less than 18 years ago in the picker UI
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        dialog.show();
    }
}
