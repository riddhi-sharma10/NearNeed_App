package com.example.nearneed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiChatActivity extends AppCompatActivity {

    private static final String TAG = "NearNeedChatbot";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    // UI
    private RecyclerView     rvChat;
    private EditText         etMessage;
    private ImageButton      btnSend, btnAttach, btnRemoveImage;
    private FrameLayout      imagePreviewContainer;
    private ImageView        ivPreview;

    // State
    private AiChatAdapter        adapter;
    private final List<AiChatMessage> messages = new ArrayList<>();
    private final JSONArray conversationHistory  = new JSONArray();
    private Uri pendingImageUri = null; // image waiting to be sent

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30,  TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        pendingImageUri = uri;
                        imagePreviewContainer.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).centerCrop().into(ivPreview);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        rvChat                = findViewById(R.id.rvAiChat);
        etMessage             = findViewById(R.id.etMessage);
        btnSend               = findViewById(R.id.btnSend);
        btnAttach             = findViewById(R.id.btnAttach);
        btnRemoveImage        = findViewById(R.id.btnRemoveImage);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        ivPreview             = findViewById(R.id.ivPreview);

        adapter = new AiChatAdapter(messages);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        rvChat.setLayoutManager(llm);
        rvChat.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> openImagePicker());
        btnRemoveImage.setOnClickListener(v -> clearPendingImage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        initConversationContext();
        addBotMessage("👋 Hi! I'm the NearNeed Chatbot.\n\nI can help with bookings, payments, ID verification, posting gigs, and more. You can also attach an image using the 📎 button!");
    }

    // ─── Image picker ─────────────────────────────────────────────────────────

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePicker.launch(Intent.createChooser(intent, "Choose an image"));
    }

    private void clearPendingImage() {
        pendingImageUri = null;
        imagePreviewContainer.setVisibility(View.GONE);
    }

    // ─── System context baked into history ───────────────────────────────────

    private void initConversationContext() {
        try {
            JSONObject ctxUser = new JSONObject();
            ctxUser.put("role", "user");
            JSONArray up = new JSONArray();
            up.put(new JSONObject().put("text",
                    "You are the NearNeed support chatbot. NearNeed is a hyperlocal gig platform in India. " +
                    "Help with: bookings, payments, ID verification, posting gigs, community posts, profile, messaging. " +
                    "RULES: " +
                    "1. Be extremely concise and brief. Answer exactly what is asked and stop. Do NOT give long explanations. " +
                    "2. DO NOT use any markdown formatting. No asterisks (**), no bold text, no bullet points, no hashes. Use plain text only. " +
                    "3. If unsure, say: contact support@nearneed.in"));
            ctxUser.put("parts", up);
            conversationHistory.put(ctxUser);

            JSONObject ctxModel = new JSONObject();
            ctxModel.put("role", "model");
            JSONArray mp = new JSONArray();
            mp.put(new JSONObject().put("text",
                    "Got it! I'm the NearNeed support chatbot, here to help with your queries."));
            ctxModel.put("parts", mp);
            conversationHistory.put(ctxModel);
        } catch (Exception e) {
            Log.e(TAG, "Context init error", e);
        }
    }

    // ─── Send flow ────────────────────────────────────────────────────────────

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        Uri    imageUri = pendingImageUri;
        if (text.isEmpty() && imageUri == null) return;

        etMessage.setText("");
        btnSend.setEnabled(false);
        clearPendingImage();
        addUserMessage(text, imageUri);

        // Image → Gemini Vision
        if (imageUri != null) {
            int ti = addTypingIndicator();
            callGeminiVision(text.isEmpty() ? "Describe this image in context of the NearNeed app." : text, imageUri, ti);
            return;
        }

        int typingIndex = addTypingIndicator();
        String q = text.toLowerCase(Locale.ROOT);

        // If personal data query → fetch Firestore first, then call Gemini with real context
        if (isPersonalQuery(q)) {
            fetchBookingsAndCallGemini(text, typingIndex);
        } else {
            addToHistoryAndCallGemini(text, typingIndex);
        }
    }

    /** True when user is asking about THEIR specific data, not generic how-to. */
    private boolean isPersonalQuery(String q) {
        boolean personalPronoun = word(q, "my", "mine", "i have", "current", "status");
        boolean dataTopic = word(q, "booking", "bookings", "order", "provider",
                "complaint", "issue", "problem", "payment", "service", "cancel", "cancelled", "past", "gig", "gigs");
        return personalPronoun && dataTopic;
    }

    /** Word-boundary safe match — avoids "id" matching inside "provider". */
    private boolean word(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.equals(kw)
                    || input.startsWith(kw + " ")
                    || input.endsWith(" " + kw)
                    || input.contains(" " + kw + " ")
                    || input.contains(" " + kw + "s ")
                    || input.contains(" " + kw + "s")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch the user's real bookings (as seeker + provider) from Firestore,
     * build a context summary, inject into Gemini prompt.
     */
    private void fetchBookingsAndCallGemini(String userQuestion, int typingIndex) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            addToHistoryAndCallGemini(userQuestion, typingIndex);
            return;
        }
        String uid = user.getUid();
        String userName = user.getDisplayName() != null ? user.getDisplayName() : "the user";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Fetch bookings where user is seeker
        db.collection("bookings")
                .whereEqualTo("seekerId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(seekerSnap -> {
                    StringBuilder ctx = new StringBuilder();
                    ctx.append("User name: ").append(userName).append("\n");
                    ctx.append("Bookings as SEEKER (jobs they hired someone for):\n");
                    int count = 0;
                    for (QueryDocumentSnapshot doc : seekerSnap) {
                        Booking b = doc.toObject(Booking.class);
                        ctx.append(++count).append(". Service: \"").append(b.postTitle)
                                .append("\" | Status: ").append(b.status)
                                .append(" | Provider: ").append(b.providerName != null ? b.providerName : "N/A")
                                .append(" | Date: ").append(b.timestamp != null ? sdf.format(new Date(b.timestamp)) : "?")
                                .append("\n");
                    }
                    if (count == 0) ctx.append("No bookings found as seeker.\n");

                    // Now fetch as provider
                    db.collection("bookings")
                            .whereEqualTo("providerId", uid)
                            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(20)
                            .get()
                            .addOnSuccessListener(providerSnap -> {
                                ctx.append("Bookings as PROVIDER (jobs they accepted):\n");
                                int pc = 0;
                                for (QueryDocumentSnapshot doc : providerSnap) {
                                    Booking b = doc.toObject(Booking.class);
                                    ctx.append(++pc).append(". Service: \"").append(b.postTitle)
                                            .append("\" | Status: ").append(b.status)
                                            .append(" | Seeker: ").append(b.seekerName != null ? b.seekerName : "N/A")
                                            .append(" | Date: ").append(b.timestamp != null ? sdf.format(new Date(b.timestamp)) : "?")
                                            .append("\n");
                                }
                                if (pc == 0) ctx.append("No bookings found as provider.\n");

                                // Build enriched question with real data
                                String enriched = "[REAL USER DATA FROM DATABASE]\n" + ctx +
                                        "\n[USER'S QUESTION]: " + userQuestion +
                                        "\n\nUsing the real data above, answer the user's question precisely. " +
                                        "If asked about a specific gig or past/cancelled booking, provide a brief summary of its details (status, provider/seeker, date, amount). " +
                                        "Refer to their actual booking details.";
                                addToHistoryAndCallGemini(enriched, typingIndex);
                            })
                            .addOnFailureListener(e -> addToHistoryAndCallGemini(userQuestion, typingIndex));
                })
                .addOnFailureListener(e -> addToHistoryAndCallGemini(userQuestion, typingIndex));
    }

    private void addToHistoryAndCallGemini(String text, int typingIndex) {
        try {
            JSONObject userTurn = new JSONObject();
            userTurn.put("role", "user");
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", text));
            userTurn.put("parts", parts);
            conversationHistory.put(userTurn);
        } catch (Exception ignored) {}
        callGeminiText(typingIndex);
    }

    // ─── Gemini — text (multi-turn history) ──────────────────────────────────

    private void callGeminiText(int typingIndex) {
        try {
            JSONObject body = new JSONObject();
            body.put("contents", conversationHistory);
            JSONObject gc = new JSONObject();
            gc.put("maxOutputTokens", 4096);
            gc.put("temperature", 0.7);
            body.put("generationConfig", gc);

            enqueueGemini(body.toString(), typingIndex, true);
        } catch (Exception e) {
            Log.e(TAG, "Build error", e);
            handleGeminiError(typingIndex);
        }
    }

    // ─── Gemini Vision — image + optional text (single-turn) ─────────────────

    private void callGeminiVision(String prompt, Uri imageUri, int typingIndex) {
        new Thread(() -> {
            try {
                byte[] imageBytes = compressImage(imageUri);
                String base64     = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                String mimeType   = getContentResolver().getType(imageUri);
                if (mimeType == null) mimeType = "image/jpeg";

                JSONObject body = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject userTurn = new JSONObject();
                userTurn.put("role", "user");
                JSONArray parts = new JSONArray();
                parts.put(new JSONObject().put("text", prompt));
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", mimeType);
                inlineData.put("data", base64);
                parts.put(new JSONObject().put("inline_data", inlineData));
                userTurn.put("parts", parts);
                contents.put(userTurn);
                body.put("contents", contents);

                JSONObject gc = new JSONObject();
                gc.put("maxOutputTokens", 4096);
                gc.put("temperature", 0.5);
                body.put("generationConfig", gc);

                enqueueGemini(body.toString(), typingIndex, false);

            } catch (Exception e) {
                Log.e(TAG, "Vision error", e);
                mainHandler.post(() -> handleGeminiError(typingIndex));
            }
        }).start();
    }

    // ─── Shared OkHttp call ───────────────────────────────────────────────────

    private void enqueueGemini(String bodyJson, int typingIndex, boolean saveToHistory) {
        Request request = new Request.Builder()
                .url(GEMINI_URL + BuildConfig.GEMINI_API_KEY)
                .post(RequestBody.create(bodyJson,
                        MediaType.parse("application/json; charset=utf-8")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Call call,
                                  @androidx.annotation.NonNull IOException e) {
                Log.e(TAG, "Network failure: " + e.getMessage());
                mainHandler.post(() -> handleGeminiError(typingIndex));
            }

            @Override
            public void onResponse(@androidx.annotation.NonNull Call call,
                                   @androidx.annotation.NonNull Response response) throws IOException {
                String rb = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Gemini (" + response.code() + "): " + rb);
                mainHandler.post(() -> {
                    removeTypingIndicator(typingIndex);
                    btnSend.setEnabled(true);
                    String reply = parseGeminiReply(rb);
                    if (reply == null) return;
                    addBotMessage(reply);
                    if (saveToHistory) {
                        try {
                            JSONObject mt = new JSONObject();
                            mt.put("role", "model");
                            JSONArray p = new JSONArray();
                            p.put(new JSONObject().put("text", reply));
                            mt.put("parts", p);
                            conversationHistory.put(mt);
                        } catch (Exception ignored) {}
                    }
                });
            }
        });
    }

    // ─── Parse Gemini response ────────────────────────────────────────────────

    private String parseGeminiReply(String rb) {
        try {
            JSONObject json = new JSONObject(rb);
            if (json.has("error")) {
                Log.e(TAG, "API error: " + json.getJSONObject("error").optString("message"));
                addBotMessage("I'm having trouble reaching AI right now — but I can still answer common questions! Try asking about bookings, verification, or payments.");
                return null;
            }
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                addBotMessage("I couldn't generate a response. Try rephrasing your question!");
                return null;
            }
            String finish = candidates.getJSONObject(0).optString("finishReason", "");
            if ("SAFETY".equals(finish)) {
                addBotMessage("I can't respond to that. Please ask something about the NearNeed app!");
                return null;
            }
            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            if (content == null) { addBotMessage("Empty response. Please try again!"); return null; }
            JSONArray parts = content.optJSONArray("parts");
            if (parts == null || parts.length() == 0) { addBotMessage("Empty response. Please try again!"); return null; }
            String reply = parts.getJSONObject(0).optString("text", "").trim();
            // Strip out Markdown asterisks and hashes so they don't appear in the plain TextView
            reply = reply.replaceAll("\\*", "").replaceAll("#", "");
            
            if (reply.isEmpty()) { addBotMessage("Empty response. Please try again!"); return null; }
            return reply;
        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            addBotMessage("Something went wrong. Please try again!");
            return null;
        }
    }

    private void handleGeminiError(int typingIndex) {
        removeTypingIndicator(typingIndex);
        btnSend.setEnabled(true);
        addBotMessage("⚠️ No response from server. Check your internet and try again.");
    }

    // ─── Image helpers ────────────────────────────────────────────────────────

    private byte[] compressImage(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap bm = BitmapFactory.decodeStream(is);
        int maxPx = 1024;
        if (bm.getWidth() > maxPx || bm.getHeight() > maxPx) {
            float scale = Math.min((float) maxPx / bm.getWidth(), (float) maxPx / bm.getHeight());
            bm = Bitmap.createScaledBitmap(bm,
                    (int)(bm.getWidth() * scale), (int)(bm.getHeight() * scale), true);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, out);
        return out.toByteArray();
    }

    // Local FAQ removed — Gemini handles all responses with real context injected above.

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void addUserMessage(String text, Uri imageUri) {
        messages.add(new AiChatMessage(text, AiChatMessage.TYPE_USER, imageUri));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messages.add(new AiChatMessage(text, AiChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private int addTypingIndicator() {
        messages.add(new AiChatMessage("", AiChatMessage.TYPE_TYPING));
        int idx = messages.size() - 1;
        adapter.notifyItemInserted(idx);
        scrollToBottom();
        return idx;
    }

    private void removeTypingIndicator(int index) {
        if (index >= 0 && index < messages.size() && messages.get(index).isTyping()) {
            messages.remove(index);
            adapter.notifyItemRemoved(index);
        }
    }

    private void scrollToBottom() {
        rvChat.post(() -> rvChat.smoothScrollToPosition(Math.max(0, messages.size() - 1)));
    }
}
