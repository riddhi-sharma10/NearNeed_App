package com.example.nearneed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> locationLiveData = new MutableLiveData<>("");
    private ListenerRegistration firestoreListener;

    public UserRepository() {
        startListening();
    }

    private void startListening() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        firestoreListener = FirebaseFirestore.getInstance()
            .collection("Users").document(user.getUid())
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || !snapshot.exists()) return;

                String name = snapshot.getString("fullName");
                String location = snapshot.getString("location");

                if (name != null && !name.isEmpty()) {
                    nameLiveData.postValue(name);
                }
                if (location != null && !location.isEmpty()) {
                    locationLiveData.postValue(location);
                }
            });
    }

    public LiveData<String> getName() {
        return nameLiveData;
    }

    public LiveData<String> getLocation() {
        return locationLiveData;
    }

    public void saveLocation(String location) {
        // Optimistic update — UI reflects instantly without waiting for Firestore round-trip
        locationLiveData.setValue(location);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("location", location);
        FirebaseFirestore.getInstance()
            .collection("Users").document(user.getUid())
            .set(data, SetOptions.merge());
    }

    public void cleanup() {
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }
}
