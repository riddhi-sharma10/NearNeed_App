package com.example.nearneed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> locationLiveData = new MutableLiveData<>("");
    private final MutableLiveData<Integer> postsCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> bookingsCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Double> ratingLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> mtdEarningsLiveData = new MutableLiveData<>("$0");

    private ListenerRegistration firestoreListener;
    private ListenerRegistration postsListener;
    private ListenerRegistration bookingsListener;
    private ListenerRegistration providerBookingsListener;

    public UserRepository() {
        startListening();
    }

    private void startListening() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        firestoreListener = FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || !snapshot.exists()) return;

                String name = snapshot.getString("name");
                if (name != null && !name.isEmpty()) {
                    nameLiveData.postValue(name);
                }
            });

        // Listen for posts count (for seeker)
        postsListener = FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("createdBy", user.getUid())
            .addSnapshotListener((snapshot, e) -> {
                if (snapshot != null) {
                    postsCountLiveData.postValue(snapshot.size());
                }
            });

        // Listen for bookings as seeker
        bookingsListener = FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("seekerId", user.getUid())
            .addSnapshotListener((snapshot, e) -> {
                updateStats(snapshot, "providerRating");
            });

        // Listen for bookings as provider
        providerBookingsListener = FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("providerId", user.getUid())
            .addSnapshotListener((snapshot, e) -> {
                updateStats(snapshot, "seekerRating");
                calculateMTDEarnings(snapshot);
            });
    }

    private void updateStats(com.google.firebase.firestore.QuerySnapshot snapshot, String ratingField) {
        if (snapshot == null) return;
        
        bookingsCountLiveData.postValue(snapshot.size());
        
        double totalRating = 0;
        int ratedCount = 0;
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Double r = doc.getDouble(ratingField);
            if (r != null && r > 0) {
                totalRating += r;
                ratedCount++;
            }
        }
        if (ratedCount > 0) {
            ratingLiveData.postValue(totalRating / ratedCount);
        }
    }

    private void calculateMTDEarnings(com.google.firebase.firestore.QuerySnapshot snapshot) {
        if (snapshot == null) return;
        
        double mtd = 0;
        long startOfMonth = getStartOfMonthTimestamp();
        
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Long timestamp = doc.getLong("timestamp");
            Double price = doc.getDouble("price");
            if (timestamp != null && timestamp >= startOfMonth && price != null) {
                mtd += price;
            }
        }
        
        if (mtd >= 1000) {
            mtdEarningsLiveData.postValue(String.format("$%.1fk", mtd / 1000.0));
        } else {
            mtdEarningsLiveData.postValue(String.format("$%.0f", mtd));
        }
    }

    private long getStartOfMonthTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public LiveData<String> getName() {
        return nameLiveData;
    }

    public LiveData<String> getLocation() {
        return locationLiveData;
    }

    public LiveData<Integer> getPostsCount() {
        return postsCountLiveData;
    }

    public LiveData<Integer> getBookingsCount() {
        return bookingsCountLiveData;
    }

    public LiveData<Double> getRating() {
        return ratingLiveData;
    }

    public LiveData<String> getMtdEarnings() {
        return mtdEarningsLiveData;
    }

    public void saveLocation(double lat, double lng) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", lat);
        data.put("longitude", lng);
        FirebaseFirestore.getInstance()
            .collection("users").document(user.getUid())
            .set(data, SetOptions.merge());
    }

    public void cleanup() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
        if (bookingsListener != null) {
            bookingsListener.remove();
            bookingsListener = null;
        }
        if (providerBookingsListener != null) {
            providerBookingsListener.remove();
            providerBookingsListener = null;
        }
    }
}
