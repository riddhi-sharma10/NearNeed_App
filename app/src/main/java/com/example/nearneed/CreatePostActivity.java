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
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {

    private EditText etServiceTitle, etDescription;
    private TextView tvCharCount;
    private MaterialButton btnNextStep;
    private List<MaterialCardView> categoryCards = new ArrayList<>();
    private List<TextView> categoryTexts = new ArrayList<>();
    private String selectedCategory = "";
    private View layoutOtherCategory;
    private EditText etOtherCategory;
    private MaterialButton btnAddOtherCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        initViews();
        setupListeners();
        updateNextButtonState();
    }

    private void initViews() {
        etServiceTitle = findViewById(R.id.etServiceTitle);
        etDescription = findViewById(R.id.etDescription);
        tvCharCount = findViewById(R.id.tvCharCount);
        btnNextStep = findViewById(R.id.btnNextStep);

        categoryCards.add(findViewById(R.id.cardCleaning));
        categoryCards.add(findViewById(R.id.cardPlumbing));
        categoryCards.add(findViewById(R.id.cardElectrical));
        categoryCards.add(findViewById(R.id.cardMore));

        categoryTexts.add(findViewById(R.id.tvCleaning));
        categoryTexts.add(findViewById(R.id.tvPlumbing));
        categoryTexts.add(findViewById(R.id.tvElectrical));
        categoryTexts.add(findViewById(R.id.tvMore));

        layoutOtherCategory = findViewById(R.id.layoutOtherCategory);
        etOtherCategory = findViewById(R.id.etOtherCategory);
        btnAddOtherCategory = findViewById(R.id.btnAddOtherCategory);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // Category Selection
        for (int i = 0; i < categoryCards.size(); i++) {
            final int index = i;
            categoryCards.get(i).setOnClickListener(v -> selectCategory(index));
        }

        // Description Word Count
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvCharCount.setText(length + "/250");
                if (length >= 250) {
                    tvCharCount.setTextColor(Color.RED);
                } else {
                    tvCharCount.setTextColor(Color.parseColor("#94A3B8"));
                }
                updateNextButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Title Change Listener
        etServiceTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNextButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Photo Listener
        findViewById(R.id.btnAddPhoto).setOnClickListener(v -> {
            Toast.makeText(this, "Photo picker coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Other Category "Set" Button
        btnAddOtherCategory.setOnClickListener(v -> {
            String otherCat = etOtherCategory.getText().toString().trim();
            if (!otherCat.isEmpty()) {
                TextView tvMore = findViewById(R.id.tvMore);
                tvMore.setText(otherCat);
                selectedCategory = otherCat;
                layoutOtherCategory.setVisibility(View.GONE);
                updateNextButtonState();
                
                // Keep it selected visually
                selectCategory(3); // 3 is the index for 'More'
            } else {
                Toast.makeText(this, "Please enter a category", Toast.LENGTH_SHORT).show();
            }
        });

        // Next Step Action
        btnNextStep.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePostStep2Activity.class);
            startActivity(intent);
        });
    }

    private void selectCategory(int index) {
        // Reset all
        for (int i = 0; i < categoryCards.size(); i++) {
            categoryCards.get(i).setStrokeColor(Color.parseColor("#E2E8F0"));
            categoryCards.get(i).setCardBackgroundColor(Color.WHITE);
            categoryTexts.get(i).setTextColor(Color.parseColor("#64748B"));
        }

        // Select chosen
        MaterialCardView selectedCard = categoryCards.get(index);
        selectedCard.setStrokeColor(Color.parseColor("#1E3A8A"));
        selectedCard.setCardBackgroundColor(Color.parseColor("#EFF6FF"));
        categoryTexts.get(index).setTextColor(Color.parseColor("#1E3A8A"));

        // Manage 'More' input field visibility
        if (index == 3) {
            layoutOtherCategory.setVisibility(View.VISIBLE);
            String currentText = categoryTexts.get(3).getText().toString();
            if (!currentText.equals("More")) {
                etOtherCategory.setText(currentText);
                selectedCategory = currentText;
            } else {
                selectedCategory = "";
            }
            etOtherCategory.requestFocus();
        } else {
            layoutOtherCategory.setVisibility(View.GONE);
            selectedCategory = categoryTexts.get(index).getText().toString();
        }
        
        updateNextButtonState();
    }

    private void updateNextButtonState() {
        boolean isTitleSet = etServiceTitle.getText().toString().trim().length() > 0;
        boolean isDescSet = etDescription.getText().toString().trim().length() > 0;
        boolean isCategorySelected = !selectedCategory.isEmpty();

        boolean isEnabled = isTitleSet && isDescSet && isCategorySelected;
        btnNextStep.setEnabled(isEnabled);
        
        // Visual feedback for disabled state
        if (isEnabled) {
            btnNextStep.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E3A8A")));
            btnNextStep.setAlpha(1.0f);
        } else {
            btnNextStep.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
            btnNextStep.setAlpha(0.6f);
        }
    }
}
