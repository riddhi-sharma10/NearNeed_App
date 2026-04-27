package com.example.nearneed;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EditProfileProviderActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail, etPassword;
    private MaterialButton btnSaveChanges;

    private final Set<TextView> selectedCategories = new HashSet<>();
    private TextView selectedExperience = null;
    private final Set<TextView> selectedDays = new HashSet<>();
    private final Set<TextView> selectedTimeSlots = new HashSet<>();
    
    private TextView tvStartTime, tvEndTime;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri imageUri = result.getData().getData();
                com.google.android.material.imageview.ShapeableImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
                TextView tvInitials = findViewById(R.id.tvProfileInitials);
                if (ivProfilePhoto != null && imageUri != null) {
                    ivProfilePhoto.setImageURI(imageUri);
                    ivProfilePhoto.setVisibility(View.VISIBLE);
                    if (tvInitials != null) {
                        tvInitials.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_provider);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        preloadData();

        // Photo Upload setup
        View photoContainer = findViewById(R.id.layoutProfilePhoto);
        if (photoContainer != null) {
            photoContainer.setOnClickListener(v -> openImagePicker());
        }
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);
        if (tvChangePhoto != null) {
            tvChangePhoto.setOnClickListener(v -> openImagePicker());
        }

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        if(btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> {
                String newName = etName.getText().toString().trim();
                String newPhone = etPhone.getText().toString().trim();
                
                android.content.SharedPreferences prefs = getSharedPreferences("ProviderProfile", MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                
                Set<String> catIds = new HashSet<>();
                for (TextView tv : selectedCategories) catIds.add(String.valueOf(tv.getId()));
                editor.putStringSet("categories", catIds);
                
                if (selectedExperience != null) {
                    editor.putInt("experience", selectedExperience.getId());
                }
                
                Set<String> dayIds = new HashSet<>();
                for (TextView tv : selectedDays) dayIds.add(String.valueOf(tv.getId()));
                editor.putStringSet("days", dayIds);
                
                Set<String> slotIds = new HashSet<>();
                for (TextView tv : selectedTimeSlots) slotIds.add(String.valueOf(tv.getId()));
                editor.putStringSet("timeSlots", slotIds);
                
                editor.apply();

                // Save to Firestore and UserPrefs
                saveToFirestore(newName, newPhone);
            });
        }
        
        setupCategories();
        setupExperience();
        setupDays();
        setupTimeSlots();
        setupTimePickers();
    }

    private void preloadData() {
        // Preload from UserPrefs
        String cachedName = UserPrefs.getName(this);
        if (etName != null && !cachedName.isEmpty()) etName.setText(cachedName);

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        if (etEmail != null && user.getEmail() != null) etEmail.setText(user.getEmail());

        // Preload from Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String fullName = snapshot.getString("name");
                    String phone = snapshot.getString("phone");
                    if (fullName != null && etName != null) etName.setText(fullName);
                    if (phone != null && etPhone != null) etPhone.setText(phone);
                }
            });
    }

    private void saveToFirestore(String name, String phone) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        if (!name.isEmpty()) data.put("name", name);
        if (!phone.isEmpty()) data.put("phone", phone);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(unused -> {
                if (!name.isEmpty()) UserPrefs.saveName(this, name);
                Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        TextInputEditText etForgotEmail = dialog.findViewById(R.id.etForgotEmail);

        EditText[] otpBoxes = {
            dialog.findViewById(R.id.fpOtpBox1),
            dialog.findViewById(R.id.fpOtpBox2),
            dialog.findViewById(R.id.fpOtpBox3),
            dialog.findViewById(R.id.fpOtpBox4),
            dialog.findViewById(R.id.fpOtpBox5),
            dialog.findViewById(R.id.fpOtpBox6)
        };

        // Auto-advance OTP boxes AND Auto-Verify on 6th digit
        for (int i = 0; i < otpBoxes.length; i++) {
            final int idx = i;
            if (otpBoxes[i] == null) continue;
            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        if (idx < otpBoxes.length - 1 && otpBoxes[idx + 1] != null) {
                            otpBoxes[idx + 1].requestFocus();
                        } else if (idx == otpBoxes.length - 1) {
                            // Automatically submit on 6th digit
                            verifyAndLaunchPasswordReset(dialog, otpBoxes);
                        }
                    } else if (s.length() == 0 && idx > 0 && otpBoxes[idx - 1] != null) {
                        otpBoxes[idx - 1].requestFocus();
                    }
                }
            });
        }

        // Step 1 – Send OTP (validate email format)
        dialog.findViewById(R.id.btnSendOtp).setOnClickListener(v -> {
            String email = etForgotEmail != null && etForgotEmail.getText() != null
                    ? etForgotEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                if (tilEmail != null) tilEmail.setError("Email is required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (tilEmail != null) tilEmail.setError("Enter a valid formatted email address");
                return;
            }
            if (tilEmail != null) tilEmail.setError(null);

            TextView tvSentTo = dialog.findViewById(R.id.tvOtpSentTo);
            if (tvSentTo != null) tvSentTo.setText("OTP sent to " + email);

            Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();
            vf.showNext();
            if (otpBoxes[0] != null) otpBoxes[0].requestFocus();
        });

        // Step 2 – Manual Verify Button Fallback
        dialog.findViewById(R.id.btnVerifyOtp).setOnClickListener(v -> {
            verifyAndLaunchPasswordReset(dialog, otpBoxes);
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

    private void verifyAndLaunchPasswordReset(Dialog dialog, EditText[] otpBoxes) {
        StringBuilder otp = new StringBuilder();
        for (EditText box : otpBoxes) {
            if (box != null && box.getText() != null) {
                otp.append(box.getText().toString().trim());
            }
        }
        if (otp.length() < 6) {
            Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.dismiss();
        Intent intent = new Intent(this, CreateNewPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    // --- Detailed Setup Methods ---
    
    private void setupCategories() {
        int[] chipIds = {
            R.id.chipPlumbing, R.id.chipElectrical, R.id.chipCarpentry,
            R.id.chipPainting, R.id.chipAppliance, R.id.chipCleaning,
            R.id.chipGardening, R.id.chipAssembly, R.id.chipOther
        };

        android.content.SharedPreferences prefs = getSharedPreferences("ProviderProfile", MODE_PRIVATE);
        Set<String> savedCategories = prefs.getStringSet("categories", new HashSet<>());

        for (int id : chipIds) {
            TextView chip = findViewById(id);
            if(chip != null) {
                // Pre-populate if logically saved
                if (savedCategories.contains(String.valueOf(id))) {
                    selectedCategories.add(chip);
                    chip.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
                chip.setOnClickListener(v -> toggleCategory((TextView) v));
            }
        }
    }

    private void toggleCategory(TextView chip) {
        if (selectedCategories.contains(chip)) {
            selectedCategories.remove(chip);
            chip.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
            chip.setTextColor(Color.BLACK);
        } else {
            selectedCategories.add(chip);
            chip.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setupExperience() {
        int[] expIds = { R.id.expLow, R.id.expMid, R.id.expHigh, R.id.expMax };

        android.content.SharedPreferences prefs = getSharedPreferences("ProviderProfile", MODE_PRIVATE);
        int savedExperience = prefs.getInt("experience", -1);

        for (int id : expIds) {
            TextView exp = findViewById(id);
            if(exp != null) {
                if (id == savedExperience) {
                    selectedExperience = exp;
                    exp.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                    exp.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
                exp.setOnClickListener(v -> {
                    if (selectedExperience != null) {
                        selectedExperience.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
                        selectedExperience.setTextColor(Color.BLACK);
                    }
                    selectedExperience = (TextView) v;
                    selectedExperience.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                    selectedExperience.setTextColor(ContextCompat.getColor(this, R.color.white));
                });
            }
        }
    }

    private void setupDays() {
        int[] dayIds = {
            R.id.daySun, R.id.dayMon, R.id.dayTue, R.id.dayWed,
            R.id.dayThu, R.id.dayFri, R.id.daySat
        };

        android.content.SharedPreferences prefs = getSharedPreferences("ProviderProfile", MODE_PRIVATE);
        Set<String> savedDays = prefs.getStringSet("days", new HashSet<>());

        for (int id : dayIds) {
            TextView day = findViewById(id);
            if(day != null) {
                if (savedDays.contains(String.valueOf(id))) {
                    selectedDays.add(day);
                    day.setBackgroundResource(R.drawable.bg_circle_selected_uniform);
                    day.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
                day.setOnClickListener(v -> toggleDay((TextView) v));
            }
        }
    }

    private void toggleDay(TextView day) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day);
            day.setBackgroundResource(R.drawable.bg_circle_unselected_uniform);
            day.setTextColor(Color.BLACK);
        } else {
            selectedDays.add(day);
            day.setBackgroundResource(R.drawable.bg_circle_selected_uniform);
            day.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setupTimeSlots() {
        int[] slotIds = {R.id.slotMorning, R.id.slotAfternoon, R.id.slotEvening};
        android.content.SharedPreferences prefs = getSharedPreferences("ProviderProfile", MODE_PRIVATE);
        Set<String> savedTimeSlots = prefs.getStringSet("timeSlots", new HashSet<>());

        for (int id : slotIds) {
            TextView slot = findViewById(id);
            if(slot != null) {
                if (savedTimeSlots.contains(String.valueOf(id))) {
                    selectedTimeSlots.add(slot);
                    slot.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                    slot.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
                slot.setOnClickListener(v -> {
                    TextView tv = (TextView) v;
                    if (selectedTimeSlots.contains(tv)) {
                        selectedTimeSlots.remove(tv);
                        tv.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
                        tv.setTextColor(Color.BLACK);
                    } else {
                        selectedTimeSlots.add(tv);
                        tv.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                        tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                    }
                });
            }
        }
    }

    private void setupTimePickers() {
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);

        if(tvStartTime != null) tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        if(tvEndTime != null) tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        // Let entire row hit
        if(tvStartTime != null && tvStartTime.getParent() != null) {
            ((View) tvStartTime.getParent()).setOnClickListener(v -> showTimePicker(tvStartTime));
        }
        if(tvEndTime != null && tvEndTime.getParent() != null) {
            ((View) tvEndTime.getParent()).setOnClickListener(v -> showTimePicker(tvEndTime));
        }
    }

    private void showTimePicker(TextView targetTextView) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> targetTextView.setText(formatTime(hourOfDay, minuteOfHour)),
                hour, minute, false);
        mTimePicker.show();
    }

    private String formatTime(int hourOfDay, int minute) {
        String amPm = "AM";
        int hour = hourOfDay;
        if (hourOfDay >= 12) {
            amPm = "PM";
            if (hourOfDay > 12) {
                hour -= 12;
            }
        }
        if (hour == 0) {
            hour = 12;
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
    }
}
