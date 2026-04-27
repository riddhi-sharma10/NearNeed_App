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

    private List<BookingWrapper> items = new ArrayList<>();
    private final String userRole;
    private final OnBookingActionListener actionListener;

    public static class BookingWrapper {
        public enum Type { BOOKING, POST, APPLICATION }
        public Type type;
        public Booking booking;
        public Post post;
        public Application application;
        public long timestamp;

        public BookingWrapper(Booking booking) {
            this.type = Type.BOOKING;
            this.booking = booking;
            this.timestamp = (booking.createdAt != null) ? booking.createdAt : (booking.timestamp != null ? booking.timestamp : 0L);
        }

        public BookingWrapper(Post post) {
            this.type = Type.POST;
            this.post = post;
            this.timestamp = (post.timestamp != null) ? post.timestamp : 0L;
        }

        public BookingWrapper(Application application) {
            this.type = Type.APPLICATION;
            this.application = application;
            this.timestamp = (application.timestamp != null) ? application.timestamp : 0L;
        }
    }

    public interface OnBookingActionListener {
        void onUpdateStatus(Booking booking);
        void onMessage(Booking booking);
        void onCancel(Booking booking);
    }

    public BookingsAdapter(String userRole, OnBookingActionListener actionListener) {
        this.userRole = userRole;
        this.actionListener = actionListener;
    }

    public void setItems(List<BookingWrapper> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Deprecated
    public void setBookings(List<Booking> bookings) {
        List<BookingWrapper> wrappers = new ArrayList<>();
        for (Booking b : bookings) wrappers.add(new BookingWrapper(b));
        this.items = wrappers;
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
        BookingWrapper wrapper = items.get(position);
        holder.bind(wrapper);
    }

    @Override
    public int getItemCount() {
        return items.size();
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

        public void bind(BookingWrapper wrapper) {
            switch (wrapper.type) {
                case BOOKING:
                    bindBooking(wrapper.booking);
                    break;
                case POST:
                    bindPost(wrapper.post);
                    break;
                case APPLICATION:
                    bindApplication(wrapper.application);
                    break;
            }
        }

        private void bindBooking(Booking booking) {
            if (tvTitle != null) tvTitle.setText(booking.postTitle != null ? booking.postTitle : "Untitled");
            if (tvType  != null) tvType.setText(booking.postType != null ? booking.postType.toUpperCase() : "");
            if (tvLocation != null) tvLocation.setText("Location Details"); // Could be improved

            String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            boolean isSeeker = !RoleManager.ROLE_PROVIDER.equalsIgnoreCase(userRole);

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

            updateProfileBlock(label, otherPartyName, null);
            updateStatusUI(booking.status);

            if (btnMessage != null) {
                btnMessage.setVisibility(View.VISIBLE);
                btnMessage.setOnClickListener(v -> actionListener.onMessage(booking));
            }

            if (btnUpdate != null) {
                if (isSeeker) {
                    boolean isDone = "completed".equals(booking.status) || "cancelled".equals(booking.status);
                    btnUpdate.setVisibility(isDone ? View.GONE : View.VISIBLE);
                    btnUpdate.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), UpdateStatusActivity.class);
                        intent.putExtra("booking_id",     booking.bookingId);
                        intent.putExtra("booking_title",  booking.postTitle);
                        intent.putExtra("current_status", booking.status);
                        intent.putExtra("service_amount", booking.amount != null ? booking.amount : 500.0);
                        intent.putExtra("provider_name",  booking.providerName != null ? booking.providerName : "Provider");
                        itemView.getContext().startActivity(intent);
                    });
                } else {
                    btnUpdate.setVisibility(View.GONE);
                }
            }
            if (btnViewDetails != null) btnViewDetails.setVisibility(View.GONE);
        }

        private void bindPost(Post post) {
            if (tvTitle != null) tvTitle.setText(post.title != null ? post.title : "Untitled");
            if (tvType  != null) tvType.setText(post.type != null ? post.type.toUpperCase() : "");
            if (tvLocation != null) tvLocation.setText(post.location != null ? post.location : "Location");

            updateProfileBlock("POST CREATOR", "You", null);
            updateStatusUI("ACTIVE");

            if (btnMessage != null) btnMessage.setVisibility(View.GONE);
            if (btnUpdate != null) {
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText("View Responses");
                btnUpdate.setOnClickListener(v -> {
                    if ("COMMUNITY".equalsIgnoreCase(post.type)) {
                        Intent intent = new Intent(itemView.getContext(), VolunteersActivity.class);
                        intent.putExtra("post_id", post.postId);
                        intent.putExtra("post_title", post.title);
                        intent.putExtra("max_slots", post.volunteersNeeded != null ? post.volunteersNeeded : 0);
                        intent.putExtra("is_seeker", true);
                        itemView.getContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent(itemView.getContext(), ResponsesActivity.class);
                        intent.putExtra("post_id", post.postId);
                        intent.putExtra("post_title", post.title);
                        intent.putExtra("is_gig", true);
                        itemView.getContext().startActivity(intent);
                    }
                });
            }
            if (btnViewDetails != null) btnViewDetails.setVisibility(View.GONE);
        }

        private void bindApplication(Application app) {
            if (tvTitle != null) tvTitle.setText(app.postTitle != null ? app.postTitle : "Untitled");
            if (tvType  != null) tvType.setText(app.postType != null ? app.postType.toUpperCase() : "");
            if (tvLocation != null) tvLocation.setText("Pending Acceptance");

            updateProfileBlock("APPLIED AS", "Provider", null);
            updateStatusUI("PENDING");

            if (btnMessage != null) btnMessage.setVisibility(View.GONE);
            if (btnUpdate != null) {
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText("View Job");
                btnUpdate.setOnClickListener(v -> {
                    // Navigate to job detail
                    Intent intent = new Intent(itemView.getContext(), GigPostDetailActivity.class);
                    intent.putExtra("post_id", app.postId);
                    itemView.getContext().startActivity(intent);
                });
            }
            if (btnViewDetails != null) btnViewDetails.setVisibility(View.GONE);
        }

        private void updateProfileBlock(String label, String name, String rating) {
            if (tvName != null) tvName.setText(name);
            if (tvRating != null) tvRating.setText(rating != null ? rating : "N/A");
            
            View profileBlock = itemView.findViewById(R.id.profileBlock);
            if (profileBlock != null) {
                android.widget.LinearLayout inner = (android.widget.LinearLayout)
                        ((android.widget.LinearLayout) profileBlock).getChildAt(1);
                if (inner != null && inner.getChildCount() > 0) {
                    android.widget.TextView lbl = (android.widget.TextView) inner.getChildAt(0);
                    if (lbl != null) lbl.setText(label);
                }
            }
        }

        private void updateStatusUI(String status) {
            if (status == null) status = "pending";
            
            switch (status.toLowerCase()) {
                case "confirmed":
                case "in_progress":
                case "ongoing":
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
                case "active":
                    tvStatus.setText("OPEN");
                    tvStatus.setTextColor(Color.parseColor("#3B82F6"));
                    tvStatus.setBackgroundResource(R.drawable.bg_pill_in_progress); // Reuse for now
                    break;
                default:
                    tvStatus.setText(status.toUpperCase());
                    tvStatus.setTextColor(Color.GRAY);
                    break;
            }
        }
    }
}
