package com.example.nearneed;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private MessagesAdapter adapter;
    private List<ChatEntry> allChats = new ArrayList<>();

    // Search UI
    private View searchBarContainer;
    private EditText etSearch;
    private ImageView btnSearch, btnClearSearch;
    private TextView btnCancelSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Build chat list
        allChats.add(new ChatEntry("Rachel", "", "I can help with the plumbing! Let me know.", "2 min", false, false));
        allChats.add(new ChatEntry("Manya Awasthi", "", "Is the Pacific Blue color still available?", "3 hrs", false, false));
        allChats.add(new ChatEntry("Rahul Singh", "", "The lawn looks great, I'll be back Tuesday.", "1 hr", false, true));
        allChats.add(new ChatEntry("Riddhi Sharma", "", "Luna had a great walk today! \ud83d\udc15", "2 days", false, true));
        allChats.add(new ChatEntry("Vishu Singh", "", "I've reset the router, try the connection.", "Yesterday", true, true));
        allChats.add(new ChatEntry("Ananya Gupta", "", "Can you share the recipe you mentioned? \ud83d\ude0a", "Yesterday", false, true));
        allChats.add(new ChatEntry("Karan Mehta", "", "Thanks for dropping by!", "Mon", false, true));
        allChats.add(new ChatEntry("Deepak Verma", "", "Sure, I can drop it off Saturday morning.", "Sun", true, true));
        allChats.add(new ChatEntry("Meera Iyer", "", "Borrowing is fine, text me when you're done.", "Sat", false, true));
        allChats.add(new ChatEntry("Aditya Rao", "", "Confirmed for 5pm at the parking lot.", "Fri", true, true));
        allChats.add(new ChatEntry("Sneha Patel", "", "I'll be there in 10 mins, just parking.", "Thu", false, true));
        allChats.add(new ChatEntry("Emily Davis", "GIG: CONTENT WRITER", "Sent you the final draft, check your email.", "1 week ago", true, true));
        allChats.add(new ChatEntry("James Miller", "GIG: DANCE INSTRUCTOR", "The new routine is uploaded to the portal.", "2 weeks ago", true, true));
        allChats.add(new ChatEntry("Robert Wilson", "GIG: ACTING COACH", "Your performance in the last class was stellar.", "2 weeks ago", false, true));
        allChats.add(new ChatEntry("Sophia Martinez", "GIG: PHOTOGRAPHER", "The event photos are processing. 2 days more.", "3 weeks ago", true, true));
        allChats.add(new ChatEntry("William Taylor", "GIG: CRICKET COACH", "Good session today, focus on footwork.", "3 weeks ago", true, true));
        allChats.add(new ChatEntry("Olivia Anderson", "GIG: TOUR GUIDE", "I recommend the pink café for your visit.", "1 month ago", false, true));
        allChats.add(new ChatEntry("Alexander Thomas", "GIG: SINGER", "The acoustics in the room are perfect.", "1 month ago", true, true));
        allChats.add(new ChatEntry("Mia Garcia", "GIG: MAKEUP ARTIST", "Confirmed for Saturday at 4 PM.", "2 months ago", true, true));
        allChats.add(new ChatEntry("Ethan Robinson", "GIG: GYM TRAINER", "Consistency is key. See you tomorrow.", "2 months ago", false, true));

        adapter = new MessagesAdapter(allChats);
        rvMessages.setAdapter(adapter);

        MessagesNavbarController.bind(this, findViewById(android.R.id.content));

        setupSearch();
    }

    private void setupSearch() {
        searchBarContainer = findViewById(R.id.searchBarContainer);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnCancelSearch = findViewById(R.id.btnCancelSearch);

        // Open search bar
        btnSearch.setOnClickListener(v -> {
            searchBarContainer.setVisibility(View.VISIBLE);
            searchBarContainer.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        });

        // Cancel / collapse search bar
        btnCancelSearch.setOnClickListener(v -> collapseSearch());

        // Clear text X button
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // Live filter as user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterChats(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterChats(String query) {
        if (query.trim().isEmpty()) {
            adapter.updateChats(allChats);
            return;
        }
        String lower = query.toLowerCase().trim();
        List<ChatEntry> filtered = new ArrayList<>();
        for (ChatEntry chat : allChats) {
            if (chat.name.toLowerCase().contains(lower) ||
                chat.snippet.toLowerCase().contains(lower) ||
                chat.gig.toLowerCase().contains(lower)) {
                filtered.add(chat);
            }
        }
        adapter.updateChats(filtered);
    }

    private void collapseSearch() {
        etSearch.setText("");
        searchBarContainer.setVisibility(View.GONE);
        adapter.updateChats(allChats);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
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
            this.chats = new ArrayList<>(chats);
        }

        public void updateChats(List<ChatEntry> newChats) {
            this.chats.clear();
            this.chats.addAll(newChats);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ChatEntry chat = chats.get(position);
            holder.tvName.setText(chat.name);
            holder.tvMessageSnippet.setText(chat.snippet);
            holder.tvTime.setText(chat.time);
            holder.vUnreadIndicator.setVisibility(chat.isUnread ? View.VISIBLE : View.GONE);
            
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("CHAT_NAME", chat.name);
                intent.putExtra("CHAT_TIME", chat.time);
                intent.putExtra("CHAT_ONLINE", chat.isOnline);
                intent.putExtra("CHAT_SNIPPET", chat.snippet);
                v.getContext().startActivity(intent);
                // Premium transition
                if (v.getContext() instanceof android.app.Activity) {
                    ((android.app.Activity) v.getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        @Override
        public int getItemCount() {
            return chats.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMessageSnippet, tvTime;
            View vUnreadIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvMessageSnippet = itemView.findViewById(R.id.tvMessageSnippet);
                tvTime = itemView.findViewById(R.id.tvTime);
                vUnreadIndicator = itemView.findViewById(R.id.vUnreadIndicator);
            }
        }
    }
}
