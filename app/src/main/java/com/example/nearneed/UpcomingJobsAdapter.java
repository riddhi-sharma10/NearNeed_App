package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpcomingJobsAdapter extends RecyclerView.Adapter<UpcomingJobsAdapter.UpcomingJobViewHolder> {

    private List<UpcomingJob> jobs;
    private OnJobActionListener listener;

    public interface OnJobActionListener {
        void onJobClicked(UpcomingJob job);
    }

    public UpcomingJobsAdapter(List<UpcomingJob> jobs, OnJobActionListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UpcomingJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_upcoming_job_card, parent, false);
        return new UpcomingJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingJobViewHolder holder, int position) {
        UpcomingJob job = jobs.get(position);
        holder.bind(job, listener);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public static class UpcomingJobViewHolder extends RecyclerView.ViewHolder {
        private TextView tvJobTitle;
        private TextView tvJobType;
        private TextView tvAssignedName;
        private TextView tvAssignedRating;
        private TextView tvBudget;
        private TextView tvPaymentMethod;
        private TextView tvStatus;
        private LinearLayout llBudgetInfo;
        private LinearLayout mainContainer;

        public UpcomingJobViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            mainContainer = itemView;
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvAssignedName = itemView.findViewById(R.id.tvAssignedName);
            tvAssignedRating = itemView.findViewById(R.id.tvAssignedRating);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            llBudgetInfo = itemView.findViewById(R.id.llBudgetInfo);
        }

        public void bind(UpcomingJob job, OnJobActionListener listener) {
            tvJobTitle.setText(job.getTitle());
            tvJobType.setText(job.getType());
            tvAssignedName.setText(job.getAssignedPerson());
            tvAssignedRating.setText(String.format("★ %.1f", job.getPersonRating()));

            // Show budget info only for gigs
            if ("GIG".equals(job.getType()) && job.getBudget() > 0) {
                llBudgetInfo.setVisibility(LinearLayout.VISIBLE);
                tvBudget.setText("₹" + job.getBudget());
                tvPaymentMethod.setText(job.getPaymentMethod() != null ? job.getPaymentMethod() : "Cash");
            } else {
                llBudgetInfo.setVisibility(LinearLayout.GONE);
            }

            // Set status
            String status = job.getStatus();
            tvStatus.setText(status.toUpperCase().replace("_", " "));
            updateStatusColor(status);

            // Click listener
            if (listener != null) {
                mainContainer.setOnClickListener(v -> listener.onJobClicked(job));
            }
        }

        private void updateStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "in_progress":
                    tvStatus.setTextColor(0xFF059669);
                    break;
                case "completed":
                    tvStatus.setTextColor(0xFF6B7280);
                    break;
                default:
                    tvStatus.setTextColor(0xFF1E3A8A);
                    break;
            }
        }
    }
}
