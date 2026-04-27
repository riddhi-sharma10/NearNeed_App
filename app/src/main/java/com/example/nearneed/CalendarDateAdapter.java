package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarDateAdapter extends RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder> {

    private final List<Date> dates;
    private int selectedPosition = 0;
    private final OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public CalendarDateAdapter(List<Date> dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dates.get(position);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());

        holder.tvDayName.setText(dayNameFormat.format(date).toUpperCase());
        holder.tvDayNumber.setText(dayNumberFormat.format(date));

        boolean isSelected = position == selectedPosition;
        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.sapphire_primary));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        } else {
            holder.itemView.setBackground(null);
            holder.tvDayName.setTextColor(0xFF94A3B8);
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            listener.onDateSelected(date);
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber;
        View indicator;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
}
