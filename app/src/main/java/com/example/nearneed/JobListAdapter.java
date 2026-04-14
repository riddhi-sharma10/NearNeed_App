package com.example.nearneed;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying jobs in the provider bottom sheet list
 */
public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> {

    private List<MapsFragment.Job> jobList;
    private OnJobClickListener onJobClickListener;

    public interface OnJobClickListener {
        void onJobClick(MapsFragment.Job job, int position);
    }

    public JobListAdapter(List<MapsFragment.Job> jobList, OnJobClickListener listener) {
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
        MapsFragment.Job job = jobList.get(position);
        holder.bind(job, position);
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateList(List<MapsFragment.Job> newList) {
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

        public void bind(MapsFragment.Job job, int position) {
            jobTitle.setText(job.title);
            jobDescription.setText(job.description);
            jobDistance.setText(job.distance);
            jobBudget.setText(job.budget);

            // Set icon with color
            jobIcon.setImageResource(job.iconResId);
            int color = ContextCompat.getColor(itemView.getContext(), job.colorResId);
            ImageViewCompat.setImageTintList(jobIcon, ColorStateList.valueOf(color));

            // Click listener
            itemContainer.setOnClickListener(v -> {
                if (onJobClickListener != null) {
                    onJobClickListener.onJobClick(job, position);
                }
            });
        }
    }
}
