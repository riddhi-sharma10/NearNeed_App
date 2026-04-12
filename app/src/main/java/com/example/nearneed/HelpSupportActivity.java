package com.example.nearneed;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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

        // Detect role and update FAQs if needed
        String role = RoleManager.getRole(this);
        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            updateProviderFaqs();
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

    private void updateProviderFaqs() {
        updateFaqText(R.id.faqPostGigHeader, R.id.faqPostGigAnswer,
                "How do I accept a job?",
                "Open your jobs feed, review the applicant's request, and tap 'Accept' to start. Communicate via chat for details.");

        updateFaqText(R.id.faqIdVerifyHeader, R.id.faqIdVerifyAnswer,
                "Does verification help my earnings?",
                "Yes! Verified providers are 3x more likely to be chosen by seekers. It builds trust in the community.");

        updateFaqText(R.id.faqFeeHeader, R.id.faqFeeAnswer,
                "What is the service fee for providers?",
                "NearNeed typically takes a small platform fee from completed bookings to keep the platform running securely.");

        updateFaqText(R.id.faqLocationHeader, R.id.faqLocationAnswer,
                "How do I set my service area?",
                "Go to Profile > Edit Profile to set your service radius. You will only see jobs within this distance.");

        updateFaqText(R.id.faqScoreHeader, R.id.faqScoreAnswer,
                "How do I improve my rating?",
                "Respond quickly to messages, complete jobs on time, and provide high-quality service to earn 5-star reviews.");

        updateFaqText(R.id.faqChatHeader, R.id.faqChatAnswer,
                "How do I communicate with seekers?",
                "Once you accept a job or express interest, a chat thread is created. Keep all communication on-app for safety.");
    }

    private void updateFaqText(@IdRes int headerId, @IdRes int answerId, String question, String answer) {
        ViewGroup header = findViewById(headerId);
        if (header != null) {
            for (int i = 0; i < header.getChildCount(); i++) {
                if (header.getChildAt(i) instanceof TextView) {
                    ((TextView) header.getChildAt(i)).setText(question);
                    break;
                }
            }
        }
        TextView answerTv = findViewById(answerId);
        if (answerTv != null) {
            answerTv.setText(answer);
        }
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
