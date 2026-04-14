package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ProviderUpcomingFragment extends Fragment {

    private RecyclerView rvUpcomingJobs;
    private LinearLayout emptyStateLayout;
    private TextView tvJobCount, emptyStateTitle, emptyStateSubtitle;
    private ChipGroup chipGroupFilter;
    private UpcomingJobsAdapter adapter;
    private UpcomingJobManager jobManager;
    private List<UpcomingJob> allJobs, filteredJobs;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_provider_upcoming, container, false);

        jobManager = new UpcomingJobManager(requireContext());

        initViews(rootView);
        setupFilters();
        loadJobs();
        setupRecyclerView();

        return rootView;
    }

    private void initViews(View rootView) {
        rvUpcomingJobs = rootView.findViewById(R.id.rvUpcomingJobs);
        emptyStateLayout = rootView.findViewById(R.id.emptyStateLayout);
        tvJobCount = rootView.findViewById(R.id.tv_job_count);
        emptyStateTitle = rootView.findViewById(R.id.emptyStateTitle);
        emptyStateSubtitle = rootView.findViewById(R.id.emptyStateSubtitle);
        chipGroupFilter = rootView.findViewById(R.id.chipGroupFilter);
    }

    private void setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = chipGroupFilter.findViewById(checkedIds.get(0));
                if (selectedChip != null && selectedChip.getTag() != null) {
                    String filterTag = selectedChip.getTag().toString();
                    applyFilter(filterTag);
                }
            }
        });

        // Set default filter to "All"
        Chip chipAll = chipGroupFilter.findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setChecked(true);
        }
    }

    private void loadJobs() {
        allJobs = new ArrayList<>(jobManager.getAllJobs());
        filteredJobs = new ArrayList<>(allJobs);
        updateJobCount();
    }

    private void setupRecyclerView() {
        rvUpcomingJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UpcomingJobsAdapter(filteredJobs, job -> {
            Intent intent = new Intent(requireContext(), ProviderJobDetailActivity.class);
            intent.putExtra("title", job.getTitle());
            intent.putExtra("category", job.getType());
            intent.putExtra("budget", job.getBudget() > 0 ? "₹" + job.getBudget() : "Not specified");
            intent.putExtra("description", "Assigned to " + job.getAssignedPerson());
            intent.putExtra("distance", "Nearby");
            intent.putExtra("duration", "In progress");
            startActivity(intent);
        });
        rvUpcomingJobs.setAdapter(adapter);
        updateEmptyState();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredJobs.clear();

        if ("all".equals(filter)) {
            filteredJobs.addAll(allJobs);
        } else {
            for (UpcomingJob job : allJobs) {
                if (filter.equals(job.getType())) {
                    filteredJobs.add(job);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateJobCount() {
        tvJobCount.setText(allJobs.size() + " " + (allJobs.size() == 1 ? "job" : "jobs") + " scheduled");
    }

    private void updateEmptyState() {
        if (filteredJobs.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvUpcomingJobs.setVisibility(View.GONE);

            if ("GIG".equals(currentFilter)) {
                emptyStateTitle.setText("No upcoming gigs");
                emptyStateSubtitle.setText("When you accept gigs, they'll appear here");
            } else if ("COMMUNITY".equals(currentFilter)) {
                emptyStateTitle.setText("No community volunteer jobs");
                emptyStateSubtitle.setText("When you accept community volunteer jobs, they'll appear here");
            } else {
                emptyStateTitle.setText("No upcoming jobs");
                emptyStateSubtitle.setText("When you accept gigs or volunteers, they'll appear here");
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvUpcomingJobs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh jobs when returning to this fragment
        loadJobs();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }
}
