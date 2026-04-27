package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProviderScheduleAdapter extends RecyclerView.Adapter<ProviderScheduleAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
    private final SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.getDefault());

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.tvTitle.setText(booking.postTitle != null ? booking.postTitle : "Service Task");
        holder.tvSubtitle.setText(booking.seekerName != null ? "For " + booking.seekerName : "Community Task");

        if (booking.timestamp != null) {
            Date date = new Date(booking.timestamp);
            holder.tvTime.setText(timeFormat.format(date));
            holder.tvAmpm.setText(ampmFormat.format(date).toUpperCase());
        } else {
            holder.tvTime.setText("--:--");
            holder.tvAmpm.setText("??");
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvAmpm, tvTitle, tvSubtitle;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_schedule_time);
            tvAmpm = itemView.findViewById(R.id.tv_schedule_ampm);
            tvTitle = itemView.findViewById(R.id.tv_schedule_title);
            tvSubtitle = itemView.findViewById(R.id.tv_schedule_subtitle);
        }
    }
}
