package com.example.nearneed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays real-time earnings for the logged-in provider.
 *
 * Data source: Firestore → bookings collection
 * Filter:      providerId == currentUser.uid AND status == "completed"
 * Total:       Sum of all booking.amount values (no hardcoded values)
 * Update:      Automatic via addSnapshotListener — live updates whenever a gig completes
 */
public class MyEarningsActivity extends AppCompatActivity {

    private TextView tvTotalBalanceAmount;
    private TransactionAdapter adapter;
    private final List<TransactionItem> transactionList = new ArrayList<>();
    private ListenerRegistration earningsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_earnings);

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvTotalBalanceAmount = findViewById(R.id.tvTotalBalanceAmount);

        // Setup RecyclerView
        RecyclerView rvTransactions = findViewById(R.id.rv_transactions);
        adapter = new TransactionAdapter(transactionList);
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setAdapter(adapter);
        }

        attachRealtimeEarningsListener();
    }

    /**
     * Attaches a real-time Firestore listener on all bookings where:
     *   - providerId == current user's UID
     *   - status     == "completed"
     *
     * The listener fires immediately on attach (initial load) and again on every change,
     * guaranteeing the UI always reflects the true sum of completed gig payments.
     */
    private void attachRealtimeEarningsListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (tvTotalBalanceAmount != null) tvTotalBalanceAmount.setText("₹0");
            return;
        }

        earningsListener = FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("providerId", user.getUid())
                .whereEqualTo("status", "completed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    transactionList.clear();
                    double totalEarnings = 0.0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);

                        double amount = booking.amount != null ? booking.amount : 0.0;
                        totalEarnings += amount;

                        transactionList.add(new TransactionItem(
                                booking.postTitle != null ? booking.postTitle : "Completed Gig",
                                booking.postType != null ? booking.postType : "GIG",
                                formatDate(booking.timestamp),
                                "5.0",
                                "₹" + (int) amount,
                                "COMPLETED",
                                "GIG".equalsIgnoreCase(booking.postType)
                                        ? R.drawable.ic_payment_wallet_blue
                                        : R.drawable.ic_groceries
                        ));
                    }

                    adapter.notifyDataSetChanged();

                    if (tvTotalBalanceAmount != null) {
                        tvTotalBalanceAmount.setText("₹" + (int) totalEarnings);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach listener to prevent memory leaks
        if (earningsListener != null) {
            earningsListener.remove();
            earningsListener = null;
        }
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) return "—";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ─── Model ────────────────────────────────────────────────────────────────

    private static class TransactionItem {
        String title, category, date, rating, amount, status;
        int iconResId;

        TransactionItem(String title, String category, String date,
                        String rating, String amount, String status, int iconResId) {
            this.title = title;
            this.category = category;
            this.date = date;
            this.rating = rating;
            this.amount = amount;
            this.status = status;
            this.iconResId = iconResId;
        }
    }

    // ─── Adapter ──────────────────────────────────────────────────────────────

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

        private final List<TransactionItem> items;

        TransactionAdapter(List<TransactionItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_inr, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TransactionItem item = items.get(position);
            if (holder.tvTitle    != null) holder.tvTitle.setText(item.title);
            if (holder.tvCategory != null) holder.tvCategory.setText(item.category);
            if (holder.tvDate     != null) holder.tvDate.setText(item.date);
            if (holder.tvRating   != null) holder.tvRating.setText(item.rating);
            if (holder.tvStatus   != null) {
                holder.tvStatus.setText(item.status);
                holder.tvStatus.setTextColor(
                        android.graphics.Color.parseColor("#059669")); // always green (completed only)
            }
            if (holder.tvAmount != null) {
                holder.tvAmount.setText("+" + item.amount);
                holder.tvAmount.setTextColor(
                        android.graphics.Color.parseColor("#059669")); // green
            }
            try {
                if (holder.ivIcon != null) holder.ivIcon.setImageResource(item.iconResId);
            } catch (Exception ignored) {}
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCategory, tvDate, tvRating, tvAmount, tvStatus;
            ImageView ivIcon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle    = itemView.findViewById(R.id.tv_transaction_title);
                tvCategory = itemView.findViewById(R.id.tv_transaction_category);
                tvDate     = itemView.findViewById(R.id.tv_transaction_date);
                tvRating   = itemView.findViewById(R.id.tv_transaction_rating);
                tvAmount   = itemView.findViewById(R.id.tv_transaction_amount);
                tvStatus   = itemView.findViewById(R.id.tv_transaction_status);
                ivIcon     = itemView.findViewById(R.id.iv_transaction_icon);
            }
        }
    }
}
