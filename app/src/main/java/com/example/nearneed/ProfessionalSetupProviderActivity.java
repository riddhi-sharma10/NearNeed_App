package com.example.nearneed;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import android.content.Intent;

public class ProfessionalSetupProviderActivity extends AppCompatActivity {

    private final Set<TextView> selectedCategories = new HashSet<>();
    private TextView selectedExperience = null;
    private final Set<TextView> selectedDays = new HashSet<>();
    private final Set<TextView> selectedTimeSlots = new HashSet<>();

    private TextView tvStartTime, tvEndTime;

    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_details);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupCategories();
        setupExperience();
        setupDays();
        setupTimeSlots();
        setupTimePickers();

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        CheckBox cbTerms = findViewById(R.id.cbTerms);
        View layoutSuccessOverlay = findViewById(R.id.layout_success_overlay);

        btnContinue.setOnClickListener(v -> {
            boolean isValid = true;
            
            TextView tvCategoryError = findViewById(R.id.tvCategoryError);
            TextView tvExperienceError = findViewById(R.id.tvExperienceError);
            TextView tvDaysError = findViewById(R.id.tvDaysError);
            TextView tvTimeSlotsError = findViewById(R.id.tvTimeSlotsError);

            if (selectedCategories.isEmpty()) {
                if (tvCategoryError != null) tvCategoryError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                if (tvCategoryError != null) tvCategoryError.setVisibility(View.GONE);
            }

            if (selectedExperience == null) {
                if (tvExperienceError != null) tvExperienceError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                if (tvExperienceError != null) tvExperienceError.setVisibility(View.GONE);
            }

            if (selectedDays.isEmpty()) {
                if (tvDaysError != null) tvDaysError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                if (tvDaysError != null) tvDaysError.setVisibility(View.GONE);
            }

            if (selectedTimeSlots.isEmpty()) {
                if (tvTimeSlotsError != null) tvTimeSlotsError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                if (tvTimeSlotsError != null) tvTimeSlotsError.setVisibility(View.GONE);
            }

            if (!isValid) {
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to the Terms first.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Save to SharedPreferences
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

            // SHOW THE OVERLAP MODAL
            if (layoutSuccessOverlay != null) {
                layoutSuccessOverlay.setVisibility(View.VISIBLE);
                
                // BACK TO HOME BUTTON IN MODAL
                MaterialButton btnSuccessHome = findViewById(R.id.btnSuccessHome);
                if (btnSuccessHome != null) {
                    btnSuccessHome.setOnClickListener(v2 -> {
                        Intent intent = new Intent(this, HomeProviderActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finishAffinity();
                    });
                }
                
                // VIEW PROFILE BUTTON IN MODAL
                MaterialButton btnSuccessProfile = findViewById(R.id.btnSuccessProfile);
                if (btnSuccessProfile != null) {
                    btnSuccessProfile.setOnClickListener(v2 -> {
                        Intent intent = new Intent(this, ProfileActivity.class);
                        startActivity(intent);
                    });
                }

                // CLOSE BUTTON IN MODAL
                View btnSuccessClose = findViewById(R.id.btnSuccessClose);
                if (btnSuccessClose != null) {
                    btnSuccessClose.setOnClickListener(v2 -> {
                        layoutSuccessOverlay.setVisibility(View.GONE);
                    });
                }
            } else {
                // Fallback if overlay not found
                Intent intent = new Intent(this, HomeProviderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAffinity();
            }
        });
    }

    private void setupCategories() {
        int[] chipIds = {
            R.id.chipPlumbing, R.id.chipElectrical, R.id.chipCarpentry,
            R.id.chipPainting, R.id.chipAppliance, R.id.chipCleaning,
            R.id.chipGardening, R.id.chipAssembly, R.id.chipOther
        };

        for (int id : chipIds) {
            TextView chip = findViewById(id);
            chip.setOnClickListener(v -> toggleCategory((TextView) v));
        }
    }

    private void toggleCategory(TextView chip) {
        if (selectedCategories.contains(chip)) {
            selectedCategories.remove(chip);
            chip.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
            chip.setTextColor(android.graphics.Color.BLACK);
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
            exp.setOnClickListener(v -> {
                if (selectedExperience != null) {
                    selectedExperience.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
                    selectedExperience.setTextColor(android.graphics.Color.BLACK);
                }
                selectedExperience = (TextView) v;
                selectedExperience.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                selectedExperience.setTextColor(ContextCompat.getColor(this, R.color.white));
            });
        }
    }

    private void setupDays() {
        int[] dayIds = {
            R.id.daySun, R.id.dayMon, R.id.dayTue, R.id.dayWed,
            R.id.dayThu, R.id.dayFri, R.id.daySat
        };

        for (int id : dayIds) {
            TextView day = findViewById(id);
            // Add default selected from layout (Mon-Fri)
            if (id != R.id.daySun && id != R.id.daySat) {
                selectedDays.add(day);
            }
            day.setOnClickListener(v -> toggleDay((TextView) v));
        }
    }

    private void toggleDay(TextView day) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day);
            day.setBackgroundResource(R.drawable.bg_circle_unselected_uniform);
            day.setTextColor(android.graphics.Color.BLACK);
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
            slot.setOnClickListener(v -> {
                TextView tv = (TextView) v;
                if (selectedTimeSlots.contains(tv)) {
                    selectedTimeSlots.remove(tv);
                    tv.setBackgroundResource(R.drawable.bg_chip_unselected_uniform);
                    tv.setTextColor(android.graphics.Color.BLACK);
                } else {
                    selectedTimeSlots.add(tv);
                    tv.setBackgroundResource(R.drawable.bg_chip_selected_uniform);
                    tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
            });
        }
    }

    private void setupTimePickers() {
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);

        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        // Also make parent layouts clickable
        View parentStartTime = (View) tvStartTime.getParent();
        View parentEndTime = (View) tvEndTime.getParent();
        if (parentStartTime != null) parentStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        if (parentEndTime != null) parentEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));
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
