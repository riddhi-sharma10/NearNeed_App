package com.example.nearneed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private final List<ChatEntry> allChats = new ArrayList<>();
    private final List<ChatEntry> displayedChats = new ArrayList<>();

    private View searchBarContainer;
    private EditText etSearch;
    private ImageView btnSearch, btnClearSearch;
    private TextView btnCancelSearch;
    private View emptyStateContainer;
    private String currentRole;
    private FirebaseFirestore firestore;
    private ListenerRegistration chatsListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));

        emptyStateContainer = view.findViewById(R.id
                .emptyStateContainer);

        adapter = new MessagesAdapter(displayedChats);
        rvMessages.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser != null ? firebaseUser.getUid() : null;

        currentRole = RoleManager.getRole(requireContext());
        updateEmptyState();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            subscribeToRealtimeChats();
        } else {
            loadChatsForRole(currentRole);
        }
        setupSearch(view);
        SeekerNavbarController.bind(requireActivity(), view, SeekerNavbarController.TAB_CHAT);
    }

    @Override
    public void onResume() {
        super.onResume();

        String latestRole = RoleManager.getRole(requireContext());
        if (!latestRole.equals(currentRole)) {
            currentRole = latestRole;
            if (currentUserId == null || currentUserId.isEmpty()) {
                loadChatsForRole(currentRole);
            }
            collapseSearch();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatsListener != null) {
            chatsListener.remove();
            chatsListener = null;
        }
    }

    private void subscribeToRealtimeChats() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            allChats.clear();
            displayedChats.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            updateEmptyState();
            return;
        }

        if (chatsListener != null) {
            chatsListener.remove();
            chatsListener = null;
        }

        chatsListener = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    Map<String, ChatEntry> merged = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants == null || participants.isEmpty()) {
                            continue;
                        }

                        String otherUserId = null;
                        for (String participant : participants) {
                            if (participant != null && !participant.equals(currentUserId)) {
                                otherUserId = participant;
                                break;
                            }
                        }
                        if (otherUserId == null) {
                            continue;
                        }

                        String snippet = doc.getString("lastMessage");
                        Timestamp ts = doc.getTimestamp("lastTimestamp");
                        String time = formatChatTime(ts);
                        Boolean isRead = doc.getBoolean("isRead");
                        boolean unread = isRead == null || !isRead;

                        ChatEntry entry = new ChatEntry(
                                doc.getId(),
                                otherUserId,
                                "NearNeed User",
                                "CHAT",
                                snippet != null ? snippet : "Start chatting",
                                time,
                                false,
                                unread
                        );
                        // Store the timestamp for client-side sorting
                        entry.lastTimestamp = ts != null ? ts.toDate().getTime() : 0L;
                        merged.put(entry.chatId, entry);
                        hydrateUserInfo(entry);
                    }

                    allChats.clear();
                    allChats.addAll(merged.values());
                    
                    // Client-side sort by lastTimestamp descending
                    java.util.Collections.sort(allChats, (c1, c2) -> {
                        return Long.compare(c2.lastTimestamp, c1.lastTimestamp);
                    });

                    displayedChats.clear();
                    displayedChats.addAll(allChats);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    updateEmptyState();
                });
    }

    private void hydrateUserInfo(ChatEntry entry) {
        firestore.collection("Users").document(entry.userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        applyUserSnapshot(entry, snapshot, true);
                    }
                });
    }

    private void applyUserSnapshot(ChatEntry entry, DocumentSnapshot snapshot, boolean modern) {
        if (snapshot == null || !snapshot.exists()) {
            return;
        }

        entry.name = readString(snapshot, modern ? "name" : "fullName", entry.name);
        entry.email = readString(snapshot, "email", buildEmail(entry.name));
        entry.phone = readString(snapshot, "phone", buildPhone(entry.name));
        entry.gender = readString(snapshot, "gender", buildGender(entry.name));
        entry.experience = readString(snapshot, "experience", buildExperience(entry.name));
        entry.rating = readString(snapshot, "rating", buildRating(entry.name));
        entry.reviews = readString(snapshot, "reviews", buildReviews(entry.name));
        entry.bio = readString(snapshot, "bio", buildBio(entry));
        entry.address = readString(snapshot, modern ? "address" : "location", "");
        entry.profileImage = readString(snapshot, modern ? "profileImage" : "photoUrl", null);
        Boolean verified = snapshot.getBoolean("isVerified");
        entry.isVerified = verified != null && verified;

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String readString(DocumentSnapshot doc, String key, String fallback) {
        String value = doc.getString(key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatChatTime(Timestamp timestamp) {
        if (timestamp == null) return "Now";
        long diff = new Date().getTime() - timestamp.toDate().getTime();
        long mins = diff / (60 * 1000);
        if (mins < 1) return "Now";
        if (mins < 60) return mins + " min";
        long hours = mins / 60;
        if (hours < 24) return hours + " hr";
        long days = hours / 24;
        return days + " day" + (days > 1 ? "s" : "");
    }

    private void setupSearch(View root) {
        searchBarContainer = root.findViewById(R.id.searchBarContainer);
        etSearch = root.findViewById(R.id.etSearch);
        btnSearch = root.findViewById(R.id.btnSearch);
        btnClearSearch = root.findViewById(R.id.btnClearSearch);
        btnCancelSearch = root.findViewById(R.id.btnCancelSearch);

        btnSearch.setOnClickListener(v -> {
            searchBarContainer.setVisibility(View.VISIBLE);
            searchBarContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in));
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        btnCancelSearch.setOnClickListener(v -> collapseSearch());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadChatsForRole(String role) {
        // Only show real conversations from Firestore
        // No demo chats - empty state when offline
        allChats.clear();
        displayedChats.clear();
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyStateContainer == null || rvMessages == null) return;
        if (displayedChats.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            rvMessages.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvMessages.setVisibility(View.VISIBLE);
        }
    }

    private void filterChats(String query) {
        if (query.trim().isEmpty()) {
            displayedChats.clear();
            displayedChats.addAll(allChats);
            adapter.notifyDataSetChanged();
            updateEmptyState();
            return;
        }

        String lower = query.toLowerCase().trim();
        List<ChatEntry> filtered = new ArrayList<>();
        for (ChatEntry chat : allChats) {
            if (chat.name.toLowerCase().contains(lower)
                    || chat.snippet.toLowerCase().contains(lower)
                    || chat.gig.toLowerCase().contains(lower)) {
                filtered.add(chat);
            }
        }

        displayedChats.clear();
        displayedChats.addAll(filtered);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void collapseSearch() {
        if (etSearch != null) {
            etSearch.setText("");
        }
        if (searchBarContainer != null) {
            searchBarContainer.setVisibility(View.GONE);
        }
        displayedChats.clear();
        displayedChats.addAll(allChats);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
        if (etSearch != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void openChat(ChatEntry chat) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.chatId);
        intent.putExtra("CHAT_USER_ID", chat.userId);
        intent.putExtra("CHAT_NAME", chat.name);
        intent.putExtra("PERSON_NAME", chat.name);
        intent.putExtra("CHAT_TIME", chat.time);
        intent.putExtra("CHAT_ONLINE", chat.isOnline);
        intent.putExtra("CHAT_SNIPPET", chat.snippet);
        intent.putExtra("CHAT_VERIFIED", chat.isVerified);
        intent.putExtra("PERSON_USER_ID", chat.userId);
        intent.putExtra("PERSON_EMAIL", chat.email != null ? chat.email : buildEmail(chat.name));
        intent.putExtra("PERSON_PHONE", chat.phone != null ? chat.phone : buildPhone(chat.name));
        intent.putExtra("PERSON_GENDER", chat.gender != null ? chat.gender : buildGender(chat.name));
        intent.putExtra("PERSON_EXPERIENCE", chat.experience != null ? chat.experience : buildExperience(chat.name));
        intent.putExtra("PERSON_RATING", chat.rating != null ? chat.rating : buildRating(chat.name));
        intent.putExtra("PERSON_REVIEWS", chat.reviews != null ? chat.reviews : buildReviews(chat.name));
        intent.putExtra("PERSON_BIO", chat.bio != null ? chat.bio : buildBio(chat));
        intent.putExtra("IS_VERIFIED", chat.isVerified);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openPersonProfile(ChatEntry chat) {
        Intent intent = new Intent(requireContext(), PersonProfileActivity.class);
        intent.putExtra("PERSON_USER_ID", chat.userId);
        intent.putExtra("PERSON_NAME", chat.name);
        intent.putExtra("PERSON_EMAIL", chat.email != null ? chat.email : buildEmail(chat.name));
        intent.putExtra("PERSON_PHONE", chat.phone != null ? chat.phone : buildPhone(chat.name));
        intent.putExtra("PERSON_GENDER", chat.gender != null ? chat.gender : buildGender(chat.name));
        intent.putExtra("PERSON_EXPERIENCE", chat.experience != null ? chat.experience : buildExperience(chat.name));
        intent.putExtra("PERSON_RATING", chat.rating != null ? chat.rating : buildRating(chat.name));
        intent.putExtra("PERSON_REVIEWS", chat.reviews != null ? chat.reviews : buildReviews(chat.name));
        intent.putExtra("PERSON_BIO", chat.bio != null ? chat.bio : buildBio(chat));
        intent.putExtra("IS_VERIFIED", chat.isVerified);
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String buildEmail(String name) {
        String normalized = name.toLowerCase().replace(" ", ".").replaceAll("[^a-z.]", "");
        if (normalized.isEmpty()) normalized = "nearneed.user";
        return normalized + "@nearneed.app";
    }

    private String buildPhone(String name) {
        int seed = Math.abs(name.hashCode());
        int last4 = 1000 + (seed % 9000);
        return "+91 98" + (seed % 90 + 10) + "" + (seed % 9) + "" + (seed % 9) + " " + last4;
    }

    private String buildGender(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith("a") || lower.contains("sharma") || lower.contains("gupta")) {
            return "Female";
        }
        return "Male";
    }

    private String buildExperience(String name) {
        int years = 2 + (Math.abs(name.hashCode()) % 9);
        return years + " years";
    }

    private String buildRating(String name) {
        int d = Math.abs(name.hashCode()) % 7;
        double rating = 4.2 + (d / 10.0);
        return String.format(java.util.Locale.getDefault(), "%.1f", Math.min(rating, 4.9));
    }

    private String buildReviews(String name) {
        int reviews = 40 + (Math.abs(name.hashCode()) % 260);
        return reviews + " reviews";
    }

    private String buildBio(ChatEntry chat) {
        return chat.name + " is an active NearNeed member. " +
                "Known for quick responses and helpful communication. " +
                "Recent context: \"" + chat.snippet + "\"";
    }

    private static class ChatEntry {
        String chatId;
        String userId;
        String name, gig, snippet, time;
        String email, phone, gender, experience, rating, reviews, bio, address, profileImage;
        boolean isVerified;
        boolean isOnline, isUnread;
        long lastTimestamp;

        ChatEntry(String chatId, String userId, String name, String gig, String snippet, String time, boolean isOnline, boolean isUnread) {
            this.chatId = chatId;
            this.userId = userId;
            this.name = name;
            this.gig = gig;
            this.snippet = snippet;
            this.time = time;
            this.isOnline = isOnline;
            this.isUnread = isUnread;
        }
    }

    private class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
        private final List<ChatEntry> chats;

        MessagesAdapter(List<ChatEntry> chats) {
            this.chats = chats;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatEntry chat = chats.get(position);
            holder.tvName.setText(chat.name);
            holder.tvMessageSnippet.setText(chat.snippet);
            holder.tvTime.setText(chat.time);
            holder.vUnreadIndicator.setVisibility(chat.isUnread ? View.VISIBLE : View.GONE);
            
            // Bold text if unread, normal if read
            holder.tvMessageSnippet.setTypeface(null, chat.isUnread ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            
            if (holder.ivAvatar != null) {
                holder.ivAvatar.setImageResource(resolveAvatarForName(chat.name));
            }

            holder.itemView.setOnClickListener(v -> openChat(chat));
            holder.tvName.setOnClickListener(v -> openPersonProfile(chat));
            if (holder.avatarContainer != null) {
                holder.avatarContainer.setOnClickListener(v -> openPersonProfile(chat));
            }
            if (holder.ivAvatar != null) {
                holder.ivAvatar.setOnClickListener(v -> openPersonProfile(chat));
            }
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMessageSnippet, tvTime;
            View vUnreadIndicator;
            View avatarContainer;
            ImageView ivAvatar;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvMessageSnippet = itemView.findViewById(R.id.tvMessageSnippet);
                tvTime = itemView.findViewById(R.id.tvTime);
                vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
                avatarContainer = itemView.findViewById(R.id.avatarContainer);
                ivAvatar = itemView.findViewById(R.id.ivAvatar);
            }
        }

        private int resolveAvatarForName(String name) {
            String lower = name == null ? "" : name.toLowerCase();
            if (lower.endsWith("a") || lower.contains("sharma") || lower.contains("gupta") || lower.contains("kapoor") || lower.contains("jain")) {
                return R.drawable.avatar_sarah;
            }
            if (lower.contains("rahul") || lower.contains("aarav") || lower.contains("aditya") || lower.contains("karan") || lower.contains("deepak") || lower.contains("vishu") || lower.contains("kabir")) {
                return R.drawable.avatar_david;
            }
            return R.drawable.avatar_alex;
        }
    }
}