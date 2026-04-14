package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SeekerUpcomingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seeker_upcoming, container, false);

        LinearLayout track1 = rootView.findViewById(R.id.llTrackButton1);
        LinearLayout track2 = rootView.findViewById(R.id.llTrackButton2);
        LinearLayout chat1 = rootView.findViewById(R.id.llChatButton1);
        LinearLayout chat2 = rootView.findViewById(R.id.llChatButton2);

        if (track1 != null) {
            track1.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MapsActivity.class)));
        }

        if (track2 != null) {
            track2.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MapsActivity.class)));
        }

        if (chat1 != null) {
            chat1.setOnClickListener(v -> openChat("David Miller"));
        }

        if (chat2 != null) {
            chat2.setOnClickListener(v -> openChat("Sophia Kim"));
        }

        return rootView;
    }

    private void openChat(String name) {
        Intent intent = new Intent(requireActivity(), ChatActivity.class);
        intent.putExtra("CHAT_NAME", name);
        intent.putExtra("CHAT_ONLINE", true);
        startActivity(intent);
    }
}
