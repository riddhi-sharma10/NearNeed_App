package com.example.nearneed;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResponsesAdapter extends RecyclerView.Adapter<ResponsesAdapter.ResponseViewHolder> {

    private List<Application> applications;
    private OnResponseActionListener listener;
    private boolean isCommunity;

    public interface OnResponseActionListener {
        void onAccept(Application application, int position);
        void onDecline(Application application, int position);
        void onCall(Application application);
        void onMessage(Application application);
    }

    public ResponsesAdapter(List<Application> applications, boolean isCommunity, OnResponseActionListener listener) {
        this.applications = applications;
        this.isCommunity = isCommunity;
        this.listener = listener;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_response_card, parent, false);
        return new ResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
        holder.bind(applications.get(position), position, isCommunity, listener);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ResponseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvRating, tvMessage, tvLocation, tvTime;
        private ImageView ivAvatar;
        private ImageButton btnCallApplicant, btnMessageApplicant;
        private MaterialButton btnAccept, btnDecline;
        private LinearLayout llBudgetCard;
        private TextView tvProposedBudget, tvPaymentMethod, tvPriceAppliedValue;

        private LinearLayout llPriceApplied;

        public ResponseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName             = itemView.findViewById(R.id.tvApplicantName);
            tvRating           = itemView.findViewById(R.id.tvApplicantRating);
            tvMessage          = itemView.findViewById(R.id.tvApplicantMessage);
            tvLocation         = itemView.findViewById(R.id.tvApplicantLocation);
            tvTime             = itemView.findViewById(R.id.tvAppliedTime);
            ivAvatar           = itemView.findViewById(R.id.ivApplicantAvatar);
            btnCallApplicant   = itemView.findViewById(R.id.btnCallApplicant);
            btnMessageApplicant = itemView.findViewById(R.id.btnMessageApplicant);
            btnAccept          = itemView.findViewById(R.id.btnAccept);
            btnDecline         = itemView.findViewById(R.id.btnDecline);
            llBudgetCard       = itemView.findViewById(R.id.llBudgetCard);
            tvProposedBudget   = itemView.findViewById(R.id.tvProposedBudget);
            tvPaymentMethod    = itemView.findViewById(R.id.tvPaymentMethod);
            tvPriceAppliedValue = itemView.findViewById(R.id.tvPriceAppliedValue);
            llPriceApplied     = itemView.findViewById(R.id.llPriceApplied);
        }

        public void bind(Application app, int position, boolean isCommunity, OnResponseActionListener listener) {

            // ── Name ────────────────────────────────────────────────────────────────
            if (app.applicantName != null && !app.applicantName.isEmpty()) {
                tvName.setText(app.applicantName);
            } else {
                tvName.setText("Loading...");
                // Live-fetch name from Firestore for old records that were saved without it
                if (app.applicantId != null && !app.applicantId.isEmpty()) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(app.applicantId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc != null && doc.exists()) {
                                    String name = doc.getString("name");
                                    if (name == null || name.isEmpty()) name = doc.getString("fullName");
                                    if (name != null && !name.isEmpty()) {
                                        app.applicantName = name;
                                        tvName.setText(name);
                                    } else {
                                        tvName.setText("Provider");
                                    }

                                    // Also backfill photo if missing
                                    if ((app.applicantPhotoUrl == null || app.applicantPhotoUrl.isEmpty()) && ivAvatar != null) {
                                        String photo = doc.getString("photoUrl");
                                        if (photo == null) photo = doc.getString("profileImageUrl");
                                        if (photo != null && !photo.isEmpty()) {
                                            loadPhoto(photo);
                                        }
                                    }
                                } else {
                                    tvName.setText("Provider");
                                }
                            })
                            .addOnFailureListener(e -> tvName.setText("Provider"));
                } else {
                    tvName.setText("Provider");
                }
            }

            // ── Photo ────────────────────────────────────────────────────────────────
            if (ivAvatar != null) {
                String photoUrl = (app.applicantPhotoUrl != null && !app.applicantPhotoUrl.isEmpty())
                        ? app.applicantPhotoUrl
                        : DbConstants.getCatAvatarUrl(app.applicantId);
                loadPhoto(photoUrl);
            }

            // ── Rating ───────────────────────────────────────────────────────────────
            if (tvRating != null) {
                double rating = app.applicantRating != null ? app.applicantRating : 0.0;
                if (rating > 0) {
                    tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", rating));
                    tvRating.setVisibility(View.VISIBLE);
                } else {
                    tvRating.setText("★ New");
                    tvRating.setVisibility(View.VISIBLE);
                }
            }

            // ── Message & Location & Time ─────────────────────────────────────────────
            if (tvMessage  != null) tvMessage.setText(app.message != null ? app.message : "");
            if (tvLocation != null) tvLocation.setText(app.applicantLocation != null ? app.applicantLocation : "Nearby");
            if (tvTime     != null) tvTime.setText(formatTime(app.appliedAt));

            // ── Budget & Price Applied ───────────────────────────────────────────────
            if (llPriceApplied != null) {
                llPriceApplied.setVisibility(isCommunity ? View.GONE : View.VISIBLE);
            }
            if (llBudgetCard != null) {
                if (!isCommunity && app.proposedBudget != null && app.proposedBudget > 0) {
                    llBudgetCard.setVisibility(View.VISIBLE);
                    String budgetStr = "₹" + String.format(Locale.getDefault(), "%.0f", app.proposedBudget);
                    if (tvProposedBudget   != null) tvProposedBudget.setText(budgetStr);
                    if (tvPriceAppliedValue != null) tvPriceAppliedValue.setText(budgetStr);
                    if (tvPaymentMethod    != null) tvPaymentMethod.setText(
                            app.paymentMethod != null ? app.paymentMethod : "CASH");
                } else {
                    llBudgetCard.setVisibility(View.GONE);
                }
            }

            // ── Status: accepted / declined / pending ─────────────────────────────────
            boolean isAccepted = "accepted".equals(app.status);
            boolean isDeclined = "declined".equals(app.status) || "rejected".equals(app.status);

            // Card background tint to signal acceptance
            itemView.setAlpha(isDeclined ? 0.55f : 1.0f);

            if (isAccepted) {
                // Green "ACCEPTED" badge on the Accept button
                if (btnAccept != null) {
                    btnAccept.setText("✓ Accepted");
                    btnAccept.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#059669")));
                    btnAccept.setEnabled(false);
                }
                if (btnDecline != null) btnDecline.setVisibility(View.GONE);

            } else if (isDeclined) {
                if (btnDecline != null) {
                    btnDecline.setText("Declined");
                    btnDecline.setEnabled(false);
                }
                if (btnAccept != null) btnAccept.setVisibility(View.GONE);

                // Pending — reset both buttons to default
                if (btnAccept != null) {
                    btnAccept.setText("Accept");
                    btnAccept.setEnabled(true);
                    btnAccept.setVisibility(View.VISIBLE);
                    btnAccept.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), isCommunity ? R.color.brand_success : R.color.sapphire_primary)));
                }
                if (btnDecline != null) {
                    btnDecline.setText("Decline");
                    btnDecline.setEnabled(true);
                    btnDecline.setVisibility(View.VISIBLE);
                    btnDecline.setStrokeColorResource(isCommunity ? R.color.brand_success : R.color.sapphire_primary);
                }
            }
            
            // Community Colors for Contact Buttons
            if (isCommunity) {
                int greenColor = ContextCompat.getColor(itemView.getContext(), R.color.brand_success);
                if (btnCallApplicant != null) {
                    btnCallApplicant.setBackgroundResource(R.drawable.bg_circle_light_green);
                    btnCallApplicant.setImageTintList(ColorStateList.valueOf(greenColor));
                }
                if (btnMessageApplicant != null) {
                    btnMessageApplicant.setBackgroundResource(R.drawable.bg_circle_light_green);
                    btnMessageApplicant.setImageTintList(ColorStateList.valueOf(greenColor));
                }
            } else {
                int blueColor = ContextCompat.getColor(itemView.getContext(), R.color.sapphire_primary);
                if (btnCallApplicant != null) {
                    btnCallApplicant.setBackgroundResource(R.drawable.bg_circle_light_blue);
                    btnCallApplicant.setImageTintList(ColorStateList.valueOf(blueColor));
                }
                if (btnMessageApplicant != null) {
                    btnMessageApplicant.setBackgroundResource(R.drawable.bg_circle_light_blue);
                    btnMessageApplicant.setImageTintList(ColorStateList.valueOf(blueColor));
                }
            }

            // ── Click listeners ───────────────────────────────────────────────────────
            if (btnAccept  != null) btnAccept.setOnClickListener(v  -> { if (listener != null) listener.onAccept(app, position); });
            if (btnDecline != null) btnDecline.setOnClickListener(v -> { if (listener != null) listener.onDecline(app, position); });
            if (btnCallApplicant    != null) btnCallApplicant.setOnClickListener(v    -> { if (listener != null) listener.onCall(app); });
            if (btnMessageApplicant != null) btnMessageApplicant.setOnClickListener(v -> { if (listener != null) listener.onMessage(app); });
        }

        private void loadPhoto(String url) {
            if (ivAvatar == null) return;
            Glide.with(ivAvatar.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivAvatar);
        }

        private String formatTime(long timestamp) {
            if (timestamp <= 0) return "";
            long diff    = System.currentTimeMillis() - timestamp;
            long minutes = diff / (60 * 1000);
            long hours   = diff / (60 * 60 * 1000);
            long days    = diff / (24 * 60 * 60 * 1000);
            if (minutes < 1)  return "Just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours   < 24) return hours   + "h ago";
            if (days    < 7)  return days    + "d ago";
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(timestamp));
        }
    }
}
