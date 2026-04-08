package com.example.nearneed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private EditText etMessageInput;
    private ImageView btnSend, btnAdd, btnBack;
    private TextView tvChatName;
    private TextView tvChatStatus;
    
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvMessages = findViewById(R.id.rvMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        String chatTime = getIntent().getStringExtra("CHAT_TIME");
        boolean isOnline = getIntent().getBooleanExtra("CHAT_ONLINE", false);
        String chatSnippet = getIntent().getStringExtra("CHAT_SNIPPET");

        if (chatName != null) {
            tvChatName.setText(chatName);
        }
        
        if (isOnline) {
            tvChatStatus.setText("Online");
            tvChatStatus.setTextColor(android.graphics.Color.parseColor("#10B981")); // theme community green roughly
        } else {
            tvChatStatus.setText(chatTime != null ? "Seen " + chatTime : "Offline");
            tvChatStatus.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        }

        btnBack.setOnClickListener(v -> finish());

        // Realistic alternating conversation
        messageList.add(new ChatMessage("Hey! Are you available today?", false, false));       // received
        messageList.add(new ChatMessage("Yeah, what's up?", false, true));                      // sent
        messageList.add(new ChatMessage("I need help with the plumbing at my place.", false, false)); // received
        messageList.add(new ChatMessage("That would be amazing, thank you!", false, true));     // sent
        messageList.add(new ChatMessage("I can come by around 3 PM if that works?", false, false)); // received
        messageList.add(new ChatMessage("3 PM works perfectly for me 👍", false, true));        // sent
        messageList.add(new ChatMessage("Great! Do I need to get any parts?", false, false));   // received
        messageList.add(new ChatMessage("Just bring the wrench set, I have everything else.", false, true)); // sent
        messageList.add(new ChatMessage("Perfect. See you then!", false, false));               // received
        messageList.add(new ChatMessage("See you! 🔧", false, true));                          // sent

        // If opened from messages list with a snippet, show it too
        if (chatSnippet != null && !chatSnippet.isEmpty()) {
            messageList.add(new ChatMessage(chatSnippet, false, false));
        }

        adapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // messages start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
        // Scroll to the latest message
        rvMessages.scrollToPosition(messageList.size() - 1);

        // SEND button logic - Send text message
        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(new ChatMessage(text, false, true));
                etMessageInput.setText("");
            }
        });

        // Toggle between Send and Mic buttons based on input text
        final ImageView btnMicLocal = findViewById(R.id.btnMic);
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnSend.setVisibility(View.VISIBLE);
                    if (btnMicLocal != null) btnMicLocal.setVisibility(View.GONE);
                } else {
                    btnSend.setVisibility(View.GONE);
                    if (btnMicLocal != null) btnMicLocal.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize button visibility
        btnSend.setVisibility(View.GONE);

        // MIC button - simulate voice recording on tap
        if (btnMicLocal != null) {
            btnMicLocal.setOnClickListener(v -> {
                Toast.makeText(this, "🎙 Voice message sent!", Toast.LENGTH_SHORT).show();
                addMessage(new ChatMessage("", true, true));
            });
            
            // Add a long-press simulation for recording
            btnMicLocal.setOnLongClickListener(v -> {
                Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // ADD/ATTACHMENT button
        btnAdd.setOnClickListener(v -> showAttachmentOptions());

        // EMOJI button
        ImageView btnEmoji = findViewById(R.id.btnEmoji);
        if (btnEmoji != null) {
            btnEmoji.setOnClickListener(v ->
                Toast.makeText(this, "😊 Emoji picker coming soon!", Toast.LENGTH_SHORT).show()
            );
        }

        // PROFILE / INFO area
        findViewById(R.id.topBar).setOnClickListener(v -> 
            Toast.makeText(this, "Viewing " + tvChatName.getText() + "'s profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void showAttachmentOptions() {
        String[] options = {"Upload Attachment", "Send Voice Message"};
        new AlertDialog.Builder(this)
                .setTitle("Attach Media")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "Attachment uploaded!", Toast.LENGTH_SHORT).show();
                        addMessage(new ChatMessage("Sent an attachment \ud83d\udcc1", false, true));
                    } else if (which == 1) {
                        addMessage(new ChatMessage("", true, true));
                    }
                })
                .show();
    }

    private void addMessage(ChatMessage message) {
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            boolean isAnyPlaying = false;
            for (int i = 0; i < messageList.size(); i++) {
                ChatMessage m = messageList.get(i);
                if (m.isPlaying) {
                    isAnyPlaying = true;
                    m.progress += 2;
                    if (m.progress >= 100) {
                        m.progress = 0;
                        m.isPlaying = false;
                    }
                    adapter.notifyItemChanged(i);
                }
            }
            if (isAnyPlaying) {
                handler.postDelayed(this, 100); 
            }
        }
    };

    public void startProgressLoop() {
        handler.removeCallbacks(progressUpdater);
        handler.post(progressUpdater);
    }

    class ChatMessage {
        String text;
        boolean isVoice;
        boolean isOutgoing;
        boolean isPlaying = false;
        int progress = 0;

        ChatMessage(String text, boolean isVoice, boolean isOutgoing) {
            this.text = text;
            this.isVoice = isVoice;
            this.isOutgoing = isOutgoing;
        }
    }

    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatMessage> messages;
        private static final int TYPE_SENT = 1;
        private static final int TYPE_RECEIVED = 2;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isOutgoing ? TYPE_SENT : TYPE_RECEIVED;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SENT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sent, parent, false);
                return new ChatViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_received, parent, false);
                return new ChatViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage message = messages.get(position);

            if (message.isVoice && holder.clVoiceMessage != null) {
                if (holder.tvMessage != null) holder.tvMessage.setVisibility(View.GONE);
                holder.clVoiceMessage.setVisibility(View.VISIBLE);

                if (holder.sbProgress != null) holder.sbProgress.setProgress(message.progress);
                if (holder.ivPlayPause != null) {
                    holder.ivPlayPause.setImageResource(
                        message.isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
                    );
                    holder.ivPlayPause.setOnClickListener(v -> {
                        message.isPlaying = !message.isPlaying;
                        if (message.isPlaying) startProgressLoop();
                        notifyItemChanged(position);
                    });
                }

                if (holder.tvDuration != null) {
                    int secs = (int) (message.progress / 100f * 60);
                    holder.tvDuration.setText("0:" + String.format("%02d", secs));
                }

                if (holder.ivDelete != null) {
                    holder.ivDelete.setOnClickListener(v -> {
                        messages.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, messages.size());
                    });
                }

            } else {
                if (holder.tvMessage != null) {
                    holder.tvMessage.setVisibility(View.VISIBLE);
                    holder.tvMessage.setText(message.text);
                }
                if (holder.clVoiceMessage != null) {
                    holder.clVoiceMessage.setVisibility(View.GONE);
                }
            }

            // Animate in message (fade + slight scale)
            holder.itemView.setAlpha(0f);
            holder.itemView.setScaleX(0.95f);
            holder.itemView.setScaleY(0.95f);
            holder.itemView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(250)
                    .start();
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvDuration;
            View clVoiceMessage;
            ImageView ivPlayPause, ivDelete;
            SeekBar sbProgress;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                clVoiceMessage = itemView.findViewById(R.id.clVoiceMessage);
                ivPlayPause = itemView.findViewById(R.id.ivPlayPause);
                ivDelete = itemView.findViewById(R.id.ivDelete); // May be null in received layout
                sbProgress = itemView.findViewById(R.id.sbProgress);
                tvDuration = itemView.findViewById(R.id.tvDuration);
                
                if (sbProgress != null) {
                    sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                int pos = getAdapterPosition();
                                if(pos != RecyclerView.NO_POSITION) {
                                    messages.get(pos).progress = progress;
                                }
                            }
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                }
            }
        }
    }
}
