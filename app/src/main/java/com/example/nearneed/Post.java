package com.example.nearneed;

import java.util.List;

/**
 * Represents a post (Gig or Community Volunteer request) stored in Firestore.
 * Firestore path: posts/{postId}
 */
public class Post {
    public String postId;
    public String userId;           // Creator's user ID
    public String title;
    public String description;
    public String type;             // "GIG" or "COMMUNITY"
    public String category;         // "High Urgency", "Normal", etc. (for GIGs)
    public String budget;           // e.g., "₹500 - 800" or "" for COMMUNITY
    public String location;         // Display string e.g., "1.5 km away"
    public Double lat;
    public Double lng;
    public String status;           // "active", "completed", "cancelled"
    public Long createdAt;
    public Long updatedAt;

    // Post Creation fields
    public String urgency;
    public String preferredDate;
    public String preferredTime;
    public String additionalNotes;
    public List<String> imageUrls;


    // COMMUNITY-specific fields
    public Integer slots;           // Total slots needed
    public Integer slotsFilled;     // How many have applied/volunteered

    // Application tracking
    public List<String> applicants; // User IDs of applicants
    public Boolean hasApplied;      // True if current user has applied
    public String applicationStatus; // "pending", "accepted", "rejected"

    // UI/Display fields (computed, not stored in Firestore)
    public String postedBy;         // Creator's name
    public Integer iconResId;       // Android drawable resource ID
    public Integer colorResId;      // Android color resource ID
    public String distance;         // Computed distance from user

    public Post() {}

    public Post(String title, String description, String type, String category, 
                String budget, Double lat, Double lng) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.budget = budget;
        this.lat = lat;
        this.lng = lng;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.slots = 0;
        this.slotsFilled = 0;
        this.hasApplied = false;
    }
}
