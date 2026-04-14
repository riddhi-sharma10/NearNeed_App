package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SeekerPastFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seeker_past, container, false);

        TextView btnRebook1 = rootView.findViewById(R.id.btnRebook1);
        TextView btnInvoice1 = rootView.findViewById(R.id.btnViewInvoice1);
        TextView btnRebook3 = rootView.findViewById(R.id.btnRebook3);
        TextView btnInvoice3 = rootView.findViewById(R.id.btnViewInvoice3);

        if (btnRebook1 != null) {
            btnRebook1.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MapsActivity.class)));
        }

        if (btnRebook3 != null) {
            btnRebook3.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MapsActivity.class)));
        }

        if (btnInvoice1 != null) {
            btnInvoice1.setOnClickListener(v -> openInvoice("past_booking_1", "Professional Garden Grooming", 850));
        }

        if (btnInvoice3 != null) {
            btnInvoice3.setOnClickListener(v -> openInvoice("past_booking_3", "Premium Home Deep Clean", 3200));
        }

        return rootView;
    }

    private void openInvoice(String bookingId, String serviceName, double amount) {
        Intent intent = new Intent(requireActivity(), PaymentSuccessActivity.class);
        intent.putExtra("booking_id", bookingId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("service_amount", amount);
        startActivity(intent);
    }
}
