package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

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

            new android.os.Handler().postDelayed(() -> {
                isFullyVerified = true;
                btnSubmit.setText("ID Verified Successfully");
                btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1E3A8A));

                new android.os.Handler().postDelayed(() -> {
                    if (isFullyVerified) {
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
            if (requestCode == REQUEST_PICK_FRONT) {
                setCardUploadedState(cardUploadFront, "Front of ID");
            } else if (requestCode == REQUEST_PICK_BACK) {
                setCardUploadedState(cardUploadBack, "Back of ID");
            }
        }
    }

    private void setCardUploadedState(android.view.View card, String side) {
        android.widget.ImageView icon = null;
        TextView title = null;
        TextView desc = null;
        android.widget.ImageView tick = null;

        if (card.getId() == R.id.cardUploadFront) {
            icon = card.findViewById(R.id.iconFront);
            title = card.findViewById(R.id.titleFront);
            desc = card.findViewById(R.id.descFront);
            tick = card.findViewById(R.id.tickFront);
        } else if (card.getId() == R.id.cardUploadBack) {
            icon = card.findViewById(R.id.iconBack);
            title = card.findViewById(R.id.titleBack);
            desc = card.findViewById(R.id.descBack);
            tick = card.findViewById(R.id.tickBack);
        }

        if (icon != null && title != null && desc != null) {
            final android.widget.ImageView fIcon = icon;
            final TextView fTitle = title;
            final TextView fDesc = desc;
            final android.widget.ImageView fTick = tick;

            fTitle.setText("Scanning ID...");
            fDesc.setText("Extracting security features...");
            fIcon.setVisibility(View.VISIBLE);
            fIcon.setImageResource(R.drawable.ic_search_grey);
            fIcon.setColorFilter(0xFF1E3A8A);
            fIcon.setPadding(0, 0, 0, 0);
            fIcon.setBackground(null);
            
            if (fTick != null) {
                fTick.setVisibility(View.GONE);
            }

            new android.os.Handler().postDelayed(() -> {
                card.setBackgroundResource(R.drawable.bg_id_uploaded);
                fIcon.setVisibility(View.GONE);
                fTitle.setTextColor(0xFF1E3A8A);
                fDesc.setText("Data extracted successfully");
                fDesc.setTextColor(0xFF1E3A8A);
                
                if (fTick != null) {
                    fTick.setVisibility(View.VISIBLE);
                }

                if (side.contains("Front")) frontUploaded = true;
                else backUploaded = true;

                checkReadyToSubmit();
            }, 2000);
        }
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
