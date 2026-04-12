package com.example.nearneed;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class CommunityPostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_post);

        ImageView btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // CATEGORY SELECTION LOGIC
        int[] chipIds = {R.id.chipMedical, R.id.chipFood, R.id.chipTransport, R.id.chipTools, R.id.chipShelter, R.id.chipInformation, R.id.chipOther};
        
        // Default select Food
        selectChip(findViewById(R.id.chipFood), chipIds);

        for (int id : chipIds) {
            findViewById(id).setOnClickListener(v -> {
                if (id == R.id.chipOther) {
                    showOtherCategoryDialog(chipIds);
                } else {
                    selectChip((android.widget.TextView) v, chipIds);
                }
            });
        }

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                android.widget.EditText etTitle = findViewById(R.id.etRequestTitle);
                android.widget.EditText etDesc = findViewById(R.id.etRequestDescription);
                
                if (etTitle.getText().toString().trim().isEmpty()) {
                    etTitle.setError("Title is required");
                    return;
                }
                if (etDesc.getText().toString().trim().isEmpty()) {
                    etDesc.setError("Description is required");
                    return;
                }

                android.content.Intent intent = new android.content.Intent(this, CommunityPostStep2Activity.class);
                startActivity(intent);
            });
        }
    }

    private void selectChip(android.widget.TextView selected, int[] chipIds) {
        for (int otherId : chipIds) {
            android.widget.TextView ot = findViewById(otherId);
            ot.setBackgroundResource(R.drawable.sel_community_chip);
            ot.setTextColor(0xFF1E293B);
            ot.setTypeface(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL);
        }
        selected.setBackgroundResource(R.drawable.bg_id_uploaded);
        selected.getBackground().setTint(0xFF065F46); // Dark Green
        selected.setTextColor(android.graphics.Color.WHITE);
        selected.setTypeface(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD);
    }

    private void showOtherCategoryDialog(int[] chipIds) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Specify category");
        input.setPadding(40, 40, 40, 40);
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Other Category")
            .setView(input)
            .setPositiveButton("Specify", (dialog, which) -> {
                String text = input.getText().toString().trim();
                if (!text.isEmpty()) {
                    android.widget.TextView chipOther = findViewById(R.id.chipOther);
                    chipOther.setText(text);
                    selectChip(chipOther, chipIds);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
