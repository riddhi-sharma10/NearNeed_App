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

public class ProviderOngoingFragment extends Fragment {

    private TextView tvProvStatus1, tvProvStatus2;
    private MaterialButton update1, update2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider_ongoing, container, false);

        tvProvStatus1 = root.findViewById(R.id.tvProvStatus1);
        tvProvStatus2 = root.findViewById(R.id.tvProvStatus2);

        // ── CARD 1: Plumbing Repair ────────────────────────────────────────
        MaterialButton msg1 = root.findViewById(R.id.btnProvMsg1);
        update1 = root.findViewById(R.id.btnProvUpdateStatus1);

        if (msg1 != null)
            msg1.setOnClickListener(v -> openChat("Arjun Mehta"));

        if (update1 != null)
            update1.setOnClickListener(v -> openUpdateStatus("prov_1", "Plumbing Repair"));

        // ── CARD 2: AC Repair & Servicing ─────────────────────────────────
        MaterialButton msg2 = root.findViewById(R.id.btnProvMsg2);
        update2 = root.findViewById(R.id.btnProvUpdateStatus2);

        if (msg2 != null)
            msg2.setOnClickListener(v -> openChat("Priya Nair"));

        if (update2 != null)
            update2.setOnClickListener(v -> openUpdateStatus("prov_2", "AC Repair & Servicing"));

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
        String status1 = stateManager.getStatus("prov_1");
        updateCardUI(status1, tvProvStatus1, update1);

        // Card 2
        String status2 = stateManager.getStatus("prov_2");
        updateCardUI(status2, tvProvStatus2, update2);
    }

    private void updateCardUI(String status, TextView tvStatus, MaterialButton btnUpdate) {
        if (tvStatus == null || btnUpdate == null) return;

        if (BookingStateManager.STATUS_CANCELLED.equals(status)) {
            tvStatus.setText("CANCELLED");
            tvStatus.setTextColor(Color.parseColor("#DC2626")); // Red text
            tvStatus.setBackgroundResource(R.drawable.bg_pill_cancelled_soft); // Soft red background
            btnUpdate.setVisibility(View.GONE);
        } else if (BookingStateManager.STATUS_COMPLETED.equals(status)) {
            tvStatus.setText("COMPLETED");
            tvStatus.setTextColor(Color.parseColor("#059669")); // Green text
            tvStatus.setBackgroundResource(R.drawable.bg_pill_completed_soft); // Soft green background
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
