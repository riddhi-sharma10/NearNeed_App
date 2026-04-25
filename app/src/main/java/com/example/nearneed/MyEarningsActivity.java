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

import java.util.ArrayList;
import java.util.List;

public class MyEarningsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_earnings);

        // Back button navigation
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Get Intent Extras
        String specificServiceName = getIntent().getStringExtra("service_name");
        double specificServiceAmount = getIntent().getDoubleExtra("service_amount", -1);

        // Update UI dynamically
        TextView tvTotalBalanceLabel = findViewById(R.id.tvTotalBalanceLabel);
        TextView tvTotalBalanceAmount = findViewById(R.id.tvTotalBalanceAmount);
        TextView tvRecentActivityLabel = findViewById(R.id.tvRecentActivityLabel);

        // Setup RecyclerView
        RecyclerView rvTransactions = findViewById(R.id.rv_transactions);
        List<TransactionItem> transactionList = new ArrayList<>();
        TransactionAdapter adapter = new TransactionAdapter(transactionList);
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setAdapter(adapter);
        }

        ApplicationViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ApplicationViewModel.class);
        viewModel.getUserApplications().observe(this, apps -> {
            transactionList.clear();
            double total = 0;
            for (Application app : apps) {
                String status = app.status != null ? app.status.toUpperCase() : "PENDING";
                double amount = app.proposedBudget != null ? app.proposedBudget : 0.0;
                
                if ("COMPLETED".equals(status)) {
                    total += amount;
                }

                transactionList.add(new TransactionItem(
                    app.postTitle != null ? app.postTitle : "Untitled Job",
                    app.postType != null ? app.postType : "Service",
                    formatDate(app.appliedAt),
                    "5.0",
                    "₹" + (int)amount,
                    status,
                    "GIG".equals(app.postType) ? R.drawable.ic_payment_wallet_blue : R.drawable.ic_groceries
                ));
            }
            adapter.notifyDataSetChanged();
            if (tvTotalBalanceAmount != null) {
                tvTotalBalanceAmount.setText("₹" + (int)total);
            }
        });
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) return "Just Now";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    private List<TransactionItem> getDummyTransactions() {
        List<TransactionItem> list = new ArrayList<>();
        list.add(new TransactionItem("Emergency Food Delivery", "Food Support", "Today, 2:45 PM", "4.8", "₹120", "COMPLETED", R.drawable.ic_groceries));
        list.add(new TransactionItem("Medicine Pick-up", "Medical Help", "Yesterday, 8:15 PM", "5.0", "₹85", "PENDING", R.drawable.ic_solid_firstaid));
        list.add(new TransactionItem("Leaky Faucet Repair", "Maintenance", "12 Mar, 10:30 AM", "4.7", "₹200", "COMPLETED", R.drawable.ic_solid_wrench));
        list.add(new TransactionItem("Garden Weeding", "Gardening", "10 Mar, 4:00 PM", "4.9", "₹150", "FAILED", R.drawable.ic_solid_plant));
        list.add(new TransactionItem("Laptop Setup Help", "IT Services", "08 Mar, 1:20 PM", "5.0", "₹300", "COMPLETED", R.drawable.ic_solid_laptop));
        list.add(new TransactionItem("Dog Walking", "Pet Care", "05 Mar, 7:30 AM", "4.6", "₹80", "COMPLETED", R.drawable.ic_solid_paw));
        list.add(new TransactionItem("Deep Cleaning", "Cleaning", "01 Mar, 9:00 AM", "4.8", "₹450", "PENDING", R.drawable.ic_solid_broom));
        list.add(new TransactionItem("Furniture Assembly", "General Labor", "28 Feb, 3:45 PM", "5.0", "₹250", "COMPLETED", R.drawable.ic_solid_supplies));
        list.add(new TransactionItem("Car Wash", "Maintenance", "25 Feb, 11:15 AM", "4.5", "₹180", "FAILED", R.drawable.ic_solid_car));
        list.add(new TransactionItem("Urgent Document Courier", "Delivery", "20 Feb, 5:50 PM", "4.9", "₹95", "COMPLETED", R.drawable.ic_truck_blue));
        return list;
    }

    // Inner Model Class
    private static class TransactionItem {
        String title;
        String category;
        String date;
        String rating;
        String amount;
        String status;
        int iconResId;

        TransactionItem(String title, String category, String date, String rating, String amount, String status, int iconResId) {
            this.title = title;
            this.category = category;
            this.date = date;
            this.rating = rating;
            this.amount = amount;
            this.status = status;
            this.iconResId = iconResId;
        }
    }

    // Inner Adapter Class
    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

        private final List<TransactionItem> items;

        TransactionAdapter(List<TransactionItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_inr, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TransactionItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvCategory.setText(item.category);
            holder.tvDate.setText(item.date);
            holder.tvRating.setText(item.rating);
            holder.tvAmount.setText("+" + item.amount);
            holder.tvStatus.setText(item.status);
            
            // Dynamic text color for status
            if ("COMPLETED".equalsIgnoreCase(item.status)) {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#059669")); // Green
                holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#059669")); // Green
                holder.tvAmount.setText("+" + item.amount.replace("+", "")); // Ensure single plus
            } else if ("PENDING".equalsIgnoreCase(item.status)) {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D97706")); // Orange
                holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#D97706")); // Orange
                holder.tvAmount.setText("+" + item.amount.replace("+", ""));
            } else if ("FAILED".equalsIgnoreCase(item.status)) {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#DC2626")); // Red
                holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#DC2626")); // Red
                holder.tvAmount.setText(item.amount.replace("+", "")); // No plus sign for failed
            }
            
            try {
                holder.ivIcon.setImageResource(item.iconResId);
            } catch (Exception e) {
                // fallback
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvCategory, tvDate, tvRating, tvAmount, tvStatus;
            ImageView ivIcon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_transaction_title);
                tvCategory = itemView.findViewById(R.id.tv_transaction_category);
                tvDate = itemView.findViewById(R.id.tv_transaction_date);
                tvRating = itemView.findViewById(R.id.tv_transaction_rating);
                tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
                tvStatus = itemView.findViewById(R.id.tv_transaction_status);
                ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            }
        }
    }
}
