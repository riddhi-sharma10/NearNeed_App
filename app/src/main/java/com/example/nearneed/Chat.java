package com.example.nearneed;

import java.util.List;

/**
 * Represents a chat session between two users.
 * Firestore path: chats/{chatId}
 */
public class Chat {
    public String chatId;
    public List<String> participants;
    public String lastMessage;
    public Long lastTimestamp;

    public Chat() {}

    public Chat(String chatId, List<String> participants, String lastMessage) {
        this.chatId = chatId;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastTimestamp = System.currentTimeMillis();
    }
}
