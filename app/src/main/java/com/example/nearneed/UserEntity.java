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
        entity.name = profile.fullName;
        entity.phone = profile.phone;
        entity.address = profile.location;
        entity.profileImageUrl = profile.photoUrl;
        entity.bio = profile.bio;
        entity.role = "User"; // default
        entity.rating = 0.0; // default
        entity.jobsCompleted = 0; // default
        return entity;
    }

    public UserProfile toProfile() {
        UserProfile profile = new UserProfile();
        profile.userId = this.userId;
        profile.fullName = this.name;
        profile.phone = this.phone;
        profile.location = this.address;
        profile.photoUrl = this.profileImageUrl;
        profile.bio = this.bio;
        return profile;
    }
}
