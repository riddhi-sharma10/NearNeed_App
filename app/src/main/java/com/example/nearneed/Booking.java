package com.example.nearneed;

/**
 * Represents a booking (confirmed transaction after acceptance).
 * Firestore path: bookings/{bookingId}
 */
public class Booking {
    public String bookingId;
    public String postId;               // Reference to the post
    public String postTitle;            // Denormalized post title
    public String postType;             // "GIG" or "COMMUNITY"
    public String seekerId;             // User seeking service
    public String seekerName;           // Denormalized seeker name
    public String providerId;           // User providing service
    public String providerName;         // Denormalized provider name
    public String status;               // "confirmed", "in_progress", "completed", "cancelled"
    public String paymentStatus;        // "pending", "completed", "failed"
    public Double amount;               // Amount in rupees
    public String currency;             // "INR" etc.
    
    // Timing
    public Long createdAt;
    public Long startTime;
    public Long endTime;
    public Long completedAt;
    public Long cancelledAt;
    
    // Additional details
    public String location;             // Service location
    public Double lat;
    public Double lng;
    public String notes;                // Optional notes/instructions
    
    // Feedback
    public Integer seekerRating;        // 1-5 stars
    public String seekerReview;
    public Integer providerRating;      // 1-5 stars
    public String providerReview;
    public Long ratedAt;
    
    // Application reference (optional)
    public String applicationId;        // Reference to the application that led to this booking
    public java.util.List<String> participants; // [seekerId, providerId] for easier querying

    public Booking() {}

    public Booking(String postId, String postTitle, String postType,
                   String seekerId, String providerId, String creatorId) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.postType = postType;
        this.seekerId = seekerId;
        this.providerId = providerId;
        this.status = "confirmed";
        this.paymentStatus = "pending";
        this.createdAt = System.currentTimeMillis();
    }
}
