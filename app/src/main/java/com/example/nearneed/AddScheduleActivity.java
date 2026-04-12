package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AddScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnAddSchedule = findViewById(R.id.btnAddSchedule);

        btnBack.setOnClickListener(v -> finish());

        // DATE PICKER
        findViewById(R.id.layoutDate).setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                android.widget.TextView tv = findViewById(R.id.tvSelectedDate);
                tv.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                tv.setTextColor(0xFF1E293B);
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        // TIME PICKER
        findViewById(R.id.layoutTime).setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                android.widget.TextView tv = findViewById(R.id.tvSelectedTime);
                String ampm = hourOfDay >= 12 ? "PM" : "AM";
                int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                tv.setText(String.format("%02d:%02d %s", displayHour, minute, ampm));
                tv.setTextColor(0xFF1E293B);
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show();
        });

        // CHIP SELECTION LOGIC
        android.view.View chipPremium = findViewById(R.id.chipPremiumTask);
        android.view.View chipCommunity = findViewById(R.id.chipCommunityHelp);

        chipPremium.setOnClickListener(v -> {
            updateChipState(chipPremium, true, "#EFF6FF", "#2563EB");
            updateChipState(chipCommunity, false, "#F1F5F9", "#64748B");
        });

        chipCommunity.setOnClickListener(v -> {
            updateChipState(chipCommunity, true, "#FFF7ED", "#EA580C");
            updateChipState(chipPremium, false, "#F1F5F9", "#64748B");
        });

        // Initialize with Premium selected
        updateChipState(chipPremium, true, "#EFF6FF", "#2563EB");
        updateChipState(chipCommunity, false, "#F1F5F9", "#64748B");

        btnAddSchedule.setOnClickListener(v -> {
            Toast.makeText(this, "Schedule Item Added Successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateChipState(android.view.View chip, boolean selected, String activeBg, String activeColor) {
        int color = selected ? android.graphics.Color.parseColor(activeColor) : android.graphics.Color.parseColor("#64748B");
        int bgColor = selected ? android.graphics.Color.parseColor(activeBg) : android.graphics.Color.parseColor("#F1F5F9");
        
        chip.getBackground().setTint(bgColor);
        
        android.view.View dot = chip.findViewWithTag("dot") != null ? chip.findViewWithTag("dot") : chip.getChildAt(0);
        dot.getBackground().setTint(color);
        
        android.widget.TextView tv = (android.widget.TextView) chip.getChildAt(1);
        tv.setTextColor(color);
        tv.setAlpha(selected ? 1.0f : 0.6f);
    }
}
