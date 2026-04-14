package com.example.nearneed;

public class Status {
    private String id;
    private String bookingId;
    private String statusType; // "not_started", "in_progress", "on_hold", "completed", "cancelled"
    private String description;
    private String photoUri; // URI of uploaded photo
    private long timestamp;
    private String updatedBy; // User who updated

    public Status(String id, String bookingId, String statusType, String description, String photoUri, long timestamp, String updatedBy) {
        this.id = id;
        this.bookingId = bookingId;
        this.statusType = statusType;
        this.description = description;
        this.photoUri = photoUri;
        this.timestamp = timestamp;
        this.updatedBy = updatedBy;
    }

    // Getters
    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public String getStatusType() { return statusType; }
    public String getDescription() { return description; }
    public String getPhotoUri() { return photoUri; }
    public long getTimestamp() { return timestamp; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setStatusType(String statusType) { this.statusType = statusType; }
    public void setDescription(String description) { this.description = description; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Status color mapping (resource ID values)
    public int getStatusColorResId() {
        switch (statusType) {
            case "not_started":
                return android.R.color.darker_gray;
            case "in_progress":
                return android.R.color.holo_blue_light;
            case "on_hold":
                return android.R.color.holo_orange_light;
            case "completed":
                return android.R.color.holo_green_light;
            case "cancelled":
                return android.R.color.holo_red_light;
            default:
                return android.R.color.darker_gray;
        }
    }
}
