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

public class ProviderUpcomingFragment extends Fragment {

    private MaterialCardView cvProvUpcoming1, cvProvUpcoming2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider_upcoming, container, false);

        cvProvUpcoming1 = root.findViewById(R.id.cvProvUpcoming1);
        cvProvUpcoming2 = root.findViewById(R.id.cvProvUpcoming2);

        // ── CARD 1: GIG — Garden Maintenance ──────────────────────────────
        MaterialButton msgSeeker1 = root.findViewById(R.id.btnMsgSeeker1);
        MaterialButton cancel1 = root.findViewById(R.id.btnProvCancel1);
        
        if (msgSeeker1 != null)
            msgSeeker1.setOnClickListener(v -> openChat("Riya Sharma"));

        if (cancel1 != null)
            cancel1.setOnClickListener(v -> showCancelDialog("prov_up_1", "Garden Maintenance", cvProvUpcoming1));

        // ── CARD 2: Community — Park Cleanup Drive ────────────────────────
        MaterialButton msgOrganiser = root.findViewById(R.id.btnMsgOrganiser);
        MaterialButton cancel2 = root.findViewById(R.id.btnProvCancel2);

        if (msgOrganiser != null)
            msgOrganiser.setOnClickListener(v -> openChat("Sarah Johnson"));

        if (cancel2 != null)
            cancel2.setOnClickListener(v -> showCancelDialog("prov_up_2", "Park Cleanup Drive", cvProvUpcoming2));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkStatuses();
    }

    private void checkStatuses() {
        BookingStateManager stateManager = BookingStateManager.getInstance();
        if (cvProvUpcoming1 != null && stateManager.isCancelled("prov_up_1")) {
            cvProvUpcoming1.setVisibility(View.GONE);
        }
        if (cvProvUpcoming2 != null && stateManager.isCancelled("prov_up_2")) {
            cvProvUpcoming2.setVisibility(View.GONE);
        }
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

    private void openChat(String name) {
        Intent i = new Intent(requireActivity(), ChatActivity.class);
        i.putExtra("CHAT_NAME", name);
        i.putExtra("CHAT_ONLINE", true);
        startActivity(i);
    }
}
