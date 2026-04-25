package com.example.nearneed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    private final androidx.lifecycle.MutableLiveData<String> name = new androidx.lifecycle.MutableLiveData<>();
    private final androidx.lifecycle.MutableLiveData<String> location = new androidx.lifecycle.MutableLiveData<>();

    public LiveData<String> getName() {
        return name;
    }

    public LiveData<String> getLocation() {
        return location;
    }

    public void saveName(String name) {
        this.name.setValue(name);
    }

    public void saveLocation(String location) {
        this.location.setValue(location);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
