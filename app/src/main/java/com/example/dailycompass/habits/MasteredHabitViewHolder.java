package com.example.dailycompass.habits;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;

public class MasteredHabitViewHolder extends RecyclerView.ViewHolder {

    public CardView cardView;
    public ImageView ivIcon;
    public TextView tvName;
    public TextView tvMasteredDate;
    public TextView tvStreak;
    public TextView tvMilestone;

    public MasteredHabitViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.cardView);
        ivIcon = itemView.findViewById(R.id.ivIcon);
        tvName = itemView.findViewById(R.id.tvName);
        tvMasteredDate = itemView.findViewById(R.id.tvMasteredDate);
        tvStreak = itemView.findViewById(R.id.tvStreak);
        tvMilestone = itemView.findViewById(R.id.tvMilestone);
    }
}