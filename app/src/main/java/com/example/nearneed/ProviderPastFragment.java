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

import java.util.ArrayList;
import java.util.List;

public class ProviderPastFragment extends Fragment {

    private RecyclerView rvJobs;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateTitle;
    private TextView emptyStateSubtitle;
    private UpcomingJobsAdapter adapter;
    private UpcomingJobManager jobManager;
    private List<UpcomingJob> jobs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_provider_past, container, false);

        jobManager = new UpcomingJobManager(requireContext());
        rvJobs = rootView.findViewById(R.id.rvProviderPastJobs);
        emptyStateLayout = rootView.findViewById(R.id.emptyStateLayout);
        emptyStateTitle = rootView.findViewById(R.id.emptyStateTitle);
        emptyStateSubtitle = rootView.findViewById(R.id.emptyStateSubtitle);

        rvJobs.setLayoutManager(new LinearLayoutManager(requireContext()));

        jobs = new ArrayList<>(jobManager.getJobsByStatus("completed"));
        adapter = new UpcomingJobsAdapter(jobs, job -> {
            Intent intent = new Intent(requireContext(), ProviderJobDetailActivity.class);
            intent.putExtra("title", job.getTitle());
            intent.putExtra("category", job.getType());
            intent.putExtra("budget", job.getBudget() > 0 ? "₹" + job.getBudget() : "Not specified");
            intent.putExtra("description", "Completed by " + job.getAssignedPerson());
            intent.putExtra("distance", "Completed");
            intent.putExtra("duration", "Past job");
            startActivity(intent);
        });
        rvJobs.setAdapter(adapter);

        updateEmptyState();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (jobManager == null || jobs == null || adapter == null) {
            return;
        }
        jobs.clear();
        jobs.addAll(jobManager.getJobsByStatus("completed"));
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = jobs == null || jobs.isEmpty();
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (isEmpty) {
            emptyStateTitle.setText("No past jobs");
            emptyStateSubtitle.setText("Completed jobs will appear here");
        }
    }
}
