package com.example.nearneed;

public class Response {
    private String responseId;
    private String applicantName;
    private float applicantRating;
    private String message;
    private String location;
    private long timestamp;
    private String status; // new, accepted, declined
    private int proposedBudget; // for gigs (optional)
    private String paymentMethod; // for gigs (optional)
    private String applicantPhone; // optional contact number

    public Response(String responseId, String applicantName, float applicantRating,
                   String message, String location, long timestamp, String status) {
        this(responseId, applicantName, applicantRating, message, location, timestamp, status, 0, null);
    }

    public Response(String responseId, String applicantName, float applicantRating,
                   String message, String location, long timestamp, String status,
                   int proposedBudget, String paymentMethod) {
        this(responseId, applicantName, applicantRating, message, location, timestamp, status,
            proposedBudget, paymentMethod, "");
    }

    public Response(String responseId, String applicantName, float applicantRating,
                   String message, String location, long timestamp, String status,
                   int proposedBudget, String paymentMethod, String applicantPhone) {
        this.responseId = responseId;
        this.applicantName = applicantName;
        this.applicantRating = applicantRating;
        this.message = message;
        this.location = location;
        this.timestamp = timestamp;
        this.status = status;
        this.proposedBudget = proposedBudget;
        this.paymentMethod = paymentMethod;
        this.applicantPhone = applicantPhone;
    }

    // Getters and Setters
    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public float getApplicantRating() { return applicantRating; }
    public void setApplicantRating(float applicantRating) { this.applicantRating = applicantRating; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProposedBudget() { return proposedBudget; }
    public void setProposedBudget(int proposedBudget) { this.proposedBudget = proposedBudget; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
}
