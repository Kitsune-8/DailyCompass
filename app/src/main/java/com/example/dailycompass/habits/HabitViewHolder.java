package com.example.dailycompass.habits;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;

public class HabitViewHolder extends RecyclerView.ViewHolder {

    public CardView cardView;
    public ImageView ivIcon;
    public TextView tvName;
    public TextView tvStreak;
    public TextView tvTrigger;
    public TextView tvFrequency;
    public TextView tvReminder;
    public Button btnComplete;

    public HabitViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.cardView);
        ivIcon = itemView.findViewById(R.id.ivIcon);
        tvName = itemView.findViewById(R.id.tvName);
        tvStreak = itemView.findViewById(R.id.tvStreak);
        tvTrigger = itemView.findViewById(R.id.tvTrigger);
        tvFrequency = itemView.findViewById(R.id.tvFrequency);
        tvReminder = itemView.findViewById(R.id.tvReminder);
        btnComplete = itemView.findViewById(R.id.btnComplete);
    }
}