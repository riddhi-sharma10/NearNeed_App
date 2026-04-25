package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "applications")
public class ApplicationEntity {
    @PrimaryKey
    @NonNull
    public String applicationId;
    public String postId;
    public String applicantId;
    public String applicantName;
    public String status;
    public long createdAt;
    public String message;
    public double proposedBudget;

    public ApplicationEntity() {}

    public static ApplicationEntity fromApplication(Application app) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.applicationId = app.applicationId;
        entity.postId = app.postId;
        entity.applicantId = app.applicantId;
        entity.applicantName = app.applicantName;
        entity.status = app.status;
        entity.createdAt = app.createdAt;
        entity.message = app.message;
        entity.proposedBudget = app.proposedBudget;
        return entity;
    }

    public Application toApplication() {
        Application app = new Application();
        app.applicationId = this.applicationId;
        app.postId = this.postId;
        app.applicantId = this.applicantId;
        app.applicantName = this.applicantName;
        app.status = this.status;
        app.createdAt = this.createdAt;
        app.message = this.message;
        app.proposedBudget = this.proposedBudget;
        return app;
    }
}
