package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SeekerUpcomingFragment extends Fragment {

    private MaterialCardView cvSeekerUpcoming1, cvSeekerUpcoming2, cvSeekerUpcoming3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seeker_upcoming, container, false);

        cvSeekerUpcoming1 = root.findViewById(R.id.cvSeekerUpcoming1);
        cvSeekerUpcoming2 = root.findViewById(R.id.cvSeekerUpcoming2);
        cvSeekerUpcoming3 = root.findViewById(R.id.cvSeekerUpcoming3);

        // ── CARD 1: AC Repair ──────────────────────────────────────────────
        MaterialButton chat1 = root.findViewById(R.id.llChatButton1);
        MaterialButton cancel1 = root.findViewById(R.id.btnCancelBooking1);

        if (chat1 != null)
            chat1.setOnClickListener(v -> openChat("David Miller"));

        if (cancel1 != null)
            cancel1.setOnClickListener(v -> showCancelDialog("seek_up_1", "AC Repair & Maintenance", cvSeekerUpcoming1));

        // ── CARD 2: Deep Home Cleaning ────────────────────────────────────
        MaterialButton chat2 = root.findViewById(R.id.llChatButton2);
        MaterialButton cancel2 = root.findViewById(R.id.btnCancelBooking2);

        if (chat2 != null)
            chat2.setOnClickListener(v -> openChat("Sarah Jenkins"));

        if (cancel2 != null)
            cancel2.setOnClickListener(v -> showCancelDialog("seek_up_2", "Deep Home Cleaning", cvSeekerUpcoming2));

        // ── CARD 3: Park Cleanup (Community) ─────────────────────────────
        MaterialButton chatC3 = root.findViewById(R.id.llChatButtonC3);
        MaterialButton viewEventC3 = root.findViewById(R.id.llTrackButtonC3);

        if (chatC3 != null)
            chatC3.setOnClickListener(v -> openChat("Sarah Johnson"));

        if (viewEventC3 != null)
            viewEventC3.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Event details coming soon", Toast.LENGTH_SHORT).show());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkStatuses();
    }

    private void checkStatuses() {
        BookingStateManager stateManager = BookingStateManager.getInstance();
        if (cvSeekerUpcoming1 != null && stateManager.isCancelled("seek_up_1")) {
            cvSeekerUpcoming1.setVisibility(View.GONE);
        }
        if (cvSeekerUpcoming2 != null && stateManager.isCancelled("seek_up_2")) {
            cvSeekerUpcoming2.setVisibility(View.GONE);
        }
        // Assuming Card 3 (Community) cannot be cancelled directly here, or add logic if needed.
    }

    private void openChat(String name) {
        Intent i = new Intent(requireActivity(), ChatActivity.class);
        i.putExtra("CHAT_NAME", name);
        i.putExtra("CHAT_ONLINE", true);
        startActivity(i);
    }

    private void showCancelDialog(String bookingId, String title, MaterialCardView card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel \"" + title + "\"?")
                .setPositiveButton("Yes, Cancel", (d, w) -> {
                    BookingStateManager.getInstance().setStatus(bookingId, BookingStateManager.STATUS_CANCELLED);
                    if (card != null) card.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Keep Booking", null)
                .show();
    }
}
