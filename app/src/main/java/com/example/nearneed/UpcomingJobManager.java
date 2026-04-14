package com.example.nearneed;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for persisting and retrieving upcoming jobs from SharedPreferences.
 */
public class UpcomingJobManager {
    private static final String PREF_NAME = "upcoming_jobs";
    private static final String KEY_JOBS = "jobs_list";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public UpcomingJobManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Save a new upcoming job
     */
    public void saveJob(UpcomingJob job) {
        List<UpcomingJob> jobs = getAllJobs();
        jobs.add(job);
        saveAllJobs(jobs);
    }

    /**
     * Get all upcoming jobs
     */
    public List<UpcomingJob> getAllJobs() {
        String json = sharedPreferences.getString(KEY_JOBS, "[]");
        Type type = new TypeToken<List<UpcomingJob>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Get jobs by type (GIG or COMMUNITY)
     */
    public List<UpcomingJob> getJobsByType(String type) {
        List<UpcomingJob> allJobs = getAllJobs();
        List<UpcomingJob> filtered = new ArrayList<>();
        for (UpcomingJob job : allJobs) {
            if (type.equals(job.getType())) {
                filtered.add(job);
            }
        }
        return filtered;
    }

    /**
     * Get jobs by status (in_progress, completed)
     */
    public List<UpcomingJob> getJobsByStatus(String status) {
        List<UpcomingJob> allJobs = getAllJobs();
        List<UpcomingJob> filtered = new ArrayList<>();
        for (UpcomingJob job : allJobs) {
            if (status.equals(job.getStatus())) {
                filtered.add(job);
            }
        }
        return filtered;
    }

    /**
     * Update a job's status
     */
    public void updateJobStatus(String jobId, String newStatus) {
        List<UpcomingJob> jobs = getAllJobs();
        for (UpcomingJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                job.setStatus(newStatus);
                break;
            }
        }
        saveAllJobs(jobs);
    }

    /**
     * Get a specific job by ID
     */
    public UpcomingJob getJobById(String jobId) {
        List<UpcomingJob> jobs = getAllJobs();
        for (UpcomingJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    /**
     * Delete a job
     */
    public void deleteJob(String jobId) {
        List<UpcomingJob> jobs = getAllJobs();
        jobs.removeIf(job -> job.getJobId().equals(jobId));
        saveAllJobs(jobs);
    }

    /**
     * Save all jobs
     */
    private void saveAllJobs(List<UpcomingJob> jobs) {
        String json = gson.toJson(jobs);
        sharedPreferences.edit().putString(KEY_JOBS, json).apply();
    }

    /**
     * Clear all jobs
     */
    public void clearAllJobs() {
        sharedPreferences.edit().remove(KEY_JOBS).apply();
    }
}
