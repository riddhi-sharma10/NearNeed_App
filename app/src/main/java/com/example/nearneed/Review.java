package com.example.nearneed;

public class Review {
    private String reviewerName;
    private float rating;
    private String reviewText;
    private long reviewDate;

    public Review(String reviewerName, float rating, String reviewText, long reviewDate) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    // Getters and Setters
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public long getReviewDate() { return reviewDate; }
    public void setReviewDate(long reviewDate) { this.reviewDate = reviewDate; }
}
