package com.example.nearneed;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class ProfessionalSetupProviderActivity extends AppCompatActivity {

    private final Set<TextView> selectedCategories = new HashSet<>();
    private TextView selectedExperience = null;
    private final Set<TextView> selectedDays = new HashSet<>();

    private TextView tvStartTime, tvEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_setup_provider);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupCategories();
        setupExperience();
        setupDays();
        setupTimePickers();

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        CheckBox cbTerms = findViewById(R.id.cbTerms);

        btnContinue.setOnClickListener(v -> {
            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to the Terms first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExperience == null || selectedCategories.isEmpty() || selectedDays.isEmpty()) {
                Toast.makeText(this, "Please complete your availability, experience, and services.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Setup Complete!", Toast.LENGTH_SHORT).show();
            // Proceed to next activity
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
            // Default select Plumbing
            if (id == R.id.chipPlumbing) {
                toggleCategory(chip);
            }
            chip.setOnClickListener(v -> toggleCategory((TextView) v));
        }
    }

    private void toggleCategory(TextView chip) {
        if (selectedCategories.contains(chip)) {
            selectedCategories.remove(chip);
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_header));
        } else {
            selectedCategories.add(chip);
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setupExperience() {
        int[] expIds = { R.id.expLow, R.id.expMid, R.id.expHigh, R.id.expMax };

        for (int id : expIds) {
            TextView exp = findViewById(id);
            if (id == R.id.expMid) {
                selectedExperience = exp; // Initial default from layout
            }
            exp.setOnClickListener(v -> {
                if (selectedExperience != null) {
                    selectedExperience.setBackgroundResource(R.drawable.bg_chip_unselected);
                    selectedExperience.setTextColor(ContextCompat.getColor(this, R.color.text_header));
                }
                selectedExperience = (TextView) v;
                selectedExperience.setBackgroundResource(R.drawable.bg_chip_selected);
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
            day.setBackgroundResource(R.drawable.bg_circle_light_blue);
            day.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
        } else {
            selectedDays.add(day);
            day.setBackgroundResource(R.drawable.bg_circle_blue_solid);
            day.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void setupTimePickers() {
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);

        View.OnClickListener timeClickListener = v -> {
            TextView tv = (TextView) v;
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker = new TimePickerDialog(this,
                    R.style.Theme_NearNeed, // Using default theme; adjust if an explicit dialog theme exists
                    (view, hourOfDay, minuteOfHour) -> tv.setText(formatTime(hourOfDay, minuteOfHour)),
                    hour, minute, false);
            mTimePicker.show();
        };

        tvStartTime.setOnClickListener(timeClickListener);
        tvEndTime.setOnClickListener(timeClickListener);

        // Also make parent layouts clickable
        View parentStartTime = (View) tvStartTime.getParent();
        View parentEndTime = (View) tvEndTime.getParent();
        if (parentStartTime != null) parentStartTime.setOnClickListener(timeClickListener);
        if (parentEndTime != null) parentEndTime.setOnClickListener(timeClickListener);
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
