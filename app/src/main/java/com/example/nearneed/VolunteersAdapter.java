package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VolunteersAdapter extends RecyclerView.Adapter<VolunteersAdapter.VolunteerViewHolder> {

    private final List<Volunteer> volunteers;
    private final OnVolunteerActionListener listener;
    private boolean isSeeker;
    private int maxSlots;

    public interface OnVolunteerActionListener {
        void onViewProfile(String volunteerId);
        void onMessage(String volunteerId);
        void onAccept(Volunteer volunteer);
        void onReject(Volunteer volunteer);
    }

    public VolunteersAdapter(List<Volunteer> volunteers, OnVolunteerActionListener listener) {
        this(volunteers, listener, false, 0);
    }

    public VolunteersAdapter(List<Volunteer> volunteers, OnVolunteerActionListener listener,
                            boolean isSeeker, int maxSlots) {
        this.volunteers = volunteers;
        this.listener = listener;
        this.isSeeker = isSeeker;
        this.maxSlots = maxSlots;
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_volunteer_card, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Volunteer volunteer = volunteers.get(position);
        holder.bind(volunteer, listener, isSeeker, maxSlots);
    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }

    public static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvRating;
        private TextView tvBio;
        private TextView tvMessage;
        private TextView tvStatus;
        private TextView tvVolunteerDate;
        private MaterialButton btnViewProfile, btnMessage, btnAccept, btnReject;
        private LinearLayout llDefaultActions, llSeekerActions;

        public VolunteerViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivVolunteerAvatar);
            tvName = itemView.findViewById(R.id.tvVolunteerName);
            tvRating = itemView.findViewById(R.id.tvVolunteerRating);
            tvBio = itemView.findViewById(R.id.tvVolunteerBio);
            tvMessage = itemView.findViewById(R.id.tvVolunteerMessage);
            tvStatus = itemView.findViewById(R.id.tvVolunteerStatus);
            tvVolunteerDate = itemView.findViewById(R.id.tvVolunteerDate);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            llDefaultActions = itemView.findViewById(R.id.llDefaultActions);
            llSeekerActions = itemView.findViewById(R.id.llSeekerActions);
        }

        public void bind(Volunteer volunteer, OnVolunteerActionListener listener, boolean isSeeker, int maxSlots) {
            tvName.setText(volunteer.getVolunteerName());
            VerifiedBadgeHelper.apply(itemView.getContext(), tvName, volunteer.isVerified());
            tvRating.setText(String.format("★ %.1f", volunteer.getVolunteerRating()));
            tvBio.setText(volunteer.getBio());
            tvMessage.setText(volunteer.getMessage());
            tvVolunteerDate.setText(formatTime(volunteer.getVolunteerDate()));

            // Set status badge
            String status = volunteer.getStatus();
            tvStatus.setText(status.toUpperCase());
            updateStatusBadge(status);

            if (isSeeker) {
                // Show accept/reject buttons for seekers
                llDefaultActions.setVisibility(LinearLayout.GONE);
                llSeekerActions.setVisibility(LinearLayout.VISIBLE);

                // Disable accept button if status is already confirmed or rejected
                boolean canAccept = "interested".equals(status);
                btnAccept.setEnabled(canAccept);
                btnAccept.setAlpha(canAccept ? 1.0f : 0.5f);

                btnAccept.setOnClickListener(v -> {
                    if (listener != null && canAccept) {
                        listener.onAccept(volunteer);
                    }
                });

                btnReject.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onReject(volunteer);
                    }
                });
            } else {
                // Show view profile and message buttons for default view
                llDefaultActions.setVisibility(LinearLayout.VISIBLE);
                llSeekerActions.setVisibility(LinearLayout.GONE);

                btnViewProfile.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewProfile(volunteer.getVolunteerId());
                    }
                });

                btnMessage.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMessage(volunteer.getVolunteerId());
                    }
                });
            }
        }

        private void updateStatusBadge(String status) {
            switch (status.toLowerCase()) {
                case "interested":
                    tvStatus.setTextColor(0xFF1E3A8A);
                    tvStatus.setBackgroundColor(0xFFDBEAFE);
                    break;
                case "confirmed":
                    tvStatus.setTextColor(0xFF065F46);
                    tvStatus.setBackgroundColor(0xFFD1FAE5);
                    break;
                case "completed":
                    tvStatus.setTextColor(0xFF666666);
                    tvStatus.setBackgroundColor(0xFFF3F4F6);
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
    }
}
