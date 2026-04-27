package com.example.nearneed;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing Chat messages and metadata in Firestore.
 */
public class ChatRepository {

    private static final String MESSAGES_COLLECTION = "messages";
    private static final String CHATS_COLLECTION = "chats";

    public interface MessageListener {
        void onMessagesLoaded(List<ChatMessage> messages);
        void onError(Exception e);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Observe messages in a chat conversation.
     * Firestore path: messages/{chatId}/messages/{messageId}
     */
    public static ListenerRegistration observeMessages(String chatId, MessageListener listener) {
        if (chatId == null || listener == null) return null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(MESSAGES_COLLECTION)
                .document(chatId)
                .collection("messages")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snapshot != null) {
                        List<ChatMessage> messages = snapshot.toObjects(ChatMessage.class);
                        
                        // Client-side sort by timestamp ascending (oldest first for chat history)
                        java.util.Collections.sort(messages, (m1, m2) -> {
                            Long t1 = m1.timestamp != null ? m1.timestamp : 0L;
                            Long t2 = m2.timestamp != null ? m2.timestamp : 0L;
                            return t1.compareTo(t2);
                        });
                        
                        listener.onMessagesLoaded(messages);
                    }
                });
    }

    /**
     * Send a text message.
     */
    public static void sendMessage(String chatId, String senderId, String receiverId, String text, SaveCallback callback) {
        if (chatId == null || text == null || text.trim().isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", senderId);
        messageMap.put("messageText", text.trim());
        messageMap.put("timestamp", System.currentTimeMillis());

        db.collection(MESSAGES_COLLECTION)
                .document(chatId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener(doc -> {
                    updateChatMetadata(chatId, senderId, receiverId, text.trim());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Send a media message (image or voice).
     */
    public static void sendMediaMessage(String chatId, String senderId, String receiverId,
                                        String mediaUrl, boolean isVoice, SaveCallback callback) {
        if (chatId == null || mediaUrl == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", senderId);
        messageMap.put("messageText", "");
        messageMap.put(isVoice ? "audioPath" : "imageUri", mediaUrl);
        messageMap.put("timestamp", System.currentTimeMillis());

        db.collection(MESSAGES_COLLECTION)
                .document(chatId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener(doc -> {
                    updateChatMetadata(chatId, senderId, receiverId, isVoice ? "🎤 Voice message" : "📷 Image");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e); });
    }

    /**
     * Mark a chat conversation as read for the current user.
     */
    public static void markAsRead(String chatId) {
        if (chatId == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("unreadCount", 0);
        FirebaseFirestore.getInstance().collection(CHATS_COLLECTION)
                .document(chatId)
                .update(update);
    }

    /**
     * Update chat thread metadata for the inbox view.
     * Firestore path: chats/{chatId}
     */
    private static void updateChatMetadata(String chatId, String senderId, String receiverId, String lastMessage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> chatMeta = new HashMap<>();
        chatMeta.put("participants", Arrays.asList(senderId, receiverId));
        chatMeta.put("lastMessage", lastMessage);
        chatMeta.put("lastTimestamp", System.currentTimeMillis());

        db.collection(CHATS_COLLECTION)
                .document(chatId)
                .set(chatMeta, SetOptions.merge());
    }
}
