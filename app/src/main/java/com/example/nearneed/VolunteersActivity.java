package com.example.nearneed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class VolunteersActivity extends AppCompatActivity {

    private RecyclerView rvVolunteers;
    private LinearLayout emptyStateLayout;
    private ResponsesAdapter adapter;
    private ApplicationViewModel appViewModel;
    private BookingViewModel bookingViewModel;
    private String postId;
    private List<Application> allApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteers);

        postId = getIntent().getStringExtra("post_id");
        if (postId == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupViewModels();
        setupFilters();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvVolunteers = findViewById(R.id.rvVolunteers);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        ((TextView)findViewById(R.id.tvTitle)).setText("Volunteers");
    }

    private void setupFilters() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                applyFilter(chip.getTag().toString());
            }
        });
    }

    private void setupViewModels() {
        appViewModel = new ViewModelProvider(this).get(ApplicationViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        appViewModel.getApplicationsByPost(postId).observe(this, apps -> {
            allApps = apps;
            applyFilter("all");
        });

        appViewModel.observeApplicationsByPost(this, postId);
    }

    private void applyFilter(String filter) {
        List<Application> filtered = new ArrayList<>();
        for (Application app : allApps) {
            if ("all".equals(filter)) filtered.add(app);
            else if ("confirmed".equals(filter) && "accepted".equals(app.status)) filtered.add(app);
            else if ("pending".equals(filter) && "pending".equals(app.status)) filtered.add(app);
        }
        adapter.setApplications(filtered);
        emptyStateLayout.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        rvVolunteers.setLayoutManager(new LinearLayoutManager(this));
        // Reusing ResponsesAdapter as it handles Application models perfectly
        adapter = new ResponsesAdapter(new ArrayList<>(), new ResponsesAdapter.OnResponseActionListener() {
            @Override
            public void onAccept(Application application, int position) {
                appViewModel.updateApplicationStatus(application.applicationId, "accepted");
                bookingViewModel.createBookingFromApplication(application);
            }

            @Override
            public void onDecline(Application application, int position) {
                appViewModel.updateApplicationStatus(application.applicationId, "declined");
            }

            @Override
            public void onCall(Application application) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + application.applicantPhone));
                startActivity(intent);
            }

            @Override
            public void onMessage(Application application) {
                Intent intent = new Intent(VolunteersActivity.this, ChatActivity.class);
                intent.putExtra("CHAT_NAME", application.applicantName);
                startActivity(intent);
            }
        });
        rvVolunteers.setAdapter(adapter);
    }
}
