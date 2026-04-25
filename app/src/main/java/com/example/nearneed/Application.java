package com.example.nearneed;

/**
 * Represents an application/response to a post (Gig or Community).
 * Firestore path: applications/{applicationId}
 */
public class Application {
    public String applicationId;
    public String postId;               // Reference to the post
    public String postTitle;            // Denormalized post title for quick display
    public String postType;             // "GIG" or "COMMUNITY"
    public String applicantId;          // User applying
    public String applicantName;        // Denormalized applicant name
    public String applicantPhotoUrl;    // Denormalized applicant photo
    public String status;               // "pending", "accepted", "rejected", "completed"
    public String message;              // Optional message from applicant
    public Long appliedAt;
    public Long updatedAt;

    // Creator (post owner) info for queries
    public String creatorId;            // Post creator's user ID

    // Workflow fields
    public String providerStatus;       // For provider: "pending_response", "accepted", "rejected"
    public String seekerStatus;         // For seeker: "waiting", "accepted", "completed"
    public Long acceptedAt;
    public Long completedAt;

    // Optional fields for Gigs
    public Double proposedBudget;
    public String paymentMethod;
    public Double applicantRating;
    public String applicantLocation;
    public String applicantPhone;

    public Application() {}

    public Application(String postId, String postTitle, String postType, 
                      String applicantId, String creatorId) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.postType = postType;
        this.applicantId = applicantId;
        this.creatorId = creatorId;
        this.status = "pending";
        this.appliedAt = System.currentTimeMillis();
    }
}
