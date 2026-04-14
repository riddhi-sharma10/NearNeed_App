package com.example.nearneed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class SeekerOngoingFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seeker_ongoing, container, false);

        // Wire Update Status buttons to UpdateStatusActivity
        MaterialButton btnUpdateStatus1 = rootView.findViewById(R.id.btnUpdateStatus1);
        MaterialButton btnUpdateStatus2 = rootView.findViewById(R.id.btnUpdateStatus2);

        if (btnUpdateStatus1 != null) {
            btnUpdateStatus1.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), UpdateStatusActivity.class);
                intent.putExtra("booking_id", "1"); // First booking
                intent.putExtra("booking_title", "Deep Kitchen Sanitization");
                startActivity(intent);
            });
        }

        if (btnUpdateStatus2 != null) {
            btnUpdateStatus2.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), UpdateStatusActivity.class);
                intent.putExtra("booking_id", "2"); // Second booking
                intent.putExtra("booking_title", "Afternoon Pet Walk");
                startActivity(intent);
            });
        }

        return rootView;
    }
}
