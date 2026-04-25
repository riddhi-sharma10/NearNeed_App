package com.example.nearneed;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookingDao {
    @Query("SELECT * FROM bookings WHERE seekerId = :uid OR providerId = :uid")
    List<BookingEntity> getUserBookings(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookingEntity> bookings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookingEntity booking);

    @Query("DELETE FROM bookings")
    void deleteAll();
}
