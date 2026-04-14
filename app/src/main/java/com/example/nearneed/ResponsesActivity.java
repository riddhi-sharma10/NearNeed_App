package com.example.nearneed;

import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResponsesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivResponsesHero;
    private RecyclerView rvResponses;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle, emptyStateSubtitle;
    private ResponsesAdapter adapter;
    private List<Response> allResponses, filteredResponses;
    private UpcomingJobManager upcomingJobManager;
    private boolean isGigResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        upcomingJobManager = new UpcomingJobManager(this);

        initViews();
        setupToolbar();
        setupHeroImage();
        loadResponses();
        setupRecyclerView();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivResponsesHero = findViewById(R.id.ivResponsesHero);
        rvResponses = findViewById(R.id.rvResponses);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateSubtitle = findViewById(R.id.emptyStateSubtitle);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupHeroImage() {
        if (ivResponsesHero == null) {
            return;
        }

        int[] heroImages = {
            R.drawable.welcome_bg_1,
            R.drawable.welcome_bg_2,
            R.drawable.welcome_bg_3,
            R.drawable.img_neighborhood,
            R.drawable.img_plumbing,
            R.drawable.img_lawn_mowing
        };
        int selected = heroImages[new Random().nextInt(heroImages.length)];
        ivResponsesHero.setImageResource(selected);
    }

    private void loadResponses() {
        // Initialize with sample responses (replace with database calls)
        allResponses = new ArrayList<>();

        // Check if this is for gigs or volunteers based on intent
        isGigResponses = getIntent().getBooleanExtra("is_gig", true);

        if (isGigResponses) {
            // Gig responses with budget and payment method
            allResponses.add(new Response(
                "resp_001",
                "Sarah Johnson",
                4.8f,
                "I can help with this! Very experienced in this area.",
                "2 km away",
                System.currentTimeMillis() - 3600000,
                "new",
                250,
                "Cash",
                "9876501234"
            ));

            allResponses.add(new Response(
                "resp_002",
                "Mike Chen",
                4.5f,
                "Perfect match for my skills. Let's discuss details.",
                "1.5 km away",
                System.currentTimeMillis() - 7200000,
                "accepted",
                300,
                "UPI",
                "9876502234"
            ));

            allResponses.add(new Response(
                "resp_003",
                "Emma Wilson",
                4.2f,
                "I'm interested in this opportunity.",
                "3 km away",
                System.currentTimeMillis() - 10800000,
                "new",
                280,
                "Cash",
                "9876503234"
            ));
        } else {
            // Volunteer responses with quoted amount if provided by applicant
            allResponses.add(new Response(
                "resp_001",
                "Sarah Johnson",
                4.8f,
                "I can help with this! Very experienced in this area.",
                "2 km away",
                System.currentTimeMillis() - 3600000,
                "new",
                150,
                "UPI",
                "9876511234"
            ));

            allResponses.add(new Response(
                "resp_002",
                "Mike Chen",
                4.5f,
                "Perfect match for my skills. Let's discuss details.",
                "1.5 km away",
                System.currentTimeMillis() - 7200000,
                "accepted",
                200,
                "Cash",
                "9876512234"
            ));

            allResponses.add(new Response(
                "resp_003",
                "Emma Wilson",
                4.2f,
                "I'm interested in this opportunity.",
                "3 km away",
                System.currentTimeMillis() - 10800000,
                "new",
                180,
                "UPI",
                "9876513234"
            ));
        }

        filteredResponses = new ArrayList<>(allResponses);
    }

    private void setupRecyclerView() {
        rvResponses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResponsesAdapter(filteredResponses, new ResponsesAdapter.OnResponseActionListener() {
            @Override
            public void onAccept(String responseId, int position) {
                onAcceptResponse(responseId, position);
            }

            @Override
            public void onDecline(String responseId, int position) {
                onDeclineResponse(responseId, position);
            }

            @Override
            public void onCall(Response response) {
                openDialer(response.getApplicantPhone());
            }

            @Override
            public void onMessage(Response response) {
                openInternalChat(response.getApplicantName());
            }
        }, isGigResponses);
        rvResponses.setAdapter(adapter);
        updateEmptyState();
    }

    private void openDialer(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(dialIntent);
    }

    private void openInternalChat(String applicantName) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("CHAT_NAME", applicantName != null ? applicantName : "Applicant");
        chatIntent.putExtra("CHAT_ONLINE", true);
        startActivity(chatIntent);
    }

    private void onAcceptResponse(String responseId, int position) {
        Response selectedResponse = filteredResponses.get(position);
        String confirmMessage = "They will be notified and can start work immediately.";
        if (isGigResponses && selectedResponse.getProposedBudget() > 0) {
            confirmMessage = "Applied for: ₹" + selectedResponse.getProposedBudget()
                + "\n\nThey will be notified and can start work immediately.";
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle("Accept this applicant?")
            .setMessage(confirmMessage)
            .setPositiveButton("Accept", (dialog, which) -> {
                // Update response status
                Response response = filteredResponses.get(position);
                response.setStatus("accepted");
                adapter.notifyItemChanged(position);

                // Save accepted job to upcoming jobs list
                String postTitle = getIntent().getStringExtra("post_title");
                boolean isGig = getIntent().getBooleanExtra("is_gig", true);

                UpcomingJob upcomingJob = new UpcomingJob(
                    responseId,
                    postTitle,
                    isGig ? "GIG" : "COMMUNITY",
                    "in_progress",
                    response.getApplicantName(),
                    response.getApplicantRating(),
                    response.getProposedBudget(),
                    response.getPaymentMethod(),
                    System.currentTimeMillis()
                );

                // Save to SharedPreferences
                saveUpcomingJob(upcomingJob);

                // Show success dialog
                showApplicantSelectedDialog(response.getApplicantName());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showApplicantSelectedDialog(String applicantName) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Selection Confirmed!")
            .setMessage(applicantName + " has been selected as your applicant.")
            .setPositiveButton("Go to Dashboard", (dialog, which) -> {
                // Redirect to dashboard
                navigateToDashboard();
            })
            .setCancelable(false)
            .show();
    }

    private void navigateToDashboard() {
        Intent dashboardIntent = new Intent(this, MainActivity.class);
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dashboardIntent);
        finish();
    }

    private void saveUpcomingJob(UpcomingJob job) {
        upcomingJobManager.saveJob(job);
    }

    private void onDeclineResponse(String responseId, int position) {
        String[] options = {"Send Message", "Just Decline", "Cancel"};

        new MaterialAlertDialogBuilder(this)
            .setTitle("Decline this applicant?")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Send message
                    showMessageDialog(responseId, position);
                } else if (which == 1) {
                    // Just decline
                    declineApplicant(responseId, position, null);
                }
            })
            .show();
    }

    private void showMessageDialog(String responseId, int position) {
        View messageView = getLayoutInflater().inflate(R.layout.dialog_send_message, null);
        TextInputEditText etMessage = messageView.findViewById(R.id.etMessage);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Send message to applicant")
            .setView(messageView)
            .setPositiveButton("Send", (dialog, which) -> {
                String message = etMessage != null && etMessage.getText() != null
                    ? etMessage.getText().toString().trim()
                    : "";
                declineApplicant(responseId, position, message);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void declineApplicant(String responseId, int position, String message) {
        Response response = filteredResponses.get(position);
        response.setStatus("declined");
        adapter.notifyItemChanged(position);

        Toast.makeText(this, "Applicant declined", Toast.LENGTH_SHORT).show();
        // TODO: Send notification to applicant
    }

    private void updateEmptyState() {
        if (filteredResponses.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvResponses.setVisibility(View.GONE);
            emptyStateTitle.setText("No responses yet");
            emptyStateSubtitle.setText("Check back soon for applicants!");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvResponses.setVisibility(View.VISIBLE);
        }
    }
}
