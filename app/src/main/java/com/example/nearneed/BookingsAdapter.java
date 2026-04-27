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
        MaterialButton btnMessage, btnUpdate;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            btnUpdate = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Booking booking) {
            tvTitle.setText(booking.postTitle);
            tvType.setText(booking.postType);
            
            // Show the other party's name dynamically based on who the user is in this specific booking
            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            String otherPartyName;
            
            if (currentUserId != null && currentUserId.equals(booking.seekerId)) {
                // User is the seeker, show provider name
                otherPartyName = booking.providerName;
                if (otherPartyName == null || otherPartyName.isEmpty()) otherPartyName = "Provider";
            } else {
                // User is the provider, show seeker name
                otherPartyName = booking.seekerName;
                if (otherPartyName == null || otherPartyName.isEmpty()) otherPartyName = "Seeker";
            }
            tvName.setText(otherPartyName);
            
            updateStatusUI(booking.status);

            btnMessage.setOnClickListener(v -> actionListener.onMessage(booking));
            btnUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), UpdateStatusActivity.class);
                intent.putExtra("booking_id", booking.bookingId);
                intent.putExtra("booking_title", booking.postTitle);
                intent.putExtra("current_status", booking.status);
                itemView.getContext().startActivity(intent);
            });
            
            // Hide update button if completed or cancelled
            if ("completed".equals(booking.status) || "cancelled".equals(booking.status)) {
                btnUpdate.setVisibility(View.GONE);
            } else {
                btnUpdate.setVisibility(View.VISIBLE);
            }
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
