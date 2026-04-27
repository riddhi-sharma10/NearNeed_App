package com.example.nearneed;

public class UserProfile {
    public String userId;
    public String name;
    public String fullName;         // Firestore field name used by UserProfileRepository
    public String email;
    public String phone;
    public String role;             // "seeker" | "provider"
    public Boolean isVerified;
    public String profileImageUrl;
    public String photoUrl;         // Firestore field name used by UserProfileRepository
    public Double latitude;
    public Double longitude;
    public Double lat;              // Firestore field name used by UserProfileRepository
    public Double lng;              // Firestore field name used by UserProfileRepository
    public String location;         // Address string used by UserProfileRepository
    public String category;         // for providers
    public Double rating;
    public String bio;
    public String dob;              // Date of birth
    public Integer jobsCompleted;   // for providers
    public Double earnings;         // for providers

    public UserProfile() {
    }
}
