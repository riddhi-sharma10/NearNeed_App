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
 * ViewModel for managing Chat messages and real-time updates.
 */
public class ChatViewModel extends AndroidViewModel {

    private MutableLiveData<List<ChatMessage>> messages;
    private MutableLiveData<String> error;
    private ListenerRegistration messageListener;
    private String currentUserId;
    private String seekerId;
    private String providerId;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        messages = new MutableLiveData<>(new ArrayList<>());
        error = new MutableLiveData<>();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public void setRoleIds(String seekerId, String providerId) {
        this.seekerId = seekerId;
        this.providerId = providerId;
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Start observing messages for a specific chat.
     */
    public void observeMessages(String chatId) {
        if (messageListener != null) {
            messageListener.remove();
        }

        messageListener = ChatRepository.observeMessages(chatId, new ChatRepository.MessageListener() {
            @Override
            public void onMessagesLoaded(List<ChatMessage> incoming) {
                // Determine isOutgoing based on current user ID
                for (ChatMessage m : incoming) {
                    m.isOutgoing = m.senderId != null && m.senderId.equals(currentUserId);
                }
                messages.setValue(incoming);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    /**
     * Send a text message.
     */
    public void sendMessage(String chatId, String receiverId, String text) {
        ChatRepository.sendMessage(chatId, currentUserId, receiverId, text, seekerId, providerId, new ChatRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                // Handled by real-time listener
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Failed to send: " + e.getMessage());
            }
        });
    }

    /**
     * Send a media message (Image or Voice).
     */
    public void sendMediaMessage(String chatId, String receiverId, String localPathOrUri, boolean isVoice) {
        if (isVoice) {
            StorageRepository.uploadAudio(localPathOrUri, new StorageRepository.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    performSendMedia(chatId, receiverId, downloadUrl, true);
                }

                @Override
                public void onFailure(Exception e) {
                    error.setValue("Audio upload failed: " + e.getMessage());
                }
            });
        } else {
            android.net.Uri imageUri = android.net.Uri.parse(localPathOrUri);
            StorageRepository.uploadImage(imageUri, "chat_images", new StorageRepository.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    performSendMedia(chatId, receiverId, downloadUrl, false);
                }

                @Override
                public void onFailure(Exception e) {
                    error.setValue("Image upload failed: " + e.getMessage());
                }
            });
        }
    }

    private void performSendMedia(String chatId, String receiverId, String downloadUrl, boolean isVoice) {
        ChatRepository.sendMediaMessage(chatId, currentUserId, receiverId, downloadUrl, isVoice, seekerId, providerId, new ChatRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                // Handled by real-time listener
            }

            @Override
            public void onFailure(Exception e) {
                error.setValue("Failed to send media: " + e.getMessage());
            }
        });
    }

    /**
     * Mark chat as read.
     */
    public void markAsRead(String chatId) {
        ChatRepository.markAsRead(chatId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}
