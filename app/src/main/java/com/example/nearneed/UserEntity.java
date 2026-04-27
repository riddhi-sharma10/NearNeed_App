package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profiles")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId;
    public String name;
    public String phone;
    public String address;
    public String profileImageUrl;
    public String bio;
    public String role;
    public double rating;
    public int jobsCompleted;

    public UserEntity() {}

    public static UserEntity fromProfile(UserProfile profile) {
        UserEntity entity = new UserEntity();
        entity.userId = profile.userId;
        entity.name = profile.name;
        entity.phone = profile.phone;
        entity.address = ""; // Location string is deprecated in schema
        entity.profileImageUrl = profile.profileImageUrl;
        entity.bio = profile.bio;
        entity.role = profile.role;
        entity.rating = profile.rating != null ? profile.rating : 0.0;
        entity.jobsCompleted = 0; // default
        return entity;
    }

    public UserProfile toProfile() {
        UserProfile profile = new UserProfile();
        profile.userId = this.userId;
        profile.name = this.name;
        profile.phone = this.phone;
        profile.profileImageUrl = this.profileImageUrl;
        profile.bio = this.bio;
        profile.role = this.role;
        profile.rating = this.rating;
        return profile;
    }
}
