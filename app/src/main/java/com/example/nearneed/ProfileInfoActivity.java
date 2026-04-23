package com.example.nearneed;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.Locale;

public class ProfileInfoActivity extends AppCompatActivity {

    private EditText etFullName, etBio, etDob;
    private TextView tvBioCount;
    private MaterialButton btnContinue;
    private ImageButton btnBack;
    private ImageView ivProfilePicture;
    private RelativeLayout flProfilePhoto;
    private ChipGroup cgGender;
    private android.widget.ScrollView scrollView;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    ivProfilePicture.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        initViews();
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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        flProfilePhoto.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        etDob.setOnClickListener(v -> showDatePicker());

        etBio.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.postDelayed(() -> {
                    scrollView.smoothScrollTo(0, etBio.getBottom() + 100);
                }, 300);
            }
        });

        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s != null ? s.length() : 0;
                tvBioCount.setText(length + "/150");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnContinue.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            btnContinue.setEnabled(false);
            btnContinue.setText("Saving...");

            com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                java.util.Map<String, Object> user = new java.util.HashMap<>();
                user.put("fullName", name);
                user.put("bio", etBio.getText().toString().trim());
                user.put("dob", etDob.getText().toString().trim());

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("Users").document(uid)
                        .set(user, com.google.firebase.firestore.SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(ProfileInfoActivity.this, ProfileSetupActivity.class);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                btnContinue.setEnabled(true);
                                btnContinue.setText("Continue");
                            }
                        });
            } else {
                 Toast.makeText(this, "Not authenticated.", Toast.LENGTH_SHORT).show();
                 btnContinue.setEnabled(true);
                 btnContinue.setText("Continue");
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
            etDob.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }
}
