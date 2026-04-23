package com.example.nearneed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchPredictionAdapter extends RecyclerView.Adapter<SearchPredictionAdapter.ViewHolder> {

    public static class GeocodingResult {
        public String primaryText;
        public String secondaryText;
        public double lat;
        public double lng;
        
        public GeocodingResult(String primaryText, String secondaryText, double lat, double lng) {
            this.primaryText = primaryText;
            this.secondaryText = secondaryText;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private final List<GeocodingResult> predictions = new ArrayList<>();
    private final OnPredictionClickListener listener;

    public interface OnPredictionClickListener {
        void onPredictionClick(double lat, double lng, String name);
    }

    public SearchPredictionAdapter(OnPredictionClickListener listener) {
        this.listener = listener;
    }

    public void setPredictions(List<GeocodingResult> features) {
        predictions.clear();
        if (features != null) {
            predictions.addAll(features);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_search_prediction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            GeocodingResult feature = predictions.get(position);
            
            holder.tvPrimary.setText(feature.primaryText);
            holder.tvSecondary.setText(feature.secondaryText);

            holder.itemView.setOnClickListener(v -> {
                listener.onPredictionClick(feature.lat, feature.lng, feature.primaryText);
            });
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrimary, tvSecondary;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrimary = itemView.findViewById(R.id.tv_prediction_primary);
            tvSecondary = itemView.findViewById(R.id.tv_prediction_secondary);
        }
    }
}
