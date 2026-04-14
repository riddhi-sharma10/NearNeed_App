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

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private final List<ChatEntry> allChats = new ArrayList<>();
    private final List<ChatEntry> displayedChats = new ArrayList<>();

    private View searchBarContainer;
    private EditText etSearch;
    private ImageView btnSearch, btnClearSearch;
    private TextView btnCancelSearch;
    private String currentRole;

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

        adapter = new MessagesAdapter(displayedChats);
        rvMessages.setAdapter(adapter);

        currentRole = RoleManager.getRole(requireContext());
        loadChatsForRole(currentRole);
        setupSearch(view);
        SeekerNavbarController.bind(requireActivity(), view, SeekerNavbarController.TAB_CHAT);
    }

    @Override
    public void onResume() {
        super.onResume();

        String latestRole = RoleManager.getRole(requireContext());
        if (!latestRole.equals(currentRole)) {
            currentRole = latestRole;
            loadChatsForRole(currentRole);
            collapseSearch();
        }
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
        allChats.clear();

        if (RoleManager.ROLE_PROVIDER.equals(role)) {
            allChats.add(new ChatEntry("Aarav Mehta", "CLIENT REQUEST", "Can you quote the kitchen repair by tonight?", "2 min", false, true));
            allChats.add(new ChatEntry("Neha Sharma", "JOB REQUEST", "Need the plumbing estimate before 4 PM.", "3 hrs", true, true));
            allChats.add(new ChatEntry("Rahul Singh", "CLIENT", "Please send the revised schedule for the lawn job.", "1 hr", false, true));
            allChats.add(new ChatEntry("Pooja Verma", "JOB REQUEST", "Are you available for the weekend photo shoot?", "Yesterday", true, true));
            allChats.add(new ChatEntry("Kabir Joshi", "CLIENT", "The invoice is ready for approval.", "Mon", false, false));
            allChats.add(new ChatEntry("Ishita Jain", "JOB REQUEST", "We need one more delivery slot this evening.", "Tue", true, false));
            allChats.add(new ChatEntry("Aditya Rao", "CLIENT", "Thanks for the quick response.", "Wed", false, true));
            allChats.add(new ChatEntry("Maya Kapoor", "JOB REQUEST", "Could you confirm tomorrow's booking?", "Thu", true, false));
        } else {
            allChats.add(new ChatEntry("Rachel", "SERVICE REQUEST", "I can help with the plumbing! Let me know.", "2 min", false, true));
            allChats.add(new ChatEntry("Manya Awasthi", "PROVIDER", "Is the Pacific Blue color still available?", "3 hrs", false, false));
            allChats.add(new ChatEntry("Rahul Singh", "REQUEST", "The lawn looks great, I'll be back Tuesday.", "1 hr", false, true));
            allChats.add(new ChatEntry("Riddhi Sharma", "PROVIDER", "Luna had a great walk today! 🐕", "2 days", false, true));
            allChats.add(new ChatEntry("Vishu Singh", "SERVICE REQUEST", "I've reset the router, try the connection.", "Yesterday", true, true));
            allChats.add(new ChatEntry("Ananya Gupta", "PROVIDER", "Can you share the recipe you mentioned? 😊", "Yesterday", false, true));
            allChats.add(new ChatEntry("Karan Mehta", "REQUEST", "Thanks for dropping by!", "Mon", false, true));
            allChats.add(new ChatEntry("Deepak Verma", "PROVIDER", "Sure, I can drop it off Saturday morning.", "Sun", true, true));
        }

        displayedChats.clear();
        displayedChats.addAll(allChats);
        adapter.notifyDataSetChanged();
    }

    private void filterChats(String query) {
        if (query.trim().isEmpty()) {
            displayedChats.clear();
            displayedChats.addAll(allChats);
            adapter.notifyDataSetChanged();
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
        if (etSearch != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void openChat(ChatEntry chat) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("CHAT_NAME", chat.name);
        intent.putExtra("PERSON_NAME", chat.name);
        intent.putExtra("CHAT_TIME", chat.time);
        intent.putExtra("CHAT_ONLINE", chat.isOnline);
        intent.putExtra("CHAT_SNIPPET", chat.snippet);
        intent.putExtra("PERSON_EMAIL", buildEmail(chat.name));
        intent.putExtra("PERSON_PHONE", buildPhone(chat.name));
        intent.putExtra("PERSON_GENDER", buildGender(chat.name));
        intent.putExtra("PERSON_EXPERIENCE", buildExperience(chat.name));
        intent.putExtra("PERSON_RATING", buildRating(chat.name));
        intent.putExtra("PERSON_REVIEWS", buildReviews(chat.name));
        intent.putExtra("PERSON_BIO", buildBio(chat));
        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openPersonProfile(ChatEntry chat) {
        Intent intent = new Intent(requireContext(), PersonProfileActivity.class);
        intent.putExtra("PERSON_NAME", chat.name);
        intent.putExtra("PERSON_EMAIL", buildEmail(chat.name));
        intent.putExtra("PERSON_PHONE", buildPhone(chat.name));
        intent.putExtra("PERSON_GENDER", buildGender(chat.name));
        intent.putExtra("PERSON_EXPERIENCE", buildExperience(chat.name));
        intent.putExtra("PERSON_RATING", buildRating(chat.name));
        intent.putExtra("PERSON_REVIEWS", buildReviews(chat.name));
        intent.putExtra("PERSON_BIO", buildBio(chat));
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
        String name, gig, snippet, time;
        boolean isOnline, isUnread;

        ChatEntry(String name, String gig, String snippet, String time, boolean isOnline, boolean isUnread) {
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

            holder.itemView.setOnClickListener(v -> openChat(chat));
            holder.tvName.setOnClickListener(v -> openPersonProfile(chat));
            if (holder.avatarContainer != null) {
                holder.avatarContainer.setOnClickListener(v -> openPersonProfile(chat));
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

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvMessageSnippet = itemView.findViewById(R.id.tvMessageSnippet);
                tvTime = itemView.findViewById(R.id.tvTime);
                vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
                avatarContainer = itemView.findViewById(R.id.avatarContainer);
            }
        }
    }
}