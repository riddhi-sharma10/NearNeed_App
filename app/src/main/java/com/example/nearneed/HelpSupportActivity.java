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
                R.id.faqNotificationsHeader,
                R.id.faqNotificationsAnswer,
                R.id.faqNotificationsArrow
        );
        setupFaq(
                R.id.faqChatHeader,
                R.id.faqChatAnswer,
                R.id.faqChatArrow
        );
        setupFaq(
                R.id.faqProfileHeader,
                R.id.faqProfileAnswer,
                R.id.faqProfileArrow
        );
        setupFaq(
                R.id.faqSecurityHeader,
                R.id.faqSecurityAnswer,
                R.id.faqSecurityArrow
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


        view.findViewById(R.id.btnCallAgent).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:18001234567"));
            startActivity(intent);
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateProviderFaqs() {
        updateFaqText(R.id.faqPostGigHeader, R.id.faqPostGigAnswer,
                "How do I find and accept jobs?",
                "Browse the map or your Home feed for nearby Gigs. Tap 'Apply' to express interest. Once the Seeker accepts, the booking is confirmed!");

        updateFaqText(R.id.faqIdVerifyHeader, R.id.faqIdVerifyAnswer,
                "Does ID Verification help me?",
                "Absolutely! Providers with a Verified ID badge get up to 3x more bookings because Seekers trust them more.");

        updateFaqText(R.id.faqFeeHeader, R.id.faqFeeAnswer,
                "How do I get paid?",
                "Seekers pay you directly via Cash or UPI after you complete the work. Make sure to agree on the payment method in the chat beforehand.");

        updateFaqText(R.id.faqLocationHeader, R.id.faqLocationAnswer,
                "How do I set my service area?",
                "Go to Profile > Edit Profile and adjust your location. You will only see and be notified of jobs within this specific area.");

        updateFaqText(R.id.faqScoreHeader, R.id.faqScoreAnswer,
                "How do I improve my ratings?",
                "Be punctual, communicate clearly in the chat, and complete the work professionally. 5-star ratings push you to the top of the list!");

        updateFaqText(R.id.faqChatHeader, R.id.faqChatAnswer,
                "Can I cancel a job I accepted?",
                "Yes, from the 'My Bookings' tab. However, frequent cancellations negatively impact your profile score, so only cancel if absolutely necessary.");

        updateFaqText(R.id.faqNotificationsHeader, R.id.faqNotificationsAnswer,
                "How do I get notified of new jobs?",
                "Ensure notifications are enabled in your phone settings and set your service area to receive alerts for nearby requests.");

        updateFaqText(R.id.faqReportHeader, R.id.faqReportAnswer,
                "What if a Seeker is unresponsive?",
                "You can cancel the booking if they don't respond, or report them using the menu options in the chat.");

        updateFaqText(R.id.faqProfileHeader, R.id.faqProfileAnswer,
                "How do I showcase my skills?",
                "Add a clear profile photo and list your primary skills in your bio so Seekers know exactly what services you offer.");

        updateFaqText(R.id.faqSecurityHeader, R.id.faqSecurityAnswer,
                "Is my contact info shared?",
                "Your exact phone number is kept private unless you share it in chat. We encourage keeping all communication on the platform for safety.");
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
