package com.example.nearneed;

/**
 * Data model for User Reviews/Ratings.
 * Firestore: reviews/{reviewId}
 */
public class Review {
    public String reviewId;
    public String bookingId;
    public String postId;
    public String reviewerId;
    public String revieweeId; // The person being rated
    public float rating;      // 1.0 to 5.0
    public String comment;
    public long createdAt;

    public Review() {
        // Required for Firestore
    }

    public String reviewerName; // Added for UI convenience

    public Review(String reviewerName, float rating, String comment, long createdAt) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review(String bookingId, String postId, String reviewerId, String revieweeId, float rating, String comment) {
        this.bookingId = bookingId;
        this.postId = postId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
    }

    public String getReviewerName() { return reviewerName != null ? reviewerName : "Anonymous"; }
    public float getRating() { return rating; }
    public String getReviewText() { return comment; }
    public long getReviewDate() { return createdAt; }

}
