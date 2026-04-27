package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class CommunityPostActivity extends AppCompatActivity {

    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post);

        ImageView btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // CATEGORY SELECTION LOGIC
        int[] chipIds = {
            R.id.chipMedical, R.id.chipFood, R.id.chipTransport,
            R.id.chipTools, R.id.chipShelter, R.id.chipInformation,
            R.id.chipOther
        };
        
        android.widget.EditText etOtherSpec = findViewById(R.id.etOtherCategory);

        for (int id : chipIds) {
            findViewById(id).setOnClickListener(v -> {
                selectChip((android.widget.TextView) v, chipIds);
                if (id == R.id.chipOther) {
                    etOtherSpec.setVisibility(android.view.View.VISIBLE);
                    etOtherSpec.requestFocus();
                    // Show keyboard automatically
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etOtherSpec, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    selectedCategory = etOtherSpec.getText().toString().trim();
                } else {
                    etOtherSpec.setVisibility(android.view.View.GONE);
                    selectedCategory = ((android.widget.TextView) v).getText().toString().trim();
                }
            });
        }

        etOtherSpec.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                android.widget.TextView chipOther = findViewById(R.id.chipOther);
                if (s.length() > 0) {
                    chipOther.setText(s);
                    selectedCategory = s.toString().trim();
                } else {
                    chipOther.setText("Other");
                    if (selectedCategory != null && selectedCategory.equalsIgnoreCase("Other")) {
                        selectedCategory = "";
                    }
                }
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        android.widget.EditText etTitle = findViewById(R.id.etRequestTitle);
        android.widget.EditText etDesc = findViewById(R.id.etRequestDescription);
        android.widget.TextView tvTitleCounter = findViewById(R.id.tvTitleCounter);
        android.widget.TextView tvDescCounter = findViewById(R.id.tvDescCounter);

        etTitle.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvTitleCounter.setText(s.length() + "/50");
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        etDesc.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescCounter.setText(s.length() + "/250");
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (etTitle.getText().toString().trim().isEmpty()) {
                    etTitle.setError("Title is required");
                    return;
                }
                if (etDesc.getText().toString().trim().isEmpty()) {
                    etDesc.setError("Description is required");
                    return;
                }

                android.content.Intent intent = new android.content.Intent(this, CommunityPostStep2Activity.class);
                intent.putExtra("post_type", "community");
                intent.putExtra("title", etTitle.getText().toString().trim());
                intent.putExtra("description", etDesc.getText().toString().trim());
                intent.putExtra("category", selectedCategory);
                ArrayList<String> imageUris = new ArrayList<>();
                for (android.net.Uri uri : photoUris) {
                    imageUris.add(uri.toString());
                }
                intent.putStringArrayListExtra("selected_images", imageUris);
                startActivity(intent);
            });
        }

        findViewById(R.id.cardAddPhoto).setOnClickListener(v -> {
            if (photoUris.size() >= 5) {
                android.widget.Toast.makeText(this, "Maximum 5 photos allowed", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                photoPickerLauncher.launch("image/*");
            }
        });
    }

    private java.util.List<android.net.Uri> photoUris = new java.util.ArrayList<>();
    private final androidx.activity.result.ActivityResultLauncher<String> photoPickerLauncher =
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                photoUris.add(uri);
                addPhotoThumbnail(uri);
                updateAddButtonVisibility();
            }
        });

    private void addPhotoThumbnail(android.net.Uri uri) {
        android.widget.LinearLayout layout = findViewById(R.id.layoutPhotos);
        com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
        
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            (int)(100 * getResources().getDisplayMetrics().density),
            (int)(100 * getResources().getDisplayMetrics().density)
        );
        params.setMargins(0, 0, (int)(10 * getResources().getDisplayMetrics().density), 0);
        card.setLayoutParams(params);
        card.setRadius(20 * getResources().getDisplayMetrics().density);
        card.setCardElevation(0);
        card.setStrokeColor(android.graphics.Color.parseColor("#E2E8F0"));
        card.setStrokeWidth(1);

        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ));
        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        iv.setImageURI(uri);
        
        card.addView(iv);
        
        // Add before the ADD button
        layout.addView(card, layout.getChildCount() - 1);
        
        card.setOnLongClickListener(v -> {
            layout.removeView(card);
            photoUris.remove(uri);
            updateAddButtonVisibility();
            return true;
        });
    }

    private void updateAddButtonVisibility() {
        android.view.View btnAdd = findViewById(R.id.cardAddPhoto);
        if (photoUris.size() >= 5) {
            btnAdd.setVisibility(android.view.View.GONE);
        } else {
            btnAdd.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void selectChip(android.widget.TextView selected, int[] chipIds) {
        for (int otherId : chipIds) {
            android.widget.TextView ot = findViewById(otherId);
            if (ot != null) {
                ot.setBackgroundResource(R.drawable.sel_community_chip);
                ot.setTextColor(0xFF1E293B);
                ot.setTypeface(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL);
            }
        }
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_id_uploaded);
            selected.getBackground().setTint(0xFF065F46); // Dark Green
            selected.setTextColor(android.graphics.Color.WHITE);
            selected.setTypeface(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD);
        }
    }
}
