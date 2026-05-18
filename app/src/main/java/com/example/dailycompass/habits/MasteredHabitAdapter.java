package com.example.dailycompass.habits;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;
import com.example.dailycompass.models.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MasteredHabitAdapter extends RecyclerView.Adapter<MasteredHabitViewHolder> {

    private List<Habit> habits;
    private OnMasteredHabitClickListener clickListener;

    public interface OnMasteredHabitClickListener {
        void onMasteredClick(Habit habit);
        void onMasteredLongClick(Habit habit);
    }

    public MasteredHabitAdapter(List<Habit> habits, OnMasteredHabitClickListener clickListener) {
        this.habits = habits;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MasteredHabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mastered_habit, parent, false);
        return new MasteredHabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MasteredHabitViewHolder holder, int position) {
        Habit habit = habits.get(position);

        holder.tvName.setText(habit.getName());

        // Установка цвета карточки
        try {
            int color = Color.parseColor(habit.getColor());
            holder.cardView.setCardBackgroundColor(color);
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FF9800"));
        }

        // Форматирование даты освоения
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String masteredDateStr = sdf.format(new Date(habit.getMasteredDate()));
        holder.tvMasteredDate.setText("Освоена: " + masteredDateStr);

        // Отображение серии
        int streak = habit.getCurrentStreak();
        holder.tvStreak.setText("Серия: " + streak + " дн");

        // Отображение достигнутого рубежа
        int milestone = habit.getMasteredMilestone();
        holder.tvMilestone.setText("🏆 Рубеж: " + milestone + " дней");

        // Обработчик клика
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMasteredClick(habit);
            }
        });

        // Обработчик долгого клика
        holder.cardView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMasteredLongClick(habit);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits == null ? 0 : habits.size();
    }

    public void updateList(List<Habit> newHabits) {
        this.habits = newHabits;
        notifyDataSetChanged();
    }
}