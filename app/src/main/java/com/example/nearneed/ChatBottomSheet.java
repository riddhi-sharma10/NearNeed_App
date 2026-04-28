package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatBottomSheet extends BottomSheetDialogFragment {

    private String chatId;
    private String otherUserId;
    private String otherUserName;
    
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ChatViewModel chatViewModel;
    private ChatSimpleAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    public static ChatBottomSheet newInstance(String otherUserId, String otherUserName, String seekerId, String providerId) {
        ChatBottomSheet fragment = new ChatBottomSheet();
        Bundle args = new Bundle();
        args.putString("otherUserId", otherUserId);
        args.putString("otherUserName", otherUserName);
        args.putString("seekerId", seekerId);
        args.putString("providerId", providerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            otherUserId = getArguments().getString("otherUserId");
            otherUserName = getArguments().getString("otherUserName");
            
            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (currentUserId != null && otherUserId != null) {
                String[] ids = {currentUserId, otherUserId};
                Arrays.sort(ids);
                chatId = ids[0] + "_" + ids[1];
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_chat_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        TextView tvTitle = view.findViewById(R.id.tvChatTitle);
        
        if (otherUserName != null) {
            tvTitle.setText(otherUserName);
        }

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        setupViewModel();

        view.findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    private void setupViewModel() {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        if (getArguments() != null) {
            String seekerId = getArguments().getString("seekerId");
            String providerId = getArguments().getString("providerId");
            chatViewModel.setRoleIds(seekerId, providerId);
        }

        adapter = new ChatSimpleAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            messageList.clear();
            messageList.addAll(messages);
            adapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
            // Auto-mark as read if dialog is open
            if (chatId != null) {
                chatViewModel.markAsRead(chatId);
            }
        });

        if (chatId != null) {
            chatViewModel.observeMessages(chatId);
            chatViewModel.markAsRead(chatId);
        }
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (!text.isEmpty() && chatId != null && otherUserId != null) {
            chatViewModel.sendMessage(chatId, otherUserId, text);
            etMessageInput.setText("");
        }
    }

    private static class ChatSimpleAdapter extends RecyclerView.Adapter<ChatSimpleAdapter.ViewHolder> {
        private final List<ChatMessage> messages;
        private final String currentUserId;

        ChatSimpleAdapter(List<ChatMessage> messages) {
            this.messages = messages;
            this.currentUserId = FirebaseAuth.getInstance().getUid();
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage msg = messages.get(position);
            boolean isOutgoing = msg.senderId != null && msg.senderId.equals(currentUserId);
            return isOutgoing ? 1 : 0;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int resId = (viewType == 1) ? R.layout.item_chat_sent : R.layout.item_chat_received;
            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.tvText.setText(msg.messageText);
        }

        @Override
        public int getItemCount() { return messages.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvText;
            ViewHolder(View v) {
                super(v);
                tvText = v.findViewById(R.id.tvMessage);
            }
        }
    }
}
