package com.example.nearneed;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE postId = :postId")
    List<ApplicationEntity> getApplicationsForPost(String postId);

    @Query("SELECT * FROM applications WHERE applicantId = :uid")
    List<ApplicationEntity> getUserApplications(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ApplicationEntity> applications);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ApplicationEntity application);

    @Query("DELETE FROM applications")
    void deleteAll();
}
