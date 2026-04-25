package com.example.nearneed;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :uid")
    UserEntity getUser(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Query("DELETE FROM user_profiles")
    void deleteAll();
}
