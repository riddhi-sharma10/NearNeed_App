package com.example.nearneed;

/**
 * Model representing a chat message.
 * Firestore path: messages/{chatId}/messages/{messageId}
 */
public class ChatMessage {
    public String messageId;
    public String senderId;
    public String messageText;
    public Long timestamp;

    // Media fields
    public String audioPath;
    public String imageUri;

    // UI-only state fields (transient, not stored in Firestore)
    public transient boolean isOutgoing;
    public transient boolean isVoice;
    public transient boolean isPlaying;
    public transient int progress;
    public transient int durationSecs;

    public ChatMessage() {}

    public ChatMessage(String senderId, String messageText) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String text, boolean isVoice, boolean isOutgoing) {
        this.messageText = text;
        this.isVoice = isVoice;
        this.isOutgoing = isOutgoing;
        this.timestamp = System.currentTimeMillis();
    }
}
