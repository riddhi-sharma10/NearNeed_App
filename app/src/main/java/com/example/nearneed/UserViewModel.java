package com.example.nearneed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    public UserViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<String> getName() {
        return userRepository.getName();
    }

    public LiveData<String> getLocation() {
        return userRepository.getLocation();
    }

    public LiveData<Integer> getPostsCount() {
        return userRepository.getPostsCount();
    }

    public LiveData<Integer> getBookingsCount() {
        return userRepository.getBookingsCount();
    }

    public LiveData<Double> getRating() {
        return userRepository.getRating();
    }

    public LiveData<String> getMtdEarnings() {
        return userRepository.getMtdEarnings();
    }

    public void saveLocation(double lat, double lng) {
        userRepository.saveLocation(lat, lng);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepository.cleanup();
    }
}
