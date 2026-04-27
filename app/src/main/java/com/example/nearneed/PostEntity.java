package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class PostEntity {
    @PrimaryKey
    @NonNull
    public String postId;
    public String userId;
    public String title;
    public String description;
    public String type;
    public String category;
    public String budget;
    public double lat;
    public double lng;
    public String status;
    public long createdAt;

    public PostEntity() {}

    public static PostEntity fromPost(Post post) {
        PostEntity entity = new PostEntity();
        entity.postId = post.postId;
        entity.userId = post.createdBy;
        entity.title = post.title;
        entity.description = post.description;
        entity.type = post.type;
        entity.category = post.category;
        entity.budget = post.budget;
        entity.lat = post.latitude != null ? post.latitude : 0.0;
        entity.lng = post.longitude != null ? post.longitude : 0.0;
        entity.status = post.status;
        entity.createdAt = post.timestamp != null ? post.timestamp : 0L;
        return entity;
    }

    public Post toPost() {
        Post post = new Post();
        post.postId = this.postId;
        post.createdBy = this.userId;
        post.title = this.title;
        post.description = this.description;
        post.type = this.type;
        post.category = this.category;
        post.budget = this.budget;
        post.latitude = this.lat;
        post.longitude = this.lng;
        post.status = this.status;
        post.timestamp = this.createdAt;
        return post;
    }
}
