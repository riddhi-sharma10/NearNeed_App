package com.example.nearneed;

/**
 * Represents an application/response to a post (Gig or Community).
 * Firestore path: applications/{applicationId}
 */
public class Application {
    public String applicationId;
    public String postId;
    public String applicantId;
    public String message;
    public String status; // "pending" | "accepted" | "rejected"
    public Long timestamp;

    // Denormalized fields for UI performance
    public String postTitle;
    public String postType;
    public String applicantName;
    public String applicantPhotoUrl;

    // Extended applicant profile fields (populated from user document)
    public Double applicantRating;
    public String applicantLocation;
    public String applicantPhone;
    public Long appliedAt;
    public Double proposedBudget;
    public String paymentMethod;
    public String creatorId;

    public Application() {}

    public Application(String postId, String applicantId, String message) {
        this.postId = postId;
        this.applicantId = applicantId;
        this.message = message;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }
}
