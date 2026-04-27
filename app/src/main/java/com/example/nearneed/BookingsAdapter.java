package com.example.nearneed;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private final String userRole;
    private final OnBookingActionListener actionListener;

    public interface OnBookingActionListener {
        void onUpdateStatus(Booking booking);
        void onMessage(Booking booking);
        void onCancel(Booking booking);
    }

    public BookingsAdapter(String userRole, OnBookingActionListener actionListener) {
        this.userRole = userRole;
        this.actionListener = actionListener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTitle, tvType, tvLocation, tvName, tvRating;
        ShapeableImageView ivProfile;
        MaterialButton btnMessage, btnUpdate, btnViewDetails;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            tvTitle       = itemView.findViewById(R.id.tvTitle);
            tvType        = itemView.findViewById(R.id.tvType);
            tvLocation    = itemView.findViewById(R.id.tvLocation);
            tvName        = itemView.findViewById(R.id.tvName);
            tvRating      = itemView.findViewById(R.id.tvRating);
            ivProfile     = itemView.findViewById(R.id.ivProfile);
            btnMessage    = itemView.findViewById(R.id.btnMessage);
            btnUpdate     = itemView.findViewById(R.id.btnUpdateStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }

        public void bind(Booking booking) {
            if (tvTitle != null) tvTitle.setText(booking.postTitle != null ? booking.postTitle : "Untitled");
            if (tvType  != null) tvType.setText(booking.postType != null ? booking.postType.toUpperCase() : "");

            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

            // Use the role passed to the adapter as the primary source of truth.
            // Fall back to per-booking UID check only if userRole is "all" (both roles).
            boolean isSeeker;
            if ("all".equalsIgnoreCase(userRole)) {
                // Dual-role view: decide per booking
                isSeeker = currentUserId != null && currentUserId.equals(booking.seekerId);
            } else {
                isSeeker = !RoleManager.ROLE_PROVIDER.equalsIgnoreCase(userRole);
            }

            // ── Label + other-party name ─────────────────────────────────────────
            String label;
            String otherPartyName;

            if (isSeeker) {
                label          = "ASSIGNED PRO";
                otherPartyName = (booking.providerName != null && !booking.providerName.isEmpty())
                        ? booking.providerName : "Provider";
            } else {
                label          = "CLIENT";
                otherPartyName = (booking.seekerName != null && !booking.seekerName.isEmpty())
                        ? booking.seekerName : "Seeker";
            }

            // Update the label TextView inside the profile block
            View profileBlock = itemView.findViewById(R.id.profileBlock);
            if (profileBlock != null) {
                android.widget.LinearLayout inner = (android.widget.LinearLayout)
                        ((android.widget.LinearLayout) profileBlock).getChildAt(1);
                if (inner != null && inner.getChildCount() > 0) {
                    android.widget.TextView lbl = (android.widget.TextView) inner.getChildAt(0);
                    if (lbl != null) lbl.setText(label);
                }
            }

            if (tvName != null) tvName.setText(otherPartyName);

            updateStatusUI(booking.status);

            // ── Message button: always visible for both roles ──────────────────
            if (btnMessage != null) {
                // Make Message full-width when provider (no second button)
                android.view.ViewGroup.LayoutParams params = btnMessage.getLayoutParams();
                if (params instanceof android.widget.LinearLayout.LayoutParams) {
                    android.widget.LinearLayout.LayoutParams lp =
                            (android.widget.LinearLayout.LayoutParams) params;
                    lp.weight        = isSeeker ? 1f : 1f;
                    lp.setMarginEnd(isSeeker ? (int)(8 * itemView.getResources().getDisplayMetrics().density) : 0);
                    btnMessage.setLayoutParams(lp);
                }
                btnMessage.setOnClickListener(v -> actionListener.onMessage(booking));
            }

            // ── Update Status: SEEKER only ────────────────────────────────────
            if (btnUpdate != null) {
                if (isSeeker) {
                    boolean isDone = "completed".equals(booking.status) || "cancelled".equals(booking.status);
                    btnUpdate.setVisibility(isDone ? View.GONE : View.VISIBLE);
                    btnUpdate.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), UpdateStatusActivity.class);
                        intent.putExtra("booking_id",     booking.bookingId);
                        intent.putExtra("booking_title",  booking.postTitle);
                        intent.putExtra("current_status", booking.status);
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    btnUpdate.setVisibility(View.GONE); // Provider never sees Update Status
                }
            }

            // ── View Details: hidden for now (provider has Message only) ──────
            if (btnViewDetails != null) btnViewDetails.setVisibility(View.GONE);
        }

        private void updateStatusUI(String status) {
            if (status == null) status = "pending";
            
            switch (status.toLowerCase()) {
                case "confirmed":
                case "in_progress":
                    tvStatus.setText("IN PROGRESS");
                    tvStatus.setTextColor(Color.parseColor("#047857"));
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_in_progress);
                    break;
                case "completed":
                    tvStatus.setText("COMPLETED");
                    tvStatus.setTextColor(Color.parseColor("#059669"));
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_completed_soft);
                    break;
                case "cancelled":
                    tvStatus.setText("CANCELLED");
                    tvStatus.setTextColor(Color.parseColor("#DC2626"));
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_cancelled_soft);
                    break;
                default:
                    tvStatus.setText(status.toUpperCase());
                    tvStatus.setTextColor(Color.GRAY);
                    break;
            }
        }
    }
}
