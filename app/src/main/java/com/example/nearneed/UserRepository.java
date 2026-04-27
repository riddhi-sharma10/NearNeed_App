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
            .collection("users").document(user.getUid())
            .addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || !snapshot.exists()) return;

                String name = snapshot.getString("name");
                // Note: The new schema doesn't have a single 'location' field, but lat/lng.
                // For now, we'll keep the LiveData for compatibility if needed, or just let it be.
                
                if (name != null && !name.isEmpty()) {
                    nameLiveData.postValue(name);
                }
            });
    }

    public LiveData<String> getName() {
        return nameLiveData;
    }

    public LiveData<String> getLocation() {
        return locationLiveData;
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
    }
}
