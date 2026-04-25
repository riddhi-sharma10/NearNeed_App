package com.example.nearneed;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings")
public class BookingEntity {
    @PrimaryKey
    @NonNull
    public String bookingId;
    public String postId;
    public String postTitle;
    public String seekerId;
    public String seekerName;
    public String providerId;
    public String providerName;
    public String status;
    public long createdAt;
    public String price;

    public BookingEntity() {}

    public static BookingEntity fromBooking(Booking booking) {
        BookingEntity entity = new BookingEntity();
        entity.bookingId = booking.bookingId;
        entity.postId = booking.postId;
        entity.postTitle = booking.postTitle;
        entity.seekerId = booking.seekerId;
        entity.seekerName = booking.seekerName;
        entity.providerId = booking.providerId;
        entity.providerName = booking.providerName;
        entity.status = booking.status;
        entity.createdAt = booking.createdAt;
        entity.price = booking.price;
        return entity;
    }

    public Booking toBooking() {
        Booking b = new Booking();
        b.bookingId = this.bookingId;
        b.postId = this.postId;
        b.postTitle = this.postTitle;
        b.seekerId = this.seekerId;
        b.seekerName = this.seekerName;
        b.providerId = this.providerId;
        b.providerName = this.providerName;
        b.status = this.status;
        b.createdAt = this.createdAt;
        b.price = this.price;
        return b;
    }
}
