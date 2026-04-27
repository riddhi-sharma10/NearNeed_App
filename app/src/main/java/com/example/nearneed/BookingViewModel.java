package com.example.nearneed;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing Booking data lifecycle.
 * Centralizes all booking operations and ensures listeners are cleaned up properly.
 */
public class BookingViewModel extends AndroidViewModel {

    private MutableLiveData<List<Booking>> userBookings;
    private MutableLiveData<List<Booking>> postBookings;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    
    private ListenerRegistration userBookingsListener;
    private ListenerRegistration postBookingsListener;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        userBookings = new MutableLiveData<>(new ArrayList<>());
        postBookings = new MutableLiveData<>(new ArrayList<>());
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }

    /**
     * Get live data for user's bookings.
     */
    public LiveData<List<Booking>> getUserBookings() {
        if (userBookings == null) {
            userBookings = new MutableLiveData<>();
            userBookings.setValue(new ArrayList<>());
        }
        return userBookings;
    }

    /**
     * Get live data for bookings for a post.
     */
    public LiveData<List<Booking>> getPostBookings() {
        if (postBookings == null) {
            postBookings = new MutableLiveData<>();
            postBookings.setValue(new ArrayList<>());
        }
        return postBookings;
    }

    /**
     * Get error messages as live data.
     */
    public LiveData<String> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new MutableLiveData<>();
        }
        return errorMessage;
    }

    /**
     * Get loading state.
     */
    public LiveData<Boolean> getIsLoading() {
        if (isLoading == null) {
            isLoading = new MutableLiveData<>(false);
        }
        return isLoading;
    }

    /**
     * Observe user's bookings in real-time.
     */
    public void observeUserBookings() {
        if (userBookingsListener != null) {
            userBookingsListener.remove();
        }

        // Offline First: Load from Room first
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BookingRepository.loadBookingsFromRoom(getApplication(), uid, new BookingRepository.BookingListener() {
            @Override
            public void onBookingsLoaded(List<Booking> bookings) {
                if (userBookings.getValue() == null || userBookings.getValue().isEmpty()) {
                    userBookings.setValue(bookings);
                }
            }
            @Override
            public void onError(Exception e) {}
        });

        isLoading.setValue(true);
        userBookingsListener = BookingRepository.observeUserBookings(getApplication(), new BookingRepository.BookingListener() {
            @Override
            public void onBookingsLoaded(List<Booking> bookings) {
                userBookings.setValue(bookings);
                isLoading.setValue(false);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error loading bookings: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Observe bookings for a post in real-time.
     */
    public void observeBookingsForPost(String postId) {
        if (postBookingsListener != null) {
            postBookingsListener.remove();
        }

        isLoading.setValue(true);
        postBookingsListener = BookingRepository.observeBookingsForPost(postId, new BookingRepository.BookingListener() {
            @Override
            public void onBookingsLoaded(List<Booking> bookings) {
                postBookings.setValue(bookings);
                isLoading.setValue(false);
                errorMessage.setValue(null);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.setValue("Error loading post bookings: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Create a new booking.
     */
    public void createBooking(String postId, String postTitle, String postType, 
                              String seekerId, String providerId, String applicationId, 
                              Long scheduledDate, BookingRepository.SaveCallback callback) {
        isLoading.setValue(true);
        BookingRepository.createBooking(postId, postTitle, postType, seekerId, providerId, applicationId, scheduledDate, new BookingRepository.SaveCallback() {
            @Override
            public void onSuccess(String bookingId) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(bookingId);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to create booking: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void createBookingFromApplication(com.example.nearneed.Application app) {
        // Fetch the original post to get its scheduledDate
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("posts")
                .document(app.postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long scheduledDate = null;
                    if (documentSnapshot.exists()) {
                        scheduledDate = documentSnapshot.getLong("scheduledDate");
                    }
                    // If scheduledDate is missing in post, it defaults to System.currentTimeMillis() in createBooking
                    createBooking(app.postId, app.postTitle, app.postType, app.creatorId, app.applicantId, app.applicationId, scheduledDate, null);
                })
                .addOnFailureListener(e -> {
                    // Fallback to current time if fetch fails
                    createBooking(app.postId, app.postTitle, app.postType, app.creatorId, app.applicantId, app.applicationId, System.currentTimeMillis(), null);
                });
    }

    /**
     * Update booking status.
     */
    public void updateBookingStatus(String bookingId, String newStatus, BookingRepository.SaveCallback callback) {
        isLoading.setValue(true);
        BookingRepository.updateBookingStatus(bookingId, newStatus, new BookingRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to update booking status: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Mark payment as completed.
     */
    public void markPaymentCompleted(String bookingId, BookingRepository.SaveCallback callback) {
        isLoading.setValue(true);
        BookingRepository.markPaymentCompleted(bookingId, new BookingRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to mark payment completed: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Submit a rating for a booking.
     */
    public void submitRating(String bookingId, int rating, String review, String raterRole, BookingRepository.SaveCallback callback) {
        isLoading.setValue(true);
        BookingRepository.submitRating(bookingId, rating, review, raterRole, new BookingRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to submit rating: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Clean up listeners when ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (userBookingsListener != null) {
            userBookingsListener.remove();
            userBookingsListener = null;
        }
        if (postBookingsListener != null) {
            postBookingsListener.remove();
            postBookingsListener = null;
        }
    }
}
