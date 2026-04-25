package com.example.nearneed;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResponsesAdapter extends RecyclerView.Adapter<ResponsesAdapter.ResponseViewHolder> {

    private List<Application> applications;
    private OnResponseActionListener listener;

    public interface OnResponseActionListener {
        void onAccept(Application application, int position);
        void onDecline(Application application, int position);
        void onCall(Application application);
        void onMessage(Application application);
    }

    public ResponsesAdapter(List<Application> applications, OnResponseActionListener listener) {
        this.applications = applications;
        this.listener = listener;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_response_card, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        Application app = applications.get(position);
        holder.bind(app, position, listener);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ResponseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvRating, tvMessage, tvLocation, tvTime;
        private ImageButton btnCallApplicant, btnMessageApplicant;
        private MaterialButton btnAccept, btnDecline;
        private LinearLayout llBudgetCard;
        private TextView tvProposedBudget, tvPaymentMethod, tvPriceAppliedValue;

        public ResponseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvRating = itemView.findViewById(R.id.tvApplicantRating);
            tvMessage = itemView.findViewById(R.id.tvApplicantMessage);
            tvLocation = itemView.findViewById(R.id.tvApplicantLocation);
            tvTime = itemView.findViewById(R.id.tvAppliedTime);
            btnCallApplicant = itemView.findViewById(R.id.btnCallApplicant);
            btnMessageApplicant = itemView.findViewById(R.id.btnMessageApplicant);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            llBudgetCard = itemView.findViewById(R.id.llBudgetCard);
            tvProposedBudget = itemView.findViewById(R.id.tvProposedBudget);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPriceAppliedValue = itemView.findViewById(R.id.tvPriceAppliedValue);
        }

        public void bind(Application app, int position, OnResponseActionListener listener) {
            tvName.setText(app.applicantName);
            tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", app.applicantRating != null ? app.applicantRating : 0.0f));
            tvMessage.setText(app.message);
            tvLocation.setText(app.applicantLocation != null ? app.applicantLocation : "Nearby");
            tvTime.setText(formatTime(app.timestamp));

            // Budget Info
            if (app.proposedBudget != null && !app.proposedBudget.isEmpty()) {
                llBudgetCard.setVisibility(View.VISIBLE);
                tvProposedBudget.setText(app.proposedBudget);
                tvPriceAppliedValue.setText(app.proposedBudget);
                tvPaymentMethod.setText(app.paymentMethod != null ? app.paymentMethod : "CASH");
            } else {
                llBudgetCard.setVisibility(View.GONE);
            }

            // Status Logic
            if ("accepted".equals(app.status)) {
                btnAccept.setText("Accepted");
                btnAccept.setEnabled(false);
                btnDecline.setVisibility(View.GONE);
            } else if ("declined".equals(app.status)) {
                btnDecline.setText("Declined");
                btnDecline.setEnabled(false);
                btnAccept.setVisibility(View.GONE);
            } else {
                btnAccept.setText("Accept");
                btnAccept.setEnabled(true);
                btnAccept.setVisibility(View.VISIBLE);
                btnDecline.setText("Decline");
                btnDecline.setEnabled(true);
                btnDecline.setVisibility(View.VISIBLE);
            }

            btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(app, position);
            });
            btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(app, position);
            });
            btnCallApplicant.setOnClickListener(v -> {
                if (listener != null) listener.onCall(app);
            });
            btnMessageApplicant.setOnClickListener(v -> {
                if (listener != null) listener.onMessage(app);
            });
        }

        private String formatTime(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(timestamp));
        }
    }
}
