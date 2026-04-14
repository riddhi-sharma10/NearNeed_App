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

public class ProviderOngoingFragment extends Fragment {

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
        View rootView = inflater.inflate(R.layout.fragment_provider_ongoing, container, false);

        jobManager = new UpcomingJobManager(requireContext());
        rvJobs = rootView.findViewById(R.id.rvProviderOngoingJobs);
        emptyStateLayout = rootView.findViewById(R.id.emptyStateLayout);
        emptyStateTitle = rootView.findViewById(R.id.emptyStateTitle);
        emptyStateSubtitle = rootView.findViewById(R.id.emptyStateSubtitle);

        rvJobs.setLayoutManager(new LinearLayoutManager(requireContext()));

        jobs = new ArrayList<>(jobManager.getJobsByStatus("in_progress"));
        adapter = new UpcomingJobsAdapter(jobs, job -> {
            Intent intent = new Intent(requireContext(), UpdateStatusActivity.class);
            intent.putExtra("booking_id", job.getJobId());
            intent.putExtra("booking_title", job.getTitle());
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
        jobs.addAll(jobManager.getJobsByStatus("in_progress"));
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = jobs == null || jobs.isEmpty();
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (isEmpty) {
            emptyStateTitle.setText("No ongoing jobs");
            emptyStateSubtitle.setText("Accepted jobs in progress will appear here");
        }
    }
}
