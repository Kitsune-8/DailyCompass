package com.example.dailycompass.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;

import java.util.List;

public class StatAdapter extends RecyclerView.Adapter<StatAdapter.ViewHolder> {

    private List<StatItem> items;

    public static class StatItem {
        public String period;
        public int completed;
        public int total;
        public int percent;

        public StatItem(String period, int completed, int total, int percent) {
            this.period = period;
            this.completed = completed;
            this.total = total;
            this.percent = percent;
        }
    }

    public StatAdapter(List<StatItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatItem item = items.get(position);
        holder.tvPeriod.setText(item.period);
        holder.tvCompleted.setText(item.completed + "/" + item.total);
        holder.tvPercent.setText(item.percent + "%");
        holder.progressBar.setProgress(item.percent);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void updateList(List<StatItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriod;
        TextView tvCompleted;
        TextView tvPercent;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}