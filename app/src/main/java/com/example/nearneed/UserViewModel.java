package com.example.nearneed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    public LiveData<String> getName() {
        return repository.getName();
    }

    public LiveData<String> getLocation() {
        return repository.getLocation();
    }

    public void saveLocation(String location) {
        repository.saveLocation(location);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
