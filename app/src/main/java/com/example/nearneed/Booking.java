package com.example.nearneed;

/**
 * Represents a booking (confirmed transaction after acceptance).
 * Firestore path: bookings/{bookingId}
 */
public class Booking {
    public String bookingId;
    public String postId;
    public String seekerId;
    public String providerId;
    public String status; // "upcoming" | "ongoing" | "completed"
    public String paymentStatus; // "pending" | "paid"
    public Long timestamp;

    // Denormalized fields for UI performance
    public String postTitle;
    public String postType;
    public String seekerName;
    public String providerName;
    public String applicationId;

    // Extended fields used by BookingEntity and UI
    public Long createdAt;
    public Double amount;

    public Booking() {}

    public Booking(String postId, String seekerId, String providerId) {
        this.postId = postId;
        this.seekerId = seekerId;
        this.providerId = providerId;
        this.status = "upcoming";
        this.paymentStatus = "pending";
        this.timestamp = System.currentTimeMillis();
    }
}
