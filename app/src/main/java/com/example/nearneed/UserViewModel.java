package com.example.nearneed;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.Map;

/**
 * ViewModel for managing User Profile data lifecycle.
 * Centralizes all user profile operations and ensures listeners are cleaned up properly.
 */
public class UserViewModel extends ViewModel {

    private MutableLiveData<UserProfile> currentUserProfile;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    
    private ListenerRegistration profileListener;

    /**
     * Get live data for current user profile.
     */
    public LiveData<UserProfile> getCurrentUserProfile() {
        if (currentUserProfile == null) {
            currentUserProfile = new MutableLiveData<>();
        }
        return currentUserProfile;
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
     * Observe current user profile in real-time.
     */
    public void observeCurrentUserProfile(Context context) {
        if (profileListener != null) {
            profileListener.remove();
        }

        isLoading.setValue(true);
        profileListener = UserProfileRepository.observeCurrentUserProfile(context, new UserProfileRepository.ProfileListener() {
            @Override
            public void onProfileChanged(UserProfile profile) {
                if (profile != null) {
                    currentUserProfile.setValue(profile);
                    isLoading.setValue(false);
                    errorMessage.setValue(null);
                } else {
                    errorMessage.setValue("No profile found");
                    isLoading.setValue(false);
                }
            }
        });
    }

    /**
     * Save current user profile.
     */
    public void saveCurrentUserProfile(Map<String, Object> updates, UserProfileRepository.SaveCallback callback) {
        isLoading.setValue(true);
        UserProfileRepository.saveCurrentUserProfile(updates, new UserProfileRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to save profile: " + e.getMessage());
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
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }
}
