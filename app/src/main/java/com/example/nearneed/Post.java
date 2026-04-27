package com.example.nearneed;

import java.util.List;

/**
 * Represents a post (Gig or Community Volunteer request) stored in Firestore.
 * Firestore path: posts/{postId}
 */
public class Post {
    public String postId;
    public String type;             // "gig" | "community"
    public String title;
    public String description;
    public String createdBy;        // userId
    public Double latitude;
    public Double longitude;
    public String category;
    public String budget;           // ONLY for gigs
    public Integer volunteersNeeded; // ONLY for community
    public Long timestamp;
    public String status;           // "active" | "completed" | "cancelled"

    // UI/Legacy fields
    public String location;         // Address string
    public String urgency;
    public String preferredDate;
    public String preferredTime;
    public String additionalNotes;
    public List<String> imageUrls;
    public String distance;         
    public String urgencyColor;     
    public String postedBy;         // Denormalized name of creator
    public Integer slots;           // Legacy name for volunteersNeeded
    public Integer slotsFilled;     // How many volunteer slots have been filled

    public Post() {}

    public Post(String title, String description, String type, String category, 
                String budget, Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.budget = budget;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
        this.status = "active";
    }
}
