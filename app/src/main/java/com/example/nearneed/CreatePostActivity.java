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
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private android.widget.LinearLayout layoutPhotoContainer;
    private List<Uri> selectedImages = new ArrayList<>();
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private String postType = "GIG"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        if (getIntent() != null) {
            postType = getIntent().getStringExtra("post_type");
            if (postType == null) postType = "GIG";
        }

        initViews();
        setupPhotoPicker();
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
        layoutPhotoContainer = findViewById(R.id.layoutPhotoContainer);

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
            if (selectedImages.size() >= 5) {
                Toast.makeText(this, "Maximum 5 photos allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerLauncher.launch(Intent.createChooser(intent, "Select up to " + (5 - selectedImages.size()) + " photos"));
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
            intent.putExtra("post_type", postType);
            intent.putExtra("title", etServiceTitle.getText().toString().trim());
            intent.putExtra("description", etDescription.getText().toString().trim());
            intent.putExtra("category", selectedCategory);
            
            ArrayList<String> imageUris = new ArrayList<>();
            for (Uri uri : selectedImages) {
                imageUris.add(uri.toString());
            }
            intent.putStringArrayListExtra("selected_images", imageUris);
            
            startActivity(intent);
        });
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                if (selectedImages.size() < 5) {
                                    selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                                }
                            }
                        } else if (result.getData().getData() != null) {
                            if (selectedImages.size() < 5) {
                                selectedImages.add(result.getData().getData());
                            }
                        }
                        updatePhotoGallery();
                    }
                }
        );
    }

    private void updatePhotoGallery() {
        // Remove existing image views (keep the ADD button)
        int childCount = layoutPhotoContainer.getChildCount();
        if (childCount > 1) {
            layoutPhotoContainer.removeViews(1, childCount - 1);
        }

        for (int i = 0; i < selectedImages.size(); i++) {
            Uri imageUri = selectedImages.get(i);
            addThumbnailToGallery(imageUri);
        }
        
        // Hide/Show add button based on limit
        findViewById(R.id.btnAddPhoto).setVisibility(selectedImages.size() >= 5 ? View.GONE : View.VISIBLE);
    }

    private void addThumbnailToGallery(Uri uri) {
        MaterialCardView card = new MaterialCardView(this);
        int size = (int) (100 * getResources().getDisplayMetrics().density);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
        card.setLayoutParams(params);
        card.setRadius(20 * getResources().getDisplayMetrics().density);
        card.setCardElevation(0);
        card.setStrokeColor(Color.parseColor("#E2E8F0"));
        card.setStrokeWidth(1);

        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        iv.setImageURI(uri);
        card.addView(iv);

        // Removal on click
        card.setOnClickListener(v -> {
            selectedImages.remove(uri);
            updatePhotoGallery();
        });

        layoutPhotoContainer.addView(card);
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
