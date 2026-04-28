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
 * ViewModel for managing Application data lifecycle.
 * Centralizes all application operations and ensures listeners are cleaned up properly.
 */
public class ApplicationViewModel extends AndroidViewModel {

    private MutableLiveData<List<com.example.nearneed.Application>> postApplications;
    private MutableLiveData<List<com.example.nearneed.Application>> userApplications;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    
    private ListenerRegistration postApplicationsListener;
    private ListenerRegistration userApplicationsListener;

    public ApplicationViewModel(@NonNull Application application) {
        super(application);
        postApplications = new MutableLiveData<>(new ArrayList<>());
        userApplications = new MutableLiveData<>(new ArrayList<>());
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }

    /**
     * Get live data for applications to a post (creator view).
     */
    public LiveData<List<com.example.nearneed.Application>> getPostApplications() {
        if (postApplications == null) {
            postApplications = new MutableLiveData<>();
            postApplications.setValue(new ArrayList<>());
        }
        return postApplications;
    }

    /**
     * Get live data for user's own applications.
     */
    public LiveData<List<com.example.nearneed.Application>> getUserApplications() {
        if (userApplications == null) {
            userApplications = new MutableLiveData<>();
            userApplications.setValue(new ArrayList<>());
        }
        return userApplications;
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
     * Observe applications to a post in real-time (creator view).
     */
    public void observeApplicationsForPost(String postId) {
        if (postApplicationsListener != null) {
            postApplicationsListener.remove();
        }

        // Offline First: Load from Room first
        ApplicationRepository.loadApplicationsFromRoom(getApplication(), postId, new ApplicationRepository.ApplicationListener() {
            @Override
            public void onApplicationsLoaded(List<com.example.nearneed.Application> applications) {
                if (postApplications.getValue() == null || postApplications.getValue().isEmpty()) {
                    postApplications.setValue(applications);
                }
            }
            @Override
            public void onError(Exception e) {}
        });

        isLoading.setValue(true);
        postApplicationsListener = ApplicationRepository.observeApplicationsForPost(getApplication(), postId, new ApplicationRepository.ApplicationListener() {
            @Override
            public void onApplicationsLoaded(List<com.example.nearneed.Application> applications) {
                if (postApplications != null) {
                    postApplications.setValue(applications);
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
                if (errorMessage != null) {
                    errorMessage.setValue(null);
                }
            }

            @Override
            public void onError(Exception e) {
                if (errorMessage != null) {
                    errorMessage.setValue("Error loading applications: " + e.getMessage());
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
            }
        });
    }

    /**
     * Observe user's own applications in real-time.
     */
    public void observeUserApplications() {
        if (userApplicationsListener != null) {
            userApplicationsListener.remove();
        }

        // Offline First: Load from Room first
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (userApplications != null) {
                userApplications.setValue(new ArrayList<>());
            }
            return;
        }
        String uid = currentUser.getUid();
        ApplicationRepository.loadUserApplicationsFromRoom(getApplication(), uid, new ApplicationRepository.ApplicationListener() {
            @Override
            public void onApplicationsLoaded(List<com.example.nearneed.Application> applications) {
                if (userApplications != null && (userApplications.getValue() == null || userApplications.getValue().isEmpty())) {
                    userApplications.setValue(applications);
                }
            }
            @Override
            public void onError(Exception e) {}
        });

        if (isLoading != null) {
            isLoading.setValue(true);
        }
        userApplicationsListener = ApplicationRepository.observeUserApplications(getApplication(), new ApplicationRepository.ApplicationListener() {
            @Override
            public void onApplicationsLoaded(List<com.example.nearneed.Application> applications) {
                if (userApplications != null) {
                    userApplications.setValue(applications);
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
                if (errorMessage != null) {
                    errorMessage.setValue(null);
                }
            }

            @Override
            public void onError(Exception e) {
                if (errorMessage != null) {
                    errorMessage.setValue("Error loading your applications: " + e.getMessage());
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
            }
        });
    }

    /**
     * Submit a new application.
     */
    public void submitApplication(String postId, String postTitle, String postType, 
                                  String creatorId, String message, String budget, 
                                  String paymentMethod, com.example.nearneed.ApplicationRepository.SaveCallback callback) {
        if (isLoading != null) {
            isLoading.setValue(true);
        }
        com.example.nearneed.ApplicationRepository.submitApplication(postId, postTitle, postType, creatorId, message, budget, paymentMethod, new com.example.nearneed.ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String applicationId) {
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
                if (errorMessage != null) {
                    errorMessage.setValue(null);
                }
                if (callback != null) callback.onSuccess(applicationId);
            }

            @Override
            public void onFailure(Exception e) {
                if (errorMessage != null) {
                    errorMessage.setValue("Failed to submit application: " + e.getMessage());
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Get applications for a specific post (helper for observing).
     */
    public LiveData<List<com.example.nearneed.Application>> getApplicationsByPost(String postId) {
        return postApplications;
    }

    /**
     * Observe applications for a post (for UI convenience).
     */
    public void observeApplicationsByPost(androidx.lifecycle.LifecycleOwner owner, String postId) {
        observeApplicationsForPost(postId);
    }

    /**
     * Update application status (helper for UI).
     */
    public void updateApplicationStatus(String applicationId, String status) {
        if (isLoading != null) {
            isLoading.setValue(true);
        }
        com.example.nearneed.ApplicationRepository.updateApplicationStatus(applicationId, status, new com.example.nearneed.ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
                if (errorMessage != null) {
                    errorMessage.setValue(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (errorMessage != null) {
                    errorMessage.setValue("Failed to update status: " + e.getMessage());
                }
                if (isLoading != null) {
                    isLoading.setValue(false);
                }
            }
        });
    }

    /**
     * Accept an application.
     */
    public void acceptApplication(String applicationId, ApplicationRepository.SaveCallback callback) {
        isLoading.setValue(true);
        ApplicationRepository.acceptApplication(applicationId, new ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to accept application: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Reject an application.
     */
    public void rejectApplication(String applicationId, ApplicationRepository.SaveCallback callback) {
        isLoading.setValue(true);
        ApplicationRepository.rejectApplication(applicationId, new ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to reject application: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Withdraw an application.
     */
    public void withdrawApplication(String applicationId, ApplicationRepository.SaveCallback callback) {
        isLoading.setValue(true);
        ApplicationRepository.withdrawApplication(applicationId, new ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to withdraw application: " + e.getMessage());
                isLoading.setValue(false);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Complete an application.
     */
    public void completeApplication(String applicationId, ApplicationRepository.SaveCallback callback) {
        isLoading.setValue(true);
        ApplicationRepository.completeApplication(applicationId, new ApplicationRepository.SaveCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                errorMessage.setValue(null);
                if (callback != null) callback.onSuccess(id);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Failed to complete application: " + e.getMessage());
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
        if (postApplicationsListener != null) {
            postApplicationsListener.remove();
            postApplicationsListener = null;
        }
        if (userApplicationsListener != null) {
            userApplicationsListener.remove();
            userApplicationsListener = null;
        }
    }
}
