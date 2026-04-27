package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewsAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvRating;
        private TextView tvDate;
        private TextView tvText;

        public ReviewViewHolder(@NonNull LinearLayout itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivReviewerAvatar);
            tvName = itemView.findViewById(R.id.tvReviewerName);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvText = itemView.findViewById(R.id.tvReviewText);
        }

        public void bind(Review review) {
            tvName.setText(review.getReviewerName());
            tvRating.setText(String.format("★ %.1f", review.getRating()));
            tvDate.setText(formatTime(review.getReviewDate()));
            tvText.setText(review.getReviewText());

            if (ivAvatar != null) {
                com.bumptech.glide.Glide.with(ivAvatar.getContext())
                        .load(DbConstants.getCatAvatarUrl(review.getReviewerName()))
                        .placeholder(R.drawable.ic_nav_profile)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long days = diff / (24 * 60 * 60 * 1000);

            if (days == 0) {
                return "Today";
            } else if (days == 1) {
                return "Yesterday";
            } else if (days < 30) {
                return days + " days ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
