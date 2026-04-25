package com.example.nearneed;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts")
    List<PostEntity> getAllPosts();

    @Query("SELECT * FROM posts WHERE userId = :uid")
    List<PostEntity> getUserPosts(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PostEntity> posts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PostEntity post);

    @Query("DELETE FROM posts")
    void deleteAll();
}
