package com.example.nearneed;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreatePostStep2Activity extends AppCompatActivity {

    private TextView tvNotesCharCount, tvDisplayDate, tvDisplayTime, tvLocationSearch;
    private EditText etAdditionalNotes;
    private MaterialButton btnPostRequest;
    
    private List<MaterialCardView> urgencyCards = new ArrayList<>();
    private List<TextView> urgencyTexts = new ArrayList<>();
    private List<ImageView> urgencyIcons = new ArrayList<>();
    private String selectedUrgency = "Urgent"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_step2);

        initViews();
        setupListeners();
        updatePostButtonState();
    }

    private void initViews() {
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        tvNotesCharCount = findViewById(R.id.tvNotesCharCount);
        tvDisplayDate = findViewById(R.id.tvDisplayDate);
        tvDisplayTime = findViewById(R.id.tvDisplayTime);
        tvLocationSearch = findViewById(R.id.tvLocationSearch);
        btnPostRequest = findViewById(R.id.btnPostRequest);

        urgencyCards.add(findViewById(R.id.cardUrgencyNow));
        urgencyCards.add(findViewById(R.id.cardUrgencyToday));
        urgencyCards.add(findViewById(R.id.cardUrgencyWeek));
        urgencyCards.add(findViewById(R.id.cardUrgencyFlexible));

        urgencyTexts.add(findViewById(R.id.tvUrgencyNow));
        urgencyTexts.add(findViewById(R.id.tvUrgencyToday));
        urgencyTexts.add(findViewById(R.id.tvUrgencyWeek));
        urgencyTexts.add(findViewById(R.id.tvUrgencyFlexible));

        urgencyIcons.add(findViewById(R.id.ivUrgencyNow));
        urgencyIcons.add(findViewById(R.id.ivUrgencyToday));
        urgencyIcons.add(findViewById(R.id.ivUrgencyWeek));
        urgencyIcons.add(findViewById(R.id.ivUrgencyFlexible));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        for (int i = 0; i < urgencyCards.size(); i++) {
            final int index = i;
            urgencyCards.get(i).setOnClickListener(v -> selectUrgency(index));
        }

        findViewById(R.id.llLocationSearch).setOnClickListener(v -> {
            LocationPickerHelper.show(this, displayText -> {
                String loc = displayText;
                if (loc.startsWith("DELIVER TO: ")) {
                    loc = loc.substring("DELIVER TO: ".length());
                }
                tvLocationSearch.setText(loc);
                tvLocationSearch.setTextColor(Color.parseColor("#111827"));
                updatePostButtonState();
            });
        });

        findViewById(R.id.containerSetDate).setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String selectedDate = sdf.format(calendar.getTime());
                tvDisplayDate.setText(selectedDate);
                tvDisplayDate.setTextColor(Color.parseColor("#111827"));
                updatePostButtonState();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        findViewById(R.id.containerSetTime).setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Time Window")
                    .build();

            timePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = (hour >= 12) ? "PM" : "AM";
                int displayHour = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
                
                // Typically post creation might want a window, but a single time is a good start. 
                // Let's just set the selected time.
                tvDisplayTime.setText(selectedTime);
                tvDisplayTime.setTextColor(Color.parseColor("#111827"));
                updatePostButtonState();
            });

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });

        etAdditionalNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvNotesCharCount.setText(length + "/250");
                if (length >= 250) {
                    tvNotesCharCount.setTextColor(Color.RED);
                } else {
                    tvNotesCharCount.setTextColor(Color.parseColor("#94A3B8"));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnPostRequest.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostedSuccessfullyActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void selectUrgency(int index) {
        for (int i = 0; i < urgencyCards.size(); i++) {
            urgencyCards.get(i).setStrokeColor(Color.parseColor("#E2E8F0"));
            urgencyCards.get(i).setCardBackgroundColor(Color.WHITE);
            urgencyTexts.get(i).setTextColor(Color.parseColor("#111827"));
            urgencyIcons.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
        }

        MaterialCardView selected = urgencyCards.get(index);
        selected.setStrokeColor(Color.TRANSPARENT);
        selected.setCardBackgroundColor(Color.parseColor("#1E3A8A"));
        urgencyTexts.get(index).setTextColor(Color.WHITE);
        urgencyIcons.get(index).setImageTintList(ColorStateList.valueOf(Color.WHITE));

        selectedUrgency = urgencyTexts.get(index).getText().toString();
        updatePostButtonState();
    }

    private void updatePostButtonState() {
        boolean isDateSet = !tvDisplayDate.getText().toString().contains("Set Date");
        boolean isTimeSet = !tvDisplayTime.getText().toString().contains("Set Time");
        boolean isLocationSet = !tvLocationSearch.getText().toString().contains("Search address");
        
        boolean isEnabled = isDateSet && isTimeSet && isLocationSet;
        btnPostRequest.setEnabled(isEnabled);
        
        if (isEnabled) {
            btnPostRequest.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
            btnPostRequest.setAlpha(1.0f);
        } else {
            btnPostRequest.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
            btnPostRequest.setAlpha(0.6f);
        }
    }
}

