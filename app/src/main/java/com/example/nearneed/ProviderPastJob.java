package com.example.nearneed;

public class ProviderPastJob {

    private String jobId;
    private String title;
    private String customerName;
    private String status;       // "completed" or "cancelled"
    private double amount;       // e.g. 450.00, 0.00
    private String date;         // e.g. "Oct 12, 2023"
    private float rating;        // e.g. 4.9  (0 if no rating)
    private String reviewText;   // review snippet (empty if cancelled)
    private String cancellationNote; // note shown when cancelled (empty if completed)

    public ProviderPastJob(String jobId, String title, String customerName,
                           String status, double amount, String date,
                           float rating, String reviewText, String cancellationNote) {
        this.jobId = jobId;
        this.title = title;
        this.customerName = customerName;
        this.status = status;
        this.amount = amount;
        this.date = date;
        this.rating = rating;
        this.reviewText = reviewText;
        this.cancellationNote = cancellationNote;
    }

    public String getJobId()            { return jobId; }
    public String getTitle()            { return title; }
    public String getCustomerName()     { return customerName; }
    public String getStatus()           { return status; }
    public double getAmount()           { return amount; }
    public String getDate()             { return date; }
    public float getRating()            { return rating; }
    public String getReviewText()       { return reviewText; }
    public String getCancellationNote() { return cancellationNote; }

    public boolean isCompleted()  { return "completed".equalsIgnoreCase(status); }
    public boolean isCancelled()  { return "cancelled".equalsIgnoreCase(status); }
}
