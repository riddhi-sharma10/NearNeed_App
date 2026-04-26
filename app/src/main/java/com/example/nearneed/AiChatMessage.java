package com.example.nearneed;

import android.net.Uri;

public class AiChatMessage {
    public static final int TYPE_USER   = 0;
    public static final int TYPE_BOT    = 1;
    public static final int TYPE_TYPING = 2;

    private final String text;
    private final int    type;
    private final Uri    imageUri; // nullable — only set for user messages with an image

    public AiChatMessage(String text, int type) {
        this(text, type, null);
    }

    public AiChatMessage(String text, int type, Uri imageUri) {
        this.text     = text;
        this.type     = type;
        this.imageUri = imageUri;
    }

    public String  getText()     { return text; }
    public int     getType()     { return type; }
    public Uri     getImageUri() { return imageUri; }
    public boolean isUser()      { return type == TYPE_USER; }
    public boolean isTyping()    { return type == TYPE_TYPING; }
    public boolean hasImage()    { return imageUri != null; }
}
