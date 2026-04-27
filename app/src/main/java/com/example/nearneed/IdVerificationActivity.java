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
    private String extractedFrontText = "";
    private String extractedBackText  = "";

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
            btnSubmit.setText("Verifying...");

            String combinedText = extractedFrontText + "\n" + extractedBackText;

            // Check 1: Does the OCR text look like a real government ID?
            boolean hasGovKeywords = checkGovKeywords(combinedText);
            boolean hasIdPatterns  = checkIdPatterns(combinedText);
            if (!hasGovKeywords && !hasIdPatterns) {
                Toast.makeText(this,
                    "Could not confirm a valid government ID. Please upload a clearer photo.",
                    Toast.LENGTH_LONG).show();
                resetSubmitButton();
                return;
            }

            // Check 2: Does the name on the ID match the profile name?
            String extractedName = extractNameFromText(extractedFrontText);
            if (extractedName.isEmpty()) {
                extractedName = extractNameFromText(extractedBackText);
            }
            if (!extractedName.isEmpty() && !nameMatchesProfile(extractedName)) {
                Toast.makeText(this,
                    "Name on your ID does not match your profile name. " +
                    "Please update your profile name or use a matching ID.",
                    Toast.LENGTH_LONG).show();
                resetSubmitButton();
                return;
            }

            // All checks passed — mark verified
            isFullyVerified = true;
            btnSubmit.setText("ID Verified Successfully");
            btnSubmit.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF1E3A8A));

            final String finalExtractedName = extractedName;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                UserPrefs.saveVerified(this, true);
                saveVerifiedToFirestore(finalExtractedName);
                startActivity(new Intent(this, IdVerifiedActivity.class));
            }, 800);
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
                                if (side.contains("Front")) extractedFrontText = resultText;
                                else                        extractedBackText  = resultText;
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

    private void saveVerifiedToFirestore(String idExtractedName) {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("isVerified", true);
        data.put("verifiedAt", System.currentTimeMillis());
        if (!idExtractedName.isEmpty()) {
            data.put("idNameExtracted", idExtractedName);
        }
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit Verification");
        btnSubmit.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(0xFF1E3A8A));
    }

    /**
     * Tries to extract the person's name from raw OCR text.
     * Handles Aadhaar ("Name: Reia Baid"), PAN (all-caps name line), and Voter ID formats.
     */
    private String extractNameFromText(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String[] lines = text.split("\n");

        // Pass 1: look for "Name:" or "NAME:" label (Aadhaar, Voter ID)
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase().startsWith("name:") || trimmed.toLowerCase().startsWith("name ")) {
                String name = trimmed.replaceFirst("(?i)name[:\\s]+", "").trim();
                if (name.length() > 2) return name;
            }
        }

        // Pass 2: look for an all-letters line with ≥2 words that isn't a known header phrase
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("[A-Za-z .'-]{5,50}") && trimmed.split("\\s+").length >= 2) {
                String upper = trimmed.toUpperCase();
                if (!upper.contains("INDIA") && !upper.contains("GOVERNMENT")
                        && !upper.contains("INCOME") && !upper.contains("DEPARTMENT")
                        && !upper.contains("ELECTION") && !upper.contains("AADHAAR")
                        && !upper.contains("COMMISSION") && !upper.contains("PERMANENT")
                        && !upper.contains("ACCOUNT") && !upper.contains("NUMBER")) {
                    return trimmed;
                }
            }
        }
        return "";
    }

    /**
     * Fuzzy-matches the name found on the ID against the profile name the user entered.
     * Passes if at least the majority of name words overlap.
     */
    private boolean nameMatchesProfile(String idName) {
        String profileName = UserPrefs.getName(this);
        if (profileName.isEmpty()) return true; // No profile name set — can't check, let through
        if (idName.isEmpty()) return true;       // OCR couldn't extract a name — let through

        String normId      = idName.toLowerCase().replaceAll("[^a-z ]", "").replaceAll("\\s+", " ").trim();
        String normProfile = profileName.toLowerCase().replaceAll("[^a-z ]", "").replaceAll("\\s+", " ").trim();

        if (normId.equals(normProfile)) return true;
        if (normId.contains(normProfile) || normProfile.contains(normId)) return true;

        // Word-level overlap — require at least half the profile name words to appear in the ID name
        String[] profileWords = normProfile.split(" ");
        String[] idWords      = normId.split(" ");
        int matched = 0;
        for (String pw : profileWords) {
            if (pw.length() < 2) continue;
            for (String iw : idWords) {
                if (iw.equals(pw)) { matched++; break; }
            }
        }
        int required = (int) Math.ceil(profileWords.length / 2.0);
        return matched >= required;
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
