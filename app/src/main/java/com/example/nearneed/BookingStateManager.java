package com.example.nearneed;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory frontend state manager for booking statuses.
 * Acts as a single source of truth for the current session.
 */
public class BookingStateManager {

    public static final String STATUS_ONGOING    = "ongoing";
    public static final String STATUS_COMPLETED  = "completed";
    public static final String STATUS_CANCELLED  = "cancelled";
    public static final String STATUS_PENDING    = "pending";

    // Singleton
    private static BookingStateManager instance;
    private final Map<String, String> bookingStatuses = new HashMap<>();

    private BookingStateManager() {}

    public static BookingStateManager getInstance() {
        if (instance == null) instance = new BookingStateManager();
        return instance;
    }

    /** Store or update the status of a booking by its id. */
    public void setStatus(String bookingId, String status) {
        bookingStatuses.put(bookingId, status);
    }

    /** Get the current status of a booking, defaulting to ONGOING. */
    public String getStatus(String bookingId) {
        return bookingStatuses.getOrDefault(bookingId, STATUS_ONGOING);
    }

    /** Convenience check */
    public boolean isCancelled(String bookingId) {
        return STATUS_CANCELLED.equals(bookingStatuses.get(bookingId));
    }

    public boolean isCompleted(String bookingId) {
        return STATUS_COMPLETED.equals(bookingStatuses.get(bookingId));
    }
}
