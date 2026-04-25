package com.example.nearneed;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class CommunityPostStep2Activity extends AppCompatActivity {

    private int volunteerCount = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post_step2);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // STEPPER LOGIC
        android.widget.TextView tvCount = findViewById(R.id.tvVolunteerCount);
        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            volunteerCount++;
            tvCount.setText(String.valueOf(volunteerCount));
        });
        findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (volunteerCount > 1) {
                volunteerCount--;
                tvCount.setText(String.valueOf(volunteerCount));
            }
        });

        // TOGGLE LOGIC
        com.google.android.material.switchmaterial.SwitchMaterial switchLimit = findViewById(R.id.switchLimitVolunteers);
        switchLimit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Volunteer limit enabled" : "Volunteer limit disabled";
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });

        // DATE PICKER
        findViewById(R.id.layoutDate).setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                android.widget.TextView tv = findViewById(R.id.tvSelectedDate);
                tv.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                tv.setTextColor(0xFF0F172A);
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // TIME PICKER
        findViewById(R.id.layoutTime).setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            android.app.TimePickerDialog tpd = new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                android.widget.TextView tv = findViewById(R.id.tvSelectedTime);
                String ampm = hourOfDay >= 12 ? "PM" : "AM";
                int displayHour = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                tv.setText(String.format("%02d:%02d %s", displayHour, minute, ampm));
                tv.setTextColor(0xFF0F172A);
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false);
            tpd.show();
        });

        EditText searchEdit = findViewById(R.id.searchEditText);
        DashboardSearchHelper.bindMapSearchShortcut(searchEdit, this);

        // CONTINUE -> SUCCESS
        android.view.View successOverlay = findViewById(R.id.layout_success_overlay);
        findViewById(R.id.btnContinueStep2).setOnClickListener(v -> {
            if (successOverlay != null) {
                successOverlay.setVisibility(android.view.View.VISIBLE);
            }
        });

        findViewById(R.id.btnSuccessClose).setOnClickListener(v -> successOverlay.setVisibility(android.view.View.GONE));
        findViewById(R.id.btnBackHome).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, HomeProviderActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}
