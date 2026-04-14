package com.example.nearneed;

public class UpcomingJob {
    private String jobId;
    private String title;
    private String type; // "GIG" or "COMMUNITY"
    private String status; // "in_progress", "completed"
    private String assignedPerson;
    private float personRating;
    private int budget; // for gigs only
    private String paymentMethod; // for gigs only
    private long startTime;

    public UpcomingJob(String jobId, String title, String type, String status,
                      String assignedPerson, float personRating, int budget,
                      String paymentMethod, long startTime) {
        this.jobId = jobId;
        this.title = title;
        this.type = type;
        this.status = status;
        this.assignedPerson = assignedPerson;
        this.personRating = personRating;
        this.budget = budget;
        this.paymentMethod = paymentMethod;
        this.startTime = startTime;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedPerson() { return assignedPerson; }
    public void setAssignedPerson(String assignedPerson) { this.assignedPerson = assignedPerson; }

    public float getPersonRating() { return personRating; }
    public void setPersonRating(float personRating) { this.personRating = personRating; }

    public int getBudget() { return budget; }
    public void setBudget(int budget) { this.budget = budget; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
}
