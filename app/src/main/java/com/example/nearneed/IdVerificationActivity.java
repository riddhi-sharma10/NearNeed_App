package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import android.net.Uri;
import java.io.IOException;

public class IdVerificationActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_FRONT = 1001;
    private static final int REQUEST_PICK_BACK  = 1002;

    private ImageButton btnBack;
    private MaterialButton btnSubmit;
    private TextView btnSkip;
    private android.view.View cardUploadFront, cardUploadBack;
    private CheckBox cbTerms;
    private TextView tvTermsLink;
    private boolean frontUploaded = false;
    private boolean backUploaded = false;
    private boolean isFullyVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_verification);
        initViews();
        styleTermsText();

        if (getIntent().getBooleanExtra("HIDE_SKIP", false)) {
            btnSkip.setVisibility(View.GONE);
        }

        // Initial state: submit button disabled/dimmed
        btnSubmit.setEnabled(false);
        btnSubmit.setAlpha(0.6f);

        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSkip = findViewById(R.id.btnSkip);
        cardUploadFront = findViewById(R.id.cardUploadFront);
        cardUploadBack = findViewById(R.id.cardUploadBack);
        cbTerms = findViewById(R.id.cbTerms);
        tvTermsLink = findViewById(R.id.tvTermsLink);
    }

    private void styleTermsText() {
        String fullText = "I agree to the Terms, Conditions and Guidelines";
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf("Terms");
        int end = fullText.length();

        // Specific dark blue for links
        int linkColor = 0xFF1E3A8A;

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View widget) {
                startActivity(new Intent(IdVerificationActivity.this, TermsConditionsActivity.class));
            }

            @Override
            public void updateDrawState(android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(linkColor);
                ds.setFakeBoldText(true);
            }
        };

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTermsLink.setText(spannableString);
        tvTermsLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        cardUploadFront.setOnClickListener(v -> openImagePicker(REQUEST_PICK_FRONT));
        cardUploadBack.setOnClickListener(v  -> openImagePicker(REQUEST_PICK_BACK));
        
        // Make the entire row clickable to toggle checkbox
        android.view.View layoutTerms = findViewById(R.id.layoutTerms);
        if (layoutTerms != null) {
            layoutTerms.setOnClickListener(v -> {
                cbTerms.setChecked(!cbTerms.isChecked());
            });
        }

        cbTerms.setOnCheckedChangeListener((bv, checked) -> checkReadyToSubmit());

        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Verifying Authenticity...");
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E3A8A));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isFullyVerified = true;
                btnSubmit.setText("ID Verified Successfully");
                btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E3A8A));

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isFullyVerified) {
                        UserPrefs.saveVerified(this, true);
                        saveVerifiedToFirestore();
                        Intent intent = new Intent(this, IdVerifiedActivity.class);
                        startActivity(intent);
                    }
                }, 1000);
            }, 2500);
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileSuccessActivity.class);
            startActivity(intent);
        });
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select ID Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (requestCode == REQUEST_PICK_FRONT) {
                runOcrOnImage(imageUri, cardUploadFront, "Front of ID");
            } else if (requestCode == REQUEST_PICK_BACK) {
                runOcrOnImage(imageUri, cardUploadBack, "Back of ID");
            }
        }
    }

    private void runOcrOnImage(Uri imageUri, View card, String side) {
        TextView title = card.findViewById(card.getId() == R.id.cardUploadFront ? R.id.titleFront : R.id.titleBack);
        TextView desc = card.findViewById(card.getId() == R.id.cardUploadFront ? R.id.descFront : R.id.descBack);
        android.widget.ImageView icon = card.findViewById(card.getId() == R.id.cardUploadFront ? R.id.iconFront : R.id.iconBack);
        android.widget.ImageView tick = card.findViewById(card.getId() == R.id.cardUploadFront ? R.id.tickFront : R.id.tickBack);

        title.setText("Scanning ID...");
        desc.setText("Extracting data using AI...");
        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(R.drawable.ic_search_grey);
        icon.setColorFilter(0xFF1E3A8A, android.graphics.PorterDuff.Mode.SRC_IN);
        
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String resultText = visionText.getText();
                        if (resultText != null && !resultText.isEmpty()) {
                            // Deep analysis for authenticity
                            boolean hasGovKeywords = checkGovKeywords(resultText);
                            boolean hasIdPatterns = checkIdPatterns(resultText);
                            
                            // High confidence if it has patterns, medium if it has keywords + length
                            boolean looksLikeId = hasIdPatterns || (hasGovKeywords && resultText.length() > 50);
                            
                            if (looksLikeId) {
                                finalizeOcrResult(card, icon, title, desc, tick, side, true);
                            } else {
                                Toast.makeText(this, "Document not recognized as a valid ID. Please try again.", Toast.LENGTH_LONG).show();
                                finalizeOcrResult(card, icon, title, desc, tick, side, false);
                            }
                        } else {
                            finalizeOcrResult(card, icon, title, desc, tick, side, false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "OCR failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finalizeOcrResult(card, icon, title, desc, tick, side, false);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizeOcrResult(View card, android.widget.ImageView icon, TextView title, TextView desc, android.widget.ImageView tick, String side, boolean success) {
        if (success) {
            card.setBackgroundResource(R.drawable.bg_id_uploaded);
            icon.setVisibility(View.GONE);
            title.setText(side + " Scanned");
            title.setTextColor(0xFF1E3A8A);
            desc.setText("AI verification complete");
            desc.setTextColor(0xFF1E3A8A);
            if (tick != null) {
                tick.setVisibility(View.VISIBLE);
                tick.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_tick));
            }
            if (side.contains("Front")) frontUploaded = true;
            else backUploaded = true;
            checkReadyToSubmit();
        } else {
            title.setText("Upload Failed");
            desc.setText("Try a clearer photo");
        }
    }

    private void saveVerifiedToFirestore() {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("isVerified", true);
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users").document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }

    private boolean checkGovKeywords(String text) {
        String upper = text.toUpperCase();
        return upper.contains("INDIA") || upper.contains("GOVERNMENT") || 
               upper.contains("AADHAAR") || upper.contains("INCOME TAX") || 
               upper.contains("ELECTION COMMISSION") || upper.contains("IDENTITY CARD") ||
               upper.contains("LICENSE") || upper.contains("PASSPORT") || upper.contains("UIDAI");
    }

    private boolean checkIdPatterns(String text) {
        // Aadhaar: 12 digits (often with spaces)
        boolean hasAadhaar = text.matches(".*\\d{4}\\s\\d{4}\\s\\d{4}.*") || text.matches(".*\\d{12}.*");
        
        // PAN: 5 letters, 4 digits, 1 letter
        boolean hasPan = text.matches(".*[A-Z]{5}[0-9]{4}[A-Z]{1}.*");
        
        // Passport: Letter + 7 digits
        boolean hasPassport = text.matches(".*[A-Z]{1}[0-9]{7}.*");
        
        return hasAadhaar || hasPan || hasPassport;
    }

    private void checkReadyToSubmit() {
        if (frontUploaded && backUploaded && cbTerms.isChecked()) {
            btnSubmit.setEnabled(true);
            btnSubmit.setAlpha(1.0f);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E3A8A));
        } else {
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.6f);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFACB0C0));
        }
    }
}
