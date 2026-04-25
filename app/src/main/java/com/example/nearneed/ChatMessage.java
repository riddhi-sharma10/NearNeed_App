package com.example.nearneed;

import com.google.firebase.Timestamp;

/**
 * Model representing a chat message.
 */
public class ChatMessage {
    public String messageId;
    public String senderId;
    public String receiverId;
    public Timestamp timestamp;
    public String messageText;
    public boolean isVoice;
    public boolean isOutgoing;
    public boolean isPlaying = false;
    public int progress = 0;
    public int durationSecs = 0;
    public String imageUri;
    public String audioPath;

    public ChatMessage() {}

    public ChatMessage(String messageText, boolean isVoice, boolean isOutgoing) {
        this.messageText = messageText;
        this.isVoice = isVoice;
        this.isOutgoing = isOutgoing;
        if (this.isVoice) this.durationSecs = 7;
    }
}
