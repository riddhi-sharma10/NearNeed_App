package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profiles")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId;
    public String fullName;
    public String phone;
    public String location;
    public String photoUrl;
    public String bio;
    public String role;
    public double rating;

    public UserEntity() {}

    public static UserEntity fromProfile(UserProfile profile) {
        UserEntity entity = new UserEntity();
        entity.userId = profile.userId;
        entity.fullName = profile.fullName;
        entity.phone = profile.phone;
        entity.location = profile.location;
        entity.photoUrl = profile.photoUrl;
        entity.bio = profile.bio;
        entity.role = profile.role;
        entity.rating = profile.rating;
        return entity;
    }

    public UserProfile toProfile() {
        UserProfile profile = new UserProfile();
        profile.userId = this.userId;
        profile.fullName = this.fullName;
        profile.phone = this.phone;
        profile.location = this.location;
        profile.photoUrl = this.photoUrl;
        profile.bio = this.bio;
        profile.role = this.role;
        profile.rating = this.rating;
        return profile;
    }
}
