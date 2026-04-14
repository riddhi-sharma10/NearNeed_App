package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    private List<PaymentMethod> paymentMethods;
    private OnPaymentMethodSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnPaymentMethodSelectedListener {
        void onMethodSelected(int position);
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods, OnPaymentMethodSelectedListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;

        // Find default method
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (paymentMethods.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView view = (MaterialCardView) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod method = paymentMethods.get(position);
        holder.bind(method, position == selectedPosition, position);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public PaymentMethod getSelectedPaymentMethod() {
        return paymentMethods.get(selectedPosition);
    }

    public class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private RadioButton rbPaymentMethod;
        private ImageView ivMethodIcon;
        private TextView tvMethodName;
        private TextView tvMethodDetail;
        private TextView tvDefaultBadge;
        private MaterialCardView cardView;

        public PaymentMethodViewHolder(@NonNull MaterialCardView itemView) {
            super(itemView);
            cardView = itemView;
            rbPaymentMethod = itemView.findViewById(R.id.rbPaymentMethod);
            ivMethodIcon = itemView.findViewById(R.id.ivMethodIcon);
            tvMethodName = itemView.findViewById(R.id.tvMethodName);
            tvMethodDetail = itemView.findViewById(R.id.tvMethodDetail);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
        }

        public void bind(PaymentMethod method, boolean isSelected, int position) {
            tvMethodName.setText(method.getName());
            tvMethodDetail.setText(method.getDisplayDetail());
            rbPaymentMethod.setChecked(isSelected);

            // Show default badge
            if (method.isDefault()) {
                tvDefaultBadge.setVisibility(android.view.View.VISIBLE);
            } else {
                tvDefaultBadge.setVisibility(android.view.View.GONE);
            }

            // Click listener
            cardView.setOnClickListener(v -> {
                int previousSelectedPosition = selectedPosition;
                selectedPosition = position;

                if (listener != null) {
                    listener.onMethodSelected(position);
                }

                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(selectedPosition);
            });
        }
    }
}
