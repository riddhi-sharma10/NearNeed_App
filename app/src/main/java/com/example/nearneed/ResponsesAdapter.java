package com.example.nearneed;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private List<Response> responses;
    private OnResponseActionListener listener;
    private final boolean showBudgetInfo;

    public interface OnResponseActionListener {
        void onAccept(String responseId, int position);
        void onDecline(String responseId, int position);
        void onCall(Response response);
        void onMessage(Response response);
    }

    public ResponsesAdapter(List<Response> responses, OnResponseActionListener listener, boolean showBudgetInfo) {
        this.responses = responses;
        this.listener = listener;
        this.showBudgetInfo = showBudgetInfo;
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_response_card, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        Response response = responses.get(position);
        holder.bind(response, position, listener, showBudgetInfo);
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    public static class ResponseViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvRating;
        private TextView tvMessage;
        private TextView tvLocation;
        private TextView tvTime;
        private TextView tvStatus;
        private ImageButton btnCallApplicant, btnMessageApplicant;
        private MaterialButton btnAccept, btnDecline;
        private LinearLayout mainContainer, llBudgetCard;
        private TextView tvProposedBudget, tvPaymentMethod, tvPriceAppliedValue;

        public ResponseViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            mainContainer = itemView;
            ivAvatar = itemView.findViewById(R.id.ivApplicantAvatar);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvRating = itemView.findViewById(R.id.tvApplicantRating);
            tvMessage = itemView.findViewById(R.id.tvApplicantMessage);
            tvLocation = itemView.findViewById(R.id.tvApplicantLocation);
            tvTime = itemView.findViewById(R.id.tvAppliedTime);
            tvStatus = null;
            btnCallApplicant = itemView.findViewById(R.id.btnCallApplicant);
            btnMessageApplicant = itemView.findViewById(R.id.btnMessageApplicant);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            llBudgetCard = itemView.findViewById(R.id.llBudgetCard);
            tvProposedBudget = itemView.findViewById(R.id.tvProposedBudget);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPriceAppliedValue = itemView.findViewById(R.id.tvPriceAppliedValue);
        }

        public void bind(Response response, int position, OnResponseActionListener listener, boolean showBudgetInfo) {
            tvName.setText(response.getApplicantName());
            tvRating.setText(String.format("★ %.1f", response.getApplicantRating()));
            tvMessage.setText(response.getMessage());
            tvLocation.setText(response.getLocation());
            tvTime.setText(formatTime(response.getTimestamp()));

            // Always show what the applicant quoted as budget.
            llBudgetCard.setVisibility(LinearLayout.VISIBLE);
            if (response.getProposedBudget() > 0) {
                tvProposedBudget.setText("₹" + response.getProposedBudget());
                tvPriceAppliedValue.setText("₹" + response.getProposedBudget());
            } else {
                tvProposedBudget.setText("Not quoted");
                tvPriceAppliedValue.setText("Not quoted");
            }
            tvPaymentMethod.setText(response.getPaymentMethod() != null ? response.getPaymentMethod() : "Not specified");

            // Set status badge
            String status = response.getStatus();
            if (tvStatus != null) {
                tvStatus.setText(status.toUpperCase());
                updateStatusBadge(status);
            }

            applyPrimaryButtonStyle();
            applyOutlineButtonStyle();

            // Keep button visuals consistent with theme, even when disabled by status.
            if ("accepted".equals(status)) {
                mainContainer.setAlpha(1.0f);
                btnAccept.setText("Accepted");
                btnAccept.setAlpha(1.0f);
                btnDecline.setText("Declined");
                btnDecline.setAlpha(1.0f);
            } else if ("declined".equals(status)) {
                mainContainer.setAlpha(1.0f);
                btnAccept.setText("Accept");
                btnAccept.setAlpha(1.0f);
                btnDecline.setText("Declined");
                btnDecline.setAlpha(1.0f);
            } else {
                btnAccept.setText("Accept");
                btnAccept.setAlpha(1.0f);
                btnDecline.setText("Decline");
                btnDecline.setAlpha(1.0f);
                mainContainer.setAlpha(1.0f);
            }

            // Keep visual style active and gate click actions by status.
            btnAccept.setEnabled(true);
            btnDecline.setEnabled(true);

            btnAccept.setOnClickListener(v -> {
                if (listener != null && "new".equals(response.getStatus())) {
                    listener.onAccept(response.getResponseId(), position);
                }
            });

            btnDecline.setOnClickListener(v -> {
                if (listener != null && "new".equals(response.getStatus())) {
                    listener.onDecline(response.getResponseId(), position);
                }
            });

            btnCallApplicant.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCall(response);
                }
            });

            btnMessageApplicant.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessage(response);
                }
            });
        }

        private void updateStatusBadge(String status) {
            if (tvStatus == null) {
                return;
            }
            switch (status.toLowerCase()) {
                case "new":
                    tvStatus.setTextColor(0xFF1E3A8A);
                    tvStatus.setBackgroundColor(0xFFDBEAFE);
                    break;
                case "accepted":
                    tvStatus.setTextColor(0xFF065F46);
                    tvStatus.setBackgroundColor(0xFFD1FAE5);
                    break;
                case "declined":
                    tvStatus.setTextColor(0xFFB91C1C);
                    tvStatus.setBackgroundColor(0xFFFEE2E2);
                    break;
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            long days = diff / (24 * 60 * 60 * 1000);

            if (minutes < 60) {
                return minutes + " min ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else if (days < 7) {
                return days + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }

        private void applyPrimaryButtonStyle() {
            int sapphire = ContextCompat.getColor(itemView.getContext(), R.color.sapphire_primary);
            int white = ContextCompat.getColor(itemView.getContext(), R.color.white);
            btnAccept.setBackgroundTintList(ColorStateList.valueOf(sapphire));
            btnAccept.setTextColor(white);
            btnAccept.setCornerRadius((int) (24 * itemView.getResources().getDisplayMetrics().density));
        }

        private void applyOutlineButtonStyle() {
            int white = ContextCompat.getColor(itemView.getContext(), R.color.white);
            int sapphire = ContextCompat.getColor(itemView.getContext(), R.color.sapphire_primary);
            btnDecline.setBackgroundTintList(ColorStateList.valueOf(white));
            btnDecline.setTextColor(sapphire);
            btnDecline.setStrokeColor(ColorStateList.valueOf(sapphire));
            btnDecline.setStrokeWidth((int) (1 * itemView.getResources().getDisplayMetrics().density));
            btnDecline.setCornerRadius((int) (24 * itemView.getResources().getDisplayMetrics().density));
        }
    }
}
