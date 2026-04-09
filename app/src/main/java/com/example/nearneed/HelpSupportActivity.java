package com.example.nearneed;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupFaq(
                R.id.faqPostGigHeader,
                R.id.faqPostGigAnswer,
                R.id.faqPostGigArrow
        );
        setupFaq(
                R.id.faqIdVerifyHeader,
                R.id.faqIdVerifyAnswer,
                R.id.faqIdVerifyArrow
        );
        setupFaq(
                R.id.faqFeeHeader,
                R.id.faqFeeAnswer,
                R.id.faqFeeArrow
        );
        setupFaq(
                R.id.faqLocationHeader,
                R.id.faqLocationAnswer,
                R.id.faqLocationArrow
        );
        setupFaq(
                R.id.faqScoreHeader,
                R.id.faqScoreAnswer,
                R.id.faqScoreArrow
        );
        setupFaq(
                R.id.faqReportHeader,
                R.id.faqReportAnswer,
                R.id.faqReportArrow
        );
        setupFaq(
                R.id.faqPasswordHeader,
                R.id.faqPasswordAnswer,
                R.id.faqPasswordArrow
        );
        setupFaq(
                R.id.faqNotificationsHeader,
                R.id.faqNotificationsAnswer,
                R.id.faqNotificationsArrow
        );
        setupFaq(
                R.id.faqVisibilityHeader,
                R.id.faqVisibilityAnswer,
                R.id.faqVisibilityArrow
        );
        setupFaq(
                R.id.faqChatHeader,
                R.id.faqChatAnswer,
                R.id.faqChatArrow
        );

        View btnContactUs = findViewById(R.id.btnContactUs);
        if (btnContactUs != null) {
            btnContactUs.setOnClickListener(v -> showContactSupportDialog());
        }
    }

    private void showContactSupportDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_contact_support, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnContinueChat).setOnClickListener(v -> {
            dialog.dismiss();
            android.widget.Toast.makeText(this, "Starting support chat...", android.widget.Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnCallAgent).setOnClickListener(v -> {
            dialog.dismiss();
            android.widget.Toast.makeText(this, "Connecting to support line...", android.widget.Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupFaq(@IdRes int headerId, @IdRes int answerId, @IdRes int arrowId) {
        View header = findViewById(headerId);
        TextView answer = findViewById(answerId);
        ImageView arrow = findViewById(arrowId);

        if (header == null || answer == null || arrow == null) return;

        header.setOnClickListener(v -> {
            boolean expanding = answer.getVisibility() != View.VISIBLE;
            answer.setVisibility(expanding ? View.VISIBLE : View.GONE);
            arrow.animate().rotation(expanding ? 180f : 0f).setDuration(180).start();
        });
    }
}
