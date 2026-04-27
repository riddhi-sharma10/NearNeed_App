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

    private TextInputEditText etName, etPhone, etGender;
    private MaterialButton btnSaveChanges;

    private final Set<TextView> selectedCategories = new HashSet<>();
    private TextView selectedExperience = null;
    private final Set<TextView> selectedDays = new HashSet<>();
    private final Set<TextView> selectedTimeSlots = new HashSet<>();
    
    private TextView tvStartTime, tvEndTime;
    private EditText etServiceDescription;

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
        etGender = findViewById(R.id.etGender);
        etServiceDescription = findViewById(R.id.etServiceDescription);
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



        if(btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> {
                String newName = etName != null && etName.getText() != null ? etName.getText().toString().trim() : "";
                String newPhone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String newGender = etGender != null && etGender.getText() != null ? etGender.getText().toString().trim() : "";
                
                // Save to Firestore and UserPrefs
                saveToFirestore(newName, newPhone, newGender);
            });
        }
        
        setupCategories();
        setupExperience();
        setupDays();
        setupTimeSlots();
        setupTimePickers();
        
        preloadData();
    }

    private void preloadData() {
        // Preload from UserPrefs
        String cachedName = UserPrefs.getName(this);
        if (etName != null && !cachedName.isEmpty()) etName.setText(cachedName);

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Preload from Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String fullName = snapshot.getString("fullName");
                    if (fullName == null || fullName.isEmpty()) fullName = snapshot.getString("name");
                    String phone = snapshot.getString("phone");
                    if (phone == null || phone.isEmpty()) {
                        phone = user.getPhoneNumber();
                    }
                    String gender = snapshot.getString("gender");
                    if (fullName != null && etName != null) etName.setText(fullName);
                    if (phone != null && etPhone != null) etPhone.setText(phone);
                    if (gender != null && etGender != null) etGender.setText(gender);

                    java.util.List<String> cats = (java.util.List<String>) snapshot.get("categories");
                    if (cats != null) {
                        for (String cat : cats) {
                            int resId = getResources().getIdentifier(cat, "id", getPackageName());
                            if (resId != 0) {
                                TextView chip = findViewById(resId);
                                if (chip != null && !selectedCategories.contains(chip)) toggleCategory(chip);
                            }
                        }
                    }

                    String exp = snapshot.getString("experience");
                    if (exp != null) {
                        int resId = getResources().getIdentifier(exp, "id", getPackageName());
                        if (resId != 0) {
                            TextView chip = findViewById(resId);
                            if (chip != null) chip.performClick();
                        }
                    }

                    java.util.List<String> days = (java.util.List<String>) snapshot.get("days");
                    if (days != null) {
                        for (String day : days) {
                            int resId = getResources().getIdentifier(day, "id", getPackageName());
                            if (resId != 0) {
                                TextView chip = findViewById(resId);
                                if (chip != null && !selectedDays.contains(chip)) toggleDay(chip);
                            }
                        }
                    }

                    java.util.List<String> slots = (java.util.List<String>) snapshot.get("timeSlots");
                    if (slots != null) {
                        for (String slot : slots) {
                            int resId = getResources().getIdentifier(slot, "id", getPackageName());
                            if (resId != 0) {
                                TextView chip = findViewById(resId);
                                if (chip != null && !selectedTimeSlots.contains(chip)) {
                                    selectedTimeSlots.add(chip);
                                    chip.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                                    chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                                }
                            }
                        }
                    }

                    String startTime = snapshot.getString("startTime");
                    if (startTime != null && tvStartTime != null) tvStartTime.setText(startTime);
                    String endTime = snapshot.getString("endTime");
                    if (endTime != null && tvEndTime != null) tvEndTime.setText(endTime);

                    String bio = snapshot.getString("bio");
                    if (bio != null && etServiceDescription != null) etServiceDescription.setText(bio);
                }
            });
    }

    private void saveToFirestore(String name, String phone, String gender) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        if (!name.isEmpty()) data.put("fullName", name);
        if (!phone.isEmpty()) data.put("phone", phone);
        if (!gender.isEmpty()) data.put("gender", gender);

        java.util.List<String> catNames = new java.util.ArrayList<>();
        for (TextView tv : selectedCategories) catNames.add(getResources().getResourceEntryName(tv.getId()));
        data.put("categories", catNames);

        if (selectedExperience != null) {
            data.put("experience", getResources().getResourceEntryName(selectedExperience.getId()));
        }

        java.util.List<String> dayNames = new java.util.ArrayList<>();
        for (TextView tv : selectedDays) dayNames.add(getResources().getResourceEntryName(tv.getId()));
        data.put("days", dayNames);

        java.util.List<String> slotNames = new java.util.ArrayList<>();
        for (TextView tv : selectedTimeSlots) slotNames.add(getResources().getResourceEntryName(tv.getId()));
        data.put("timeSlots", slotNames);

        if (tvStartTime != null) data.put("startTime", tvStartTime.getText().toString());
        if (tvEndTime != null) data.put("endTime", tvEndTime.getText().toString());
        if (etServiceDescription != null) data.put("bio", etServiceDescription.getText().toString().trim());

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


    
    // --- Detailed Setup Methods ---
    
    private void setupCategories() {
        int[] chipIds = {
            R.id.chipPlumbing, R.id.chipElectrical, R.id.chipCarpentry,
            R.id.chipPainting, R.id.chipAppliance, R.id.chipCleaning,
            R.id.chipGardening, R.id.chipAssembly, R.id.chipOther
        };

        for (int id : chipIds) {
            TextView chip = findViewById(id);
            if(chip != null) {
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

        for (int id : expIds) {
            TextView exp = findViewById(id);
            if(exp != null) {
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

        for (int id : dayIds) {
            TextView day = findViewById(id);
            if(day != null) {
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

        for (int id : slotIds) {
            TextView slot = findViewById(id);
            if(slot != null) {
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
