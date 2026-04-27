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
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private EditText etMessageInput;
    private ImageView btnSend, btnAdd, btnBack;
    private TextView tvChatName;
    private TextView tvChatStatus;
    
    // Image Preview Views
    private View cvImagePreview;
    private ImageView ivSelectedImage, btnRemoveImage;
    private Uri selectedImageUri;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;

    private MediaRecorder recorder;
    private String audioFilePath;
    private MediaPlayer mediaPlayer;

    private Handler handler = new Handler(Looper.getMainLooper());
    private static final Object PAYLOAD_AUDIO_STATE = "payload_audio_state";
    private int activeAudioPosition = RecyclerView.NO_POSITION;
    
    private String chatId;
    private String otherUserId;
    private String currentUserId;
    private ChatViewModel chatViewModel;
    private Runnable progressUpdater;

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

        // Bubble containers
        // Note: we'll find these in the ViewHolder since they are per-item

        // Preview views
        cvImagePreview = findViewById(R.id.cvImagePreview);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);

        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(v -> {
                selectedImageUri = null;
                if (cvImagePreview != null) cvImagePreview.setVisibility(View.GONE);
                
                // Update Mic/Send button state
                String text = etMessageInput.getText().toString().trim();
                if (text.isEmpty()) {
                    btnSend.setVisibility(View.GONE);
                    final ImageView btnMicLocal = findViewById(R.id.btnMic);
                    if (btnMicLocal != null) btnMicLocal.setVisibility(View.VISIBLE);
                }
            });
        }

        setupLaunchers();
        setupMicButton();

        String chatName = getIntent().getStringExtra("CHAT_NAME");
        String chatTime = getIntent().getStringExtra("CHAT_TIME");
        boolean isOnline = getIntent().getBooleanExtra("CHAT_ONLINE", false);
        String chatSnippet = getIntent().getStringExtra("CHAT_SNIPPET");

        if (chatName != null) {
            tvChatName.setText(chatName);
        }
        boolean chatVerified = getIntent().getBooleanExtra("CHAT_VERIFIED", false);
        VerifiedBadgeHelper.apply(this, tvChatName, chatVerified);
        
        View vOnlineDot = findViewById(R.id.vOnlineDot);
        if (isOnline) {
            tvChatStatus.setText("Online");
            tvChatStatus.setTextColor(ContextCompat.getColor(this, R.color.brand_success_vibrant));
            if (vOnlineDot != null) vOnlineDot.setVisibility(View.VISIBLE);
        } else {
            tvChatStatus.setText(chatTime != null ? "Seen " + chatTime : "Offline");
            tvChatStatus.setTextColor(ContextCompat.getColor(this, R.color.text_body_light));
            if (vOnlineDot != null) vOnlineDot.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        currentUserId = FirebaseAuth.getInstance().getUid();
        otherUserId = getIntent().getStringExtra("CHAT_USER_ID");
        chatId = getIntent().getStringExtra("CHAT_ID");
        String seekerId = getIntent().getStringExtra("SEEKER_ID");
        String providerId = getIntent().getStringExtra("PROVIDER_ID");

        if (chatId == null && currentUserId != null && otherUserId != null) {
            String[] ids = {currentUserId, otherUserId};
            Arrays.sort(ids);
            chatId = ids[0] + "_" + ids[1];
        }

        setupViewModel();
        if (chatViewModel != null) {
            chatViewModel.setRoleIds(seekerId, providerId);
        }
    }

    private void setupViewModel() {
        chatViewModel = new androidx.lifecycle.ViewModelProvider(this).get(ChatViewModel.class);
        
        chatViewModel.getMessages().observe(this, messages -> {
            messageList.clear();
            for (ChatMessage m : messages) {
                m.isVoice = m.audioPath != null && !m.audioPath.isEmpty();
                if (m.isVoice) m.durationSecs = 7;
                messageList.add(m);
            }
            adapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
            // Auto-mark as read if we are actively viewing the chat
            if (chatId != null) {
                chatViewModel.markAsRead(chatId);
            }
        });

        if (chatId != null) {
            chatViewModel.observeMessages(chatId);
            chatViewModel.markAsRead(chatId);
        }

        adapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // messages start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = rvMessages.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        // Scroll to the latest message
        rvMessages.scrollToPosition(messageList.size() - 1);

        // SEND button logic - Send text message
        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if ((!text.isEmpty() || selectedImageUri != null) && chatId != null) {
                if (selectedImageUri != null) {
                    chatViewModel.sendMediaMessage(chatId, otherUserId, selectedImageUri.toString(), false);
                    selectedImageUri = null;
                    if (cvImagePreview != null) cvImagePreview.setVisibility(View.GONE);
                } else {
                    chatViewModel.sendMessage(chatId, otherUserId, text);
                }
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
                if (s.toString().trim().length() > 0 || selectedImageUri != null) {
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
    }

    private void setupLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        showImagePreview();
                    }
                }
        );

        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Camera returns thumbnail bitmap in extras
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo != null) {
                            selectedImageUri = saveBitmapToUri(photo);
                            showImagePreview();
                        }
                    }
                }
        );
    }

    private Uri saveBitmapToUri(Bitmap bitmap) {
        File file = new File(getExternalFilesDir(null), "photo_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    private void showImagePreview() {
        if (selectedImageUri != null) {
            if (cvImagePreview != null) cvImagePreview.setVisibility(View.VISIBLE);
            if (ivSelectedImage != null) ivSelectedImage.setImageURI(selectedImageUri);
            if (btnSend != null) btnSend.setVisibility(View.VISIBLE);
            final ImageView btnMicLocal = findViewById(R.id.btnMic);
            if (btnMicLocal != null) btnMicLocal.setVisibility(View.GONE);
        }
    }

    // MIC button - simulate real-time recording
    private void setupMicButton() {
        final ImageView btnMicLocal = findViewById(R.id.btnMic);
        if (btnMicLocal != null) {
            View llRecordingTab = findViewById(R.id.llRecordingTab);
            View flInputWrapper = findViewById(R.id.flInputWrapper);
            TextView tvRecordingTime = findViewById(R.id.tvRecordingTime);
            View vRecordDot = findViewById(R.id.vRecordDot);

            btnMicLocal.setOnTouchListener(new View.OnTouchListener() {
                long startTime = 0;
                float startX = 0;
                boolean isCancelled = false;
                Runnable timerRunnable;
                
                @Override
                public boolean onTouch(View v, android.view.MotionEvent event) {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                            return false;
                        }
                        startTime = System.currentTimeMillis();
                        startX = event.getX();
                        isCancelled = false;
                        
                        btnMicLocal.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).start();
                        if (flInputWrapper != null) flInputWrapper.setVisibility(View.GONE);
                        if (btnAdd != null) btnAdd.setVisibility(View.GONE);
                        if (llRecordingTab != null) llRecordingTab.setVisibility(View.VISIBLE);
                        
                        startRecording();

                        timerRunnable = new Runnable() {
                            boolean dotVisible = true;
                            @Override
                            public void run() {
                                long elapsed = System.currentTimeMillis() - startTime;
                                int secs = (int) (elapsed / 1000);
                                if (tvRecordingTime != null) tvRecordingTime.setText(String.format("0:%02d", secs));
                                
                                dotVisible = !dotVisible;
                                if (vRecordDot != null) vRecordDot.setAlpha(dotVisible ? 1f : 0f);
                                
                                handler.postDelayed(this, 500);
                            }
                        };
                        handler.post(timerRunnable);
                        return true;
                        
                    } else if (event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                        if (startX - event.getX() > 100 && !isCancelled) {
                            isCancelled = true;
                            if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
                            
                            if (flInputWrapper != null) flInputWrapper.setVisibility(View.VISIBLE);
                            if (btnAdd != null) btnAdd.setVisibility(View.VISIBLE);
                            if (llRecordingTab != null) llRecordingTab.setVisibility(View.GONE);
                            btnMicLocal.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        }
                        return true;
                        
                    } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || 
                               event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                        
                        if (timerRunnable != null) {
                            handler.removeCallbacks(timerRunnable);
                        }
                        
                        if (flInputWrapper != null) flInputWrapper.setVisibility(View.VISIBLE);
                        if (btnAdd != null) btnAdd.setVisibility(View.VISIBLE);
                        if (llRecordingTab != null) llRecordingTab.setVisibility(View.GONE);
                        btnMicLocal.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        
                        stopRecording(isCancelled);

                        if (event.getAction() == android.view.MotionEvent.ACTION_UP && !isCancelled) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            int secs = (int) (elapsed / 1000);
                            
                            if (secs < 1) {
                                Toast.makeText(ChatActivity.this, "🎙 Hold to record a voice message", Toast.LENGTH_SHORT).show();
                            } else {
                                if (chatId != null) {
                                    chatViewModel.sendMediaMessage(chatId, otherUserId, audioFilePath, true);
                                }
                            }
                        }
                        return true;
                    }
                    return true;
                }
            });
        }
        // ADD/ATTACHMENT button - opens gallery directly
        btnAdd.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(galleryIntent);
        });

        // EMOJI button - removed per user request

        // PROFILE / INFO area
        findViewById(R.id.topBar).setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonProfileActivity.class);
            intent.putExtra("PERSON_NAME", tvChatName.getText().toString());
            intent.putExtra("PERSON_EMAIL", getIntent().getStringExtra("PERSON_EMAIL"));
            intent.putExtra("PERSON_PHONE", getIntent().getStringExtra("PERSON_PHONE"));
            intent.putExtra("PERSON_GENDER", getIntent().getStringExtra("PERSON_GENDER"));
            intent.putExtra("PERSON_EXPERIENCE", getIntent().getStringExtra("PERSON_EXPERIENCE"));
            intent.putExtra("PERSON_RATING", getIntent().getStringExtra("PERSON_RATING"));
            intent.putExtra("PERSON_REVIEWS", getIntent().getStringExtra("PERSON_REVIEWS"));
            intent.putExtra("PERSON_BIO", getIntent().getStringExtra("PERSON_BIO"));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void showAttachmentOptions() {
        String[] options = {"📷  Take Photo", "🖼  Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Attach Media")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else if (which == 1) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickImageLauncher.launch(galleryIntent);
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(cameraIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionToRecordAccepted) {
                Toast.makeText(this, "Microphone access granted", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(progressUpdater);
        activeAudioPosition = RecyclerView.NO_POSITION;
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        releaseMediaPlayer();
    }

    private void startRecording() {
        try {
            audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/voice_" + System.currentTimeMillis() + ".3gp";
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(audioFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording(boolean cancelled) {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                // Handle stop failure
            }
            recorder.release();
            recorder = null;
        }
        if (cancelled && audioFilePath != null) {
            File file = new File(audioFilePath);
            if (file.exists()) file.delete();
            audioFilePath = null;
        }
    }

    private void playAudio(String path, ChatMessage message, int position) {
        if (position == RecyclerView.NO_POSITION || position >= messageList.size()) {
            return;
        }
        if (path == null || !(new File(path).exists())) {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (activeAudioPosition != RecyclerView.NO_POSITION && activeAudioPosition != position) {
            stopAudio(messageList.get(activeAudioPosition), activeAudioPosition);
        }
        releaseMediaPlayer();
        
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            message.durationSecs = Math.max(1, mediaPlayer.getDuration() / 1000);
            message.progress = 0;
            activeAudioPosition = position;
            mediaPlayer.start();
            message.isPlaying = true;
            adapter.notifyItemChanged(position, PAYLOAD_AUDIO_STATE);
            startProgressLoop();
            
            mediaPlayer.setOnCompletionListener(mp -> {
                stopAudio(message, position);
            });
        } catch (Exception e) {
            e.printStackTrace();
            message.isPlaying = false;
            message.progress = 0;
            activeAudioPosition = RecyclerView.NO_POSITION;
            releaseMediaPlayer();
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void startProgressLoop() {
        handler.removeCallbacks(progressUpdater);
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (activeAudioPosition != RecyclerView.NO_POSITION && activeAudioPosition < messageList.size()) {
                    ChatMessage msg = messageList.get(activeAudioPosition);
                    msg.progress = getMediaPlayerProgress();
                    adapter.notifyItemChanged(activeAudioPosition, PAYLOAD_AUDIO_STATE);
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(progressUpdater);
    }

    private void stopAllAudio() {
        if (activeAudioPosition != RecyclerView.NO_POSITION && activeAudioPosition < messageList.size()) {
            stopAudio(messageList.get(activeAudioPosition), activeAudioPosition);
            return;
        }
        for (ChatMessage m : messageList) {
            m.isPlaying = false;
        }
        activeAudioPosition = RecyclerView.NO_POSITION;
        handler.removeCallbacks(progressUpdater);
        releaseMediaPlayer();
    }

    private void stopAudio(ChatMessage message, int position) {
        if (message == null || position == RecyclerView.NO_POSITION || position >= messageList.size()) {
            return;
        }
        if (activeAudioPosition == position) {
            handler.removeCallbacks(progressUpdater);
            releaseMediaPlayer();
            activeAudioPosition = RecyclerView.NO_POSITION;
        }
        message.isPlaying = false;
        message.progress = 0;
        adapter.notifyItemChanged(position, PAYLOAD_AUDIO_STATE);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private boolean isMediaPlayerActive() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private int getMediaPlayerProgress() {
        try {
            if (mediaPlayer != null && mediaPlayer.getDuration() > 0) {
                return (int) ((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration() * 100);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private int getMediaPlayerPositionSecs() {
        try {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition() / 1000;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void showFullscreenImage(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(this);
        com.bumptech.glide.Glide.with(this)
                .load(imageUri)
                .placeholder(R.color.bg_grey_light)
                .error(R.drawable.ic_image_placeholder)
                .into(imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(android.graphics.Color.BLACK);
        imageView.setPadding(0, 0, 0, 0);
        builder.setView(imageView);
        AlertDialog dialog = builder.create();
        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addMessage(ChatMessage message) {
        // Handled by ViewModel observation
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

            View llBubble = message.isOutgoing ? holder.llSentBubble : holder.llReceivedBubble;

            if (message.isVoice && holder.clVoiceMessage != null) {
                if (llBubble != null) llBubble.setVisibility(View.GONE);
                holder.clVoiceMessage.setVisibility(View.VISIBLE);

                updateVoicePlaybackViews(holder, message);

                if (holder.ivPlayPause != null) {
                    holder.ivPlayPause.setOnClickListener(v -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION) return;

                        ChatMessage currentMessage = messages.get(adapterPosition);
                        if (currentMessage.isPlaying) {
                            stopAudio(currentMessage, adapterPosition);
                        } else {
                            if (currentMessage.audioPath != null) {
                                playAudio(currentMessage.audioPath, currentMessage, adapterPosition);
                            } else {
                                // Fallback for simulated messages
                                if (activeAudioPosition != RecyclerView.NO_POSITION && activeAudioPosition < messages.size()) {
                                    stopAudio(messages.get(activeAudioPosition), activeAudioPosition);
                                }
                                currentMessage.isPlaying = true;
                                currentMessage.progress = 0;
                                activeAudioPosition = adapterPosition;
                                startProgressLoop();
                                notifyItemChanged(adapterPosition, PAYLOAD_AUDIO_STATE);
                            }
                        }
                    });
                }
            } else {
                if (llBubble != null) llBubble.setVisibility(View.VISIBLE);
                if (holder.tvMessage != null) {
                    holder.tvMessage.setText(message.messageText);
                    holder.tvMessage.setVisibility((message.messageText == null || message.messageText.isEmpty()) ? View.GONE : View.VISIBLE);
                }
                if (holder.clVoiceMessage != null) {
                    holder.clVoiceMessage.setVisibility(View.GONE);
                }

                // Image handling
                View cvImage = message.isOutgoing ? holder.cvImageSent : holder.cvImageReceived;
                ImageView ivImage = message.isOutgoing ? holder.ivSentImage : holder.ivReceivedImage;

                if (cvImage != null && ivImage != null) {
                    if (message.imageUri != null && !message.imageUri.isEmpty()) {
                        cvImage.setVisibility(View.VISIBLE);
                        com.bumptech.glide.Glide.with(ivImage.getContext())
                                .load(message.imageUri)
                                .placeholder(R.color.bg_grey_light)
                                .error(R.drawable.ic_image_placeholder)
                                .centerCrop()
                                .into(ivImage);
                        
                        // Click to view fullscreen
                        ivImage.setOnClickListener(v -> showFullscreenImage(Uri.parse(message.imageUri)));
                    } else {
                        cvImage.setVisibility(View.GONE);
                    }
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty() && payloads.contains(PAYLOAD_AUDIO_STATE)) {
                ChatMessage message = messages.get(position);
                if (message.isVoice) {
                    updateVoicePlaybackViews(holder, message);
                }
                return;
            }
            super.onBindViewHolder(holder, position, payloads);
        }

        private void updateVoicePlaybackViews(@NonNull ChatViewHolder holder, @NonNull ChatMessage message) {
            if (holder.sbProgress != null) {
                holder.sbProgress.setProgress(message.progress);
            }
            if (holder.ivPlayPause != null) {
                holder.ivPlayPause.setImageResource(
                        message.isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play
                );
                holder.ivPlayPause.setAlpha(message.isPlaying ? 0.9f : 1f);
            }
            if (holder.tvDuration != null) {
                if (message.isPlaying && message.audioPath != null) {
                    int secs = getMediaPlayerPositionSecs();
                    holder.tvDuration.setText("0:" + String.format("%02d", secs));
                } else {
                    holder.tvDuration.setText("0:" + String.format("%02d", message.durationSecs));
                }
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvDuration;
            View clVoiceMessage, cvImageSent, cvImageReceived;
            View llSentBubble, llReceivedBubble;
            ImageView ivPlayPause, ivSentImage, ivReceivedImage;
            SeekBar sbProgress;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                clVoiceMessage = itemView.findViewById(R.id.clVoiceMessage);
                ivPlayPause = itemView.findViewById(R.id.ivPlayPause);

                sbProgress = itemView.findViewById(R.id.sbProgress);
                tvDuration = itemView.findViewById(R.id.tvDuration);

                // Sent Image Views
                cvImageSent = itemView.findViewById(R.id.cvImageSent);
                ivSentImage = itemView.findViewById(R.id.ivSentImage);

                // Received Image Views
                cvImageReceived = itemView.findViewById(R.id.cvImageReceived);
                ivReceivedImage = itemView.findViewById(R.id.ivReceivedImage);

                // Bubble containers
                llSentBubble = itemView.findViewById(R.id.llSentMessageBubble);
                llReceivedBubble = itemView.findViewById(R.id.llReceivedMessageBubble);
            }
        }
    }
}
