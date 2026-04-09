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
import com.google.android.material.slider.RangeSlider;
import java.util.ArrayList;
import java.util.List;

public class CreatePostStep2Activity extends AppCompatActivity {

    private TextView tvBudgetDisplay, tvNotesCharCount, tvDisplayDate, tvDisplayTime;
    private RangeSlider budgetSlider;
    private EditText etAdditionalNotes;
    private MaterialButton btnPostRequest;
    
    private List<MaterialCardView> urgencyCards = new ArrayList<>();
    private List<TextView> urgencyTexts = new ArrayList<>();
    private List<ImageView> urgencyIcons = new ArrayList<>();
    private String selectedUrgency = "Now"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_step2);

        initViews();
        setupListeners();
        updatePostButtonState();
    }

    private void initViews() {
        tvBudgetDisplay = findViewById(R.id.tvBudgetDisplay);
        budgetSlider = findViewById(R.id.budgetSlider);
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        tvNotesCharCount = findViewById(R.id.tvNotesCharCount);
        tvDisplayDate = findViewById(R.id.tvDisplayDate);
        tvDisplayTime = findViewById(R.id.tvDisplayTime);
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
        budgetSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            String min = String.valueOf(values.get(0).intValue());
            String max = String.valueOf(values.get(1).intValue());
            tvBudgetDisplay.setText("₹" + min + " — ₹" + max);
        });

        for (int i = 0; i < urgencyCards.size(); i++) {
            final int index = i;
            urgencyCards.get(i).setOnClickListener(v -> selectUrgency(index));
        }

        findViewById(R.id.containerSetDate).setOnClickListener(v -> {
            tvDisplayDate.setText("Oct 24, 2023"); // Mock selection
            tvDisplayDate.setTextColor(Color.parseColor("#111827"));
            updatePostButtonState();
        });

        findViewById(R.id.containerSetTime).setOnClickListener(v -> {
            tvDisplayTime.setText("02:00 PM - 05:00 PM"); // Mock selection
            tvDisplayTime.setTextColor(Color.parseColor("#111827"));
            updatePostButtonState();
        });

        etAdditionalNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNotesCharCount.setText(s.length() + "/250");
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
        // Validation could go here if needed
        btnPostRequest.setEnabled(true);
        btnPostRequest.setAlpha(1.0f);
    }
}

