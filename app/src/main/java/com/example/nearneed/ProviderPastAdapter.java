package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class ProviderPastAdapter extends RecyclerView.Adapter<ProviderPastAdapter.PastJobViewHolder> {

    public interface OnPastJobActionListener {
        void onViewDetails(ProviderPastJob job);
        void onViewEarnings(ProviderPastJob job);
        void onViewCancellation(ProviderPastJob job);
    }

    private final List<ProviderPastJob> jobs;
    private final OnPastJobActionListener listener;

    public ProviderPastAdapter(List<ProviderPastJob> jobs, OnPastJobActionListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PastJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider_past_card, parent, false);
        return new PastJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastJobViewHolder holder, int position) {
        holder.bind(jobs.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class PastJobViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStatus;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final TextView tvTitle;
        private final TextView tvCustomer;

        // Completed section
        private final LinearLayout layoutCompleted;
        private final TextView tvRating;
        private final TextView tvReview;
        private final MaterialButton btnViewDetails;
        private final MaterialButton btnViewEarnings;

        // Cancelled section
        private final LinearLayout layoutCancelled;
        private final TextView tvCancellationNote;
        private final MaterialButton btnViewCancellation;

        PastJobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus          = itemView.findViewById(R.id.tvPastJobStatus);
            tvAmount          = itemView.findViewById(R.id.tvPastJobAmount);
            tvDate            = itemView.findViewById(R.id.tvPastJobDate);
            tvTitle           = itemView.findViewById(R.id.tvPastJobTitle);
            tvCustomer        = itemView.findViewById(R.id.tvPastJobCustomer);

            layoutCompleted   = itemView.findViewById(R.id.layoutCompleted);
            tvRating          = itemView.findViewById(R.id.tvPastJobRating);
            tvReview          = itemView.findViewById(R.id.tvPastJobReview);
            btnViewDetails    = itemView.findViewById(R.id.btnPastViewDetails);
            btnViewEarnings   = itemView.findViewById(R.id.btnPastViewEarnings);

            layoutCancelled   = itemView.findViewById(R.id.layoutCancelled);
            tvCancellationNote= itemView.findViewById(R.id.tvCancellationNote);
            btnViewCancellation = itemView.findViewById(R.id.btnPastViewCancellation);
        }

        void bind(ProviderPastJob job, OnPastJobActionListener listener) {
            tvTitle.setText(job.getTitle());
            tvCustomer.setText("Customer: " + job.getCustomerName());
            tvDate.setText(job.getDate());

            if (job.isCompleted()) {
                // --- Status pill ---
                tvStatus.setText("COMPLETED");
                tvStatus.setBackgroundResource(R.drawable.bg_pill_completed_soft);
                tvStatus.setTextColor(0xFF0F766E);

                // --- Amount (green, prefixed with +) ---
                tvAmount.setText(String.format(Locale.getDefault(), "+₹%.2f", job.getAmount()));
                tvAmount.setTextColor(0xFF0F766E);

                // --- Rating + review ---
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", job.getRating()));
                tvReview.setText(job.getReviewText());

                layoutCompleted.setVisibility(View.VISIBLE);
                layoutCancelled.setVisibility(View.GONE);

                btnViewDetails.setOnClickListener(v -> { if (listener != null) listener.onViewDetails(job); });
                btnViewEarnings.setOnClickListener(v -> { if (listener != null) listener.onViewEarnings(job); });

            } else {
                // --- Status pill ---
                tvStatus.setText("CANCELLED");
                tvStatus.setBackgroundResource(R.drawable.bg_pill_cancelled_soft);
                tvStatus.setTextColor(0xFFDC2626);

                // --- Amount (grey, no prefix) ---
                tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", job.getAmount()));
                tvAmount.setTextColor(0xFF6B7280);

                // --- Cancellation note ---
                String note = job.getCancellationNote();
                tvCancellationNote.setText(
                        (note != null && !note.isEmpty()) ? note
                                : "Cancellation fee not applicable for this booking type.");

                layoutCompleted.setVisibility(View.GONE);
                layoutCancelled.setVisibility(View.VISIBLE);

                btnViewCancellation.setOnClickListener(v -> { if (listener != null) listener.onViewCancellation(job); });
            }
        }
    }
}
