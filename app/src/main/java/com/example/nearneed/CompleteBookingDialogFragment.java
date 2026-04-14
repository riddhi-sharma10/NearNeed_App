package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CompleteBookingDialogFragment extends BottomSheetDialogFragment {

    private String bookingId;
    private String serviceName;
    private String providerName;
    private double amount;
    private int selectedRating = 0;
    private ImageView[] starViews;
    private TextView tvRatingValue;
    private TextInputEditText etCompletionNotes;
    private MaterialButton btnProceedToPayment;
    private OnPaymentClickListener onPaymentClickListener;

    public interface OnPaymentClickListener {
        void onProceedToPayment(String bookingId, int rating, String notes);
    }

    public static CompleteBookingDialogFragment newInstance(String bookingId, String serviceName, String providerName, double amount) {
        CompleteBookingDialogFragment fragment = new CompleteBookingDialogFragment();
        Bundle args = new Bundle();
        args.putString("booking_id", bookingId);
        args.putString("service_name", serviceName);
        args.putString("provider_name", providerName);
        args.putDouble("amount", amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getString("booking_id");
            serviceName = getArguments().getString("service_name");
            providerName = getArguments().getString("provider_name");
            amount = getArguments().getDouble("amount");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_complete_booking, container, false);

        // Initialize views
        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        TextView tvAmount = view.findViewById(R.id.tvAmount);
        TextView tvProviderName = view.findViewById(R.id.tvProviderName);
        tvRatingValue = view.findViewById(R.id.tvRatingValue);
        etCompletionNotes = view.findViewById(R.id.etCompletionNotes);
        btnProceedToPayment = view.findViewById(R.id.btnProceedToPayment);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        ImageView btnCloseDialog = view.findViewById(R.id.btnCloseDialog);

        // Initialize star views
        starViews = new ImageView[]{
            view.findViewById(R.id.ivStar1),
            view.findViewById(R.id.ivStar2),
            view.findViewById(R.id.ivStar3),
            view.findViewById(R.id.ivStar4),
            view.findViewById(R.id.ivStar5)
        };

        // Set data
        tvServiceName.setText(serviceName);
        tvAmount.setText("₹" + String.format("%.0f", amount));
        tvProviderName.setText(providerName);

        // Setup star rating
        setupStarRating();

        // Setup buttons
        btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
        btnCancel.setOnClickListener(v -> dismiss());
        btnCloseDialog.setOnClickListener(v -> dismiss());

        return view;
    }

    private void setupStarRating() {
        for (int i = 0; i < starViews.length; i++) {
            final int position = i;
            starViews[i].setOnClickListener(v -> {
                selectedRating = position + 1;
                updateStarUI();
                updateRatingText();
            });
        }
    }

    private void updateStarUI() {
        for (int i = 0; i < starViews.length; i++) {
            if (i < selectedRating) {
                starViews[i].setImageResource(android.R.drawable.star_big_on);
                starViews[i].setColorFilter(getResources().getColor(R.color.trending_amber));
            } else {
                starViews[i].setImageResource(android.R.drawable.star_big_off);
                starViews[i].setColorFilter(getResources().getColor(R.color.text_muted));
            }
        }
    }

    private void updateRatingText() {
        if (selectedRating == 0) {
            tvRatingValue.setText("Not rated");
        } else {
            tvRatingValue.setText(selectedRating + ".0 / 5.0");
        }
    }

    private void proceedToPayment() {
        if (selectedRating == 0) {
            Toast.makeText(requireContext(), "Please rate the service", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etCompletionNotes.getText().toString().trim();

        if (onPaymentClickListener != null) {
            onPaymentClickListener.onProceedToPayment(bookingId, selectedRating, notes);
        }

        dismiss();
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.onPaymentClickListener = listener;
    }
}
