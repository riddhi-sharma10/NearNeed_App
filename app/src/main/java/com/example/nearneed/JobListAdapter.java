package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.Marker;
import java.util.List;

/**
 * Adapter for displaying jobs in the bottom sheet list
 * Shows: title, description, distance, budget
 */
public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {

    private List<JobItem> jobList;
    private OnJobClickListener onJobClickListener;

    public interface OnJobClickListener {
        void onJobClick(JobItem job, int position);
    }

    public static class JobItem {
        public String title;
        public String description;
        public String distance;
        public String budget;
        public String category;
        public int iconResId;
        public int colorResId;
        public Marker marker;

        public JobItem(String title, String description, String distance, String budget, String category, int iconResId, int colorResId, Marker marker) {
            this.title = title;
            this.description = description;
            this.distance = distance;
            this.budget = budget;
            this.category = category;
            this.iconResId = iconResId;
            this.colorResId = colorResId;
            this.marker = marker;
        }
    }

    public JobListAdapter(List<JobItem> jobList, OnJobClickListener listener) {
        this.jobList = jobList;
        this.onJobClickListener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job_list, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobItem job = jobList.get(position);
        holder.bind(job, position);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateList(List<JobItem> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    public class JobViewHolder extends RecyclerView.ViewHolder {
        private ImageView jobIcon;
        private TextView jobTitle;
        private TextView jobDescription;
        private TextView jobDistance;
        private TextView jobBudget;
        private View itemContainer;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobIcon = itemView.findViewById(R.id.item_job_icon);
            jobTitle = itemView.findViewById(R.id.item_job_title);
            jobDescription = itemView.findViewById(R.id.item_job_description);
            jobDistance = itemView.findViewById(R.id.item_job_distance);
            jobBudget = itemView.findViewById(R.id.item_job_budget);
            itemContainer = itemView.findViewById(R.id.item_job_container);
        }

        public void bind(JobItem job, int position) {
            jobTitle.setText(job.title);
            jobDescription.setText(job.description);
            jobDistance.setText(job.distance);
            jobBudget.setText(job.budget);

            // Set icon with color
            jobIcon.setImageResource(job.iconResId);
            jobIcon.setTint(ContextCompat.getColor(itemView.getContext(), job.colorResId));

            // Click listener
            itemContainer.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onJobClick(job, position);
                }
            });
        }
    }
}
