package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class VolunteersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvVolunteers;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle, emptyStateSubtitle;
    private VolunteersAdapter adapter;
    private List<Volunteer> allVolunteers, filteredVolunteers;
    private String currentFilter = "all";
    private int maxSlots = 0;
    private int confirmedCount = 0;
    private boolean isSeeker = false;
    private String postTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteers);

        // Get intent extras
        maxSlots = getIntent().getIntExtra("max_slots", 0);
        isSeeker = getIntent().getBooleanExtra("is_seeker", false);
        postTitle = getIntent().getStringExtra("post_title");

        initViews();
        setupToolbar();
        loadVolunteers();
        setupFilters();
        setupRecyclerView();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvVolunteers = findViewById(R.id.rvVolunteers);
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

    private void loadVolunteers() {
        // Initialize with sample volunteers (replace with database calls)
        allVolunteers = new ArrayList<>();

        // Sample volunteer data
        allVolunteers.add(new Volunteer(
            "vol_001",
            "Alex Kumar",
            4.7f,
            "Passionate about community service. Always ready to help!",
            "Help with grocery shopping",
            "interested",
            System.currentTimeMillis() - 3600000
        ));

        allVolunteers.add(new Volunteer(
            "vol_002",
            "Priya Sharma",
            4.9f,
            "Experienced volunteer, love helping neighbors",
            "I can assist with this task",
            "confirmed",
            System.currentTimeMillis() - 7200000
        ));

        allVolunteers.add(new Volunteer(
            "vol_003",
            "Rajesh Patel",
            4.5f,
            "Available on weekends to help community",
            "Happy to help!",
            "interested",
            System.currentTimeMillis() - 10800000
        ));

        filteredVolunteers = new ArrayList<>(allVolunteers);
        updateTitle();
    }

    private void setupRecyclerView() {
        rvVolunteers.setLayoutManager(new LinearLayoutManager(this));

        // Count already confirmed volunteers
        confirmedCount = 0;
        for (Volunteer v : allVolunteers) {
            if ("confirmed".equals(v.getStatus())) {
                confirmedCount++;
            }
        }

        adapter = new VolunteersAdapter(filteredVolunteers, new VolunteersAdapter.OnVolunteerActionListener() {
            @Override
            public void onViewProfile(String volunteerId) {
                Intent intent = new Intent(VolunteersActivity.this, VolunteerProfileActivity.class);
                intent.putExtra("volunteerId", volunteerId);
                startActivity(intent);
            }

            @Override
            public void onMessage(String volunteerId) {
                Intent chatIntent = new Intent(VolunteersActivity.this, ChatActivity.class);
                chatIntent.putExtra("CHAT_NAME", "Volunteer " + volunteerId);
                chatIntent.putExtra("CHAT_ONLINE", true);
                startActivity(chatIntent);
            }

            @Override
            public void onAccept(Volunteer volunteer) {
                if (isSeeker && canAcceptMore()) {
                    volunteer.setStatus("confirmed");
                    confirmedCount++;
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onReject(Volunteer volunteer) {
                if (isSeeker) {
                    volunteer.setStatus("rejected");
                    adapter.notifyDataSetChanged();
                }
            }
        }, isSeeker, maxSlots);
        rvVolunteers.setAdapter(adapter);
        updateEmptyState();
    }

    private boolean canAcceptMore() {
        if (maxSlots <= 0) return true; // No limit
        return confirmedCount < maxSlots;
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredVolunteers.clear();

        if ("all".equals(filter)) {
            filteredVolunteers.addAll(allVolunteers);
        } else if ("confirmed".equals(filter)) {
            for (Volunteer v : allVolunteers) {
                if ("confirmed".equals(v.getStatus())) {
                    filteredVolunteers.add(v);
                }
            }
        } else if ("pending".equals(filter)) {
            for (Volunteer v : allVolunteers) {
                if ("interested".equals(v.getStatus())) {
                    filteredVolunteers.add(v);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
        updateTitle();
    }

    private void updateTitle() {
        tvTitle.setText("Volunteers (" + filteredVolunteers.size() + ")");
    }

    private void updateEmptyState() {
        if (filteredVolunteers.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvVolunteers.setVisibility(View.GONE);

            if ("confirmed".equals(currentFilter)) {
                emptyStateTitle.setText("No confirmed volunteers");
                emptyStateSubtitle.setText("Confirm volunteers as they respond");
            } else if ("pending".equals(currentFilter)) {
                emptyStateTitle.setText("No pending volunteers");
                emptyStateSubtitle.setText("Volunteers will appear here");
            } else {
                emptyStateTitle.setText("No volunteers yet");
                emptyStateSubtitle.setText("Share your post to get volunteers!");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvVolunteers.setVisibility(View.VISIBLE);
        }
    }
}
