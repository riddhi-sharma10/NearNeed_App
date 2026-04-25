package com.example.nearneed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class SeekerOngoingFragment extends Fragment {

    private TextView tvStatus1, tvStatus2;
    private MaterialButton update1, update2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seeker_ongoing, container, false);

        tvStatus1 = root.findViewById(R.id.tvStatus1);
        tvStatus2 = root.findViewById(R.id.tvStatus2);

        // ── CARD 1: Deep Kitchen Sanitization ─────────────────────────────
        MaterialButton msg1 = root.findViewById(R.id.btnMsg1);
        update1 = root.findViewById(R.id.btnUpdateStatus1);

        if (msg1 != null)
            msg1.setOnClickListener(v -> openChat("Sarah Jenkins"));

        if (update1 != null)
            update1.setOnClickListener(v -> openUpdateStatus("1", "Deep Kitchen Sanitization"));

        // ── CARD 2: Afternoon Pet Walk ─────────────────────────────────────
        MaterialButton msg2 = root.findViewById(R.id.btnMsg2);
        update2 = root.findViewById(R.id.btnUpdateStatus2);

        if (msg2 != null)
            msg2.setOnClickListener(v -> openChat("Marcus Thorne"));

        if (update2 != null)
            update2.setOnClickListener(v -> openUpdateStatus("2", "Afternoon Pet Walk"));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkStatuses();
    }

    private void checkStatuses() {
        BookingStateManager stateManager = BookingStateManager.getInstance();
        
        // Card 1
        String status1 = stateManager.getStatus("1");
        updateCardUI(status1, tvStatus1, update1);

        // Card 2
        String status2 = stateManager.getStatus("2");
        updateCardUI(status2, tvStatus2, update2);
    }

    private void updateCardUI(String status, TextView tvStatus, MaterialButton btnUpdate) {
        if (tvStatus == null || btnUpdate == null) return;

        if (BookingStateManager.STATUS_CANCELLED.equals(status)) {
            tvStatus.setText("CANCELLED");
            tvStatus.setTextColor(Color.parseColor("#DC2626")); // Red text
            tvStatus.setBackgroundResource(R.drawable.bg_pill_cancelled_soft); // Fallback to transparent/red outline if missing
            btnUpdate.setVisibility(View.GONE);
        } else if (BookingStateManager.STATUS_COMPLETED.equals(status)) {
            tvStatus.setText("COMPLETED");
            tvStatus.setTextColor(Color.parseColor("#059669")); // Green text
            tvStatus.setBackgroundResource(R.drawable.bg_pill_completed_soft); // Fallback if missing
            btnUpdate.setVisibility(View.GONE);
        } else {
            tvStatus.setText("IN PROGRESS");
            tvStatus.setTextColor(Color.parseColor("#047857"));
            tvStatus.setBackgroundResource(R.drawable.bg_pill_in_progress);
            btnUpdate.setVisibility(View.VISIBLE);
        }
    }

    private void openChat(String name) {
        Intent i = new Intent(requireActivity(), ChatActivity.class);
        i.putExtra("CHAT_NAME", name);
        i.putExtra("CHAT_ONLINE", true);
        startActivity(i);
    }

    private void openUpdateStatus(String bookingId, String title) {
        Intent i = new Intent(requireActivity(), UpdateStatusActivity.class);
        i.putExtra("booking_id", bookingId);
        i.putExtra("booking_title", title);
        startActivity(i);
    }
}
