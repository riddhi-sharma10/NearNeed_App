package com.example.nearneed;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ResponsesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvResponses;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle, emptyStateSubtitle;
    private ResponsesAdapter adapter;
    private List<Response> allResponses, filteredResponses;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses);

        initViews();
        setupToolbar();
        setupFilters();
        loadResponses();
        setupRecyclerView();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvResponses = findViewById(R.id.rvResponses);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateSubtitle = findViewById(R.id.emptyStateSubtitle);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                String filterTag = selectedChip.getTag().toString();
                applyFilter(filterTag);
            }
        });

        // Set default filter to "All"
        Chip chipAll = findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setChecked(true);
        }
    }

    private void loadResponses() {
        // Initialize with sample responses (replace with database calls)
        allResponses = new ArrayList<>();

        // Sample response data
        allResponses.add(new Response(
            "resp_001",
            "Sarah Johnson",
            4.8f,
            "I can help with this! Very experienced in this area.",
            "2 km away",
            System.currentTimeMillis() - 3600000,
            "new"
        ));

        allResponses.add(new Response(
            "resp_002",
            "Mike Chen",
            4.5f,
            "Perfect match for my skills. Let's discuss details.",
            "1.5 km away",
            System.currentTimeMillis() - 7200000,
            "accepted"
        ));

        allResponses.add(new Response(
            "resp_003",
            "Emma Wilson",
            4.2f,
            "I'm interested in this opportunity.",
            "3 km away",
            System.currentTimeMillis() - 10800000,
            "new"
        ));

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
        });
        rvResponses.setAdapter(adapter);
        updateEmptyState();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredResponses.clear();

        if ("all".equals(filter)) {
            filteredResponses.addAll(allResponses);
        } else if ("new".equals(filter)) {
            for (Response r : allResponses) {
                if ("new".equals(r.getStatus())) {
                    filteredResponses.add(r);
                }
            }
        } else if ("accepted".equals(filter)) {
            for (Response r : allResponses) {
                if ("accepted".equals(r.getStatus())) {
                    filteredResponses.add(r);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void onAcceptResponse(String responseId, int position) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Accept this applicant?")
            .setMessage("They will be notified and can start work immediately.")
            .setPositiveButton("Accept", (dialog, which) -> {
                // Update response status
                Response response = filteredResponses.get(position);
                response.setStatus("accepted");
                adapter.notifyItemChanged(position);

                Toast.makeText(this, "Applicant accepted!", Toast.LENGTH_SHORT).show();
                // TODO: Send notification to applicant
            })
            .setNegativeButton("Cancel", null)
            .show();
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

        new MaterialAlertDialogBuilder(this)
            .setTitle("Send message to applicant")
            .setView(messageView)
            .setPositiveButton("Send", (dialog, which) -> {
                // TODO: Get message from EditText and send
                declineApplicant(responseId, position, "message_content");
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

            if ("new".equals(currentFilter)) {
                emptyStateTitle.setText("No new responses");
                emptyStateSubtitle.setText("New applicants will appear here");
            } else if ("accepted".equals(currentFilter)) {
                emptyStateTitle.setText("No accepted responses");
                emptyStateSubtitle.setText("Accepted applicants will appear here");
            } else {
                emptyStateTitle.setText("No responses yet");
                emptyStateSubtitle.setText("Check back soon for applicants!");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvResponses.setVisibility(View.VISIBLE);
        }
    }
}
