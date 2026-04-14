package com.example.nearneed;

public class Volunteer {
    private String volunteerId;
    private String volunteerName;
    private float volunteerRating;
    private String bio;
    private String message;
    private String status; // interested, confirmed, completed
    private long volunteerDate;

    public Volunteer(String volunteerId, String volunteerName, float volunteerRating,
                    String bio, String message, String status, long volunteerDate) {
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.volunteerRating = volunteerRating;
        this.bio = bio;
        this.message = message;
        this.status = status;
        this.volunteerDate = volunteerDate;
    }

    // Getters and Setters
    public String getVolunteerId() { return volunteerId; }
    public void setVolunteerId(String volunteerId) { this.volunteerId = volunteerId; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public float getVolunteerRating() { return volunteerRating; }
    public void setVolunteerRating(float volunteerRating) { this.volunteerRating = volunteerRating; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getVolunteerDate() { return volunteerDate; }
    public void setVolunteerDate(long volunteerDate) { this.volunteerDate = volunteerDate; }
}
