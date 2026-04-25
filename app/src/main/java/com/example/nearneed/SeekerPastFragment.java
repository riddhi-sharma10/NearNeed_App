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

public class SeekerPastFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seeker_past, container, false);

        // ── CARD 1: Completed — Professional Garden Grooming ─────────────
        MaterialButton rebook1 = root.findViewById(R.id.btnRebook1);
        MaterialButton invoice1 = root.findViewById(R.id.btnViewInvoice1);

        if (rebook1 != null)
            rebook1.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Rebook flow coming soon", Toast.LENGTH_SHORT).show());

        if (invoice1 != null)
            invoice1.setOnClickListener(v ->
                    openInvoice("past_booking_1", "Professional Garden Grooming", 850));

        // ── CARD 2: Cancelled — Emergency Plumbing Repair ────────────────
        MaterialButton findAnother2 = root.findViewById(R.id.btnFindAnother2);
        MaterialButton details2 = root.findViewById(R.id.btnViewDetails2);

        if (findAnother2 != null)
            findAnother2.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Searching for providers...", Toast.LENGTH_SHORT).show());

        if (details2 != null)
            details2.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Cancellation details", Toast.LENGTH_SHORT).show());

        // ── CARD 3: Completed — Deep Home Cleaning ───────────────────────
        MaterialButton rebook3 = root.findViewById(R.id.btnRebook3);
        MaterialButton invoice3 = root.findViewById(R.id.btnViewInvoice3);

        if (rebook3 != null)
            rebook3.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Rebook flow coming soon", Toast.LENGTH_SHORT).show());

        if (invoice3 != null)
            invoice3.setOnClickListener(v ->
                    openInvoice("past_booking_3", "Deep Home Cleaning", 1200));

        return root;
    }

    private void openInvoice(String bookingId, String serviceName, double amount) {
        Intent i = new Intent(requireActivity(), PaymentSuccessActivity.class);
        i.putExtra("booking_id", bookingId);
        i.putExtra("service_name", serviceName);
        i.putExtra("service_amount", amount);
        startActivity(i);
    }
}
