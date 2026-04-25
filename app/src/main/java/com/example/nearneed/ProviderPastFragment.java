package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ProviderPastFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider_past, container, false);

        // ── CARD 1: Completed — Garden Maintenance ─────────────────────────
        MaterialButton msg1 = root.findViewById(R.id.btnPastMsg1);
        MaterialButton earnings1 = root.findViewById(R.id.btnPastEarnings1);

        if (msg1 != null)
            msg1.setOnClickListener(v -> openChat("Riya Sharma"));

        if (earnings1 != null)
            earnings1.setOnClickListener(v -> openEarnings("Garden Maintenance", 500));

        // ── CARD 3: Completed — AC Repair ─────────────────────────────────
        MaterialButton msg3 = root.findViewById(R.id.btnPastMsg3);
        MaterialButton earnings3 = root.findViewById(R.id.btnPastEarnings3);

        if (msg3 != null)
            msg3.setOnClickListener(v -> openChat("Priya Nair"));

        if (earnings3 != null)
            earnings3.setOnClickListener(v -> openEarnings("AC Repair & Maintenance", 850));

        // ── CARD 2: Cancelled — Deep Home Cleaning ────────────────────────
        MaterialButton cancelDetails2 = root.findViewById(R.id.btnPastCancelDetails2);
        if (cancelDetails2 != null) {
            cancelDetails2.setOnClickListener(v -> {
                Intent i = new Intent(requireActivity(), CancellationDetailsActivity.class);
                startActivity(i);
            });
        }

        return root;
    }

    private void openChat(String name) {
        Intent i = new Intent(requireActivity(), ChatActivity.class);
        i.putExtra("CHAT_NAME", name);
        i.putExtra("CHAT_ONLINE", true);
        startActivity(i);
    }

    private void openEarnings(String serviceName, double amount) {
        Intent i = new Intent(requireActivity(), MyEarningsActivity.class);
        i.putExtra("service_name", serviceName);
        i.putExtra("service_amount", amount);
        startActivity(i);
    }
}
