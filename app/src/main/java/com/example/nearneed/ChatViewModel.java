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

    public ChatViewModel(@NonNull Application application) {
        super(application);
        messages = new MutableLiveData<>(new ArrayList<>());
        error = new MutableLiveData<>();
        currentUserId = FirebaseAuth.getInstance().getUid();
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
        ChatRepository.sendMessage(chatId, currentUserId, receiverId, text, new ChatRepository.SaveCallback() {
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
