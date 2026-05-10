package com.example.dailycompass.habits;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;
import com.example.dailycompass.models.Habit;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitViewHolder> {

    private List<Habit> habits;
    private OnHabitClickListener clickListener;
    private OnCompleteClickListener completeClickListener;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
        void onHabitLongClick(Habit habit);
    }

    public interface OnCompleteClickListener {
        void onCompleteClick(Habit habit);
    }

    public HabitAdapter(List<Habit> habits,
                        OnHabitClickListener clickListener,
                        OnCompleteClickListener completeClickListener) {
        this.habits = habits;
        this.clickListener = clickListener;
        this.completeClickListener = completeClickListener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);

        holder.tvName.setText(habit.getName());

        // Установка цвета карточки
        try {
            int color = Color.parseColor(habit.getColor());
            holder.cardView.setCardBackgroundColor(color);
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#4CAF50"));
        }

        // Отображение текущей серии
        int streak = habit.getCurrentStreak();
        if (streak > 0) {
            holder.tvStreak.setText(streak + " дн");
            holder.tvStreak.setVisibility(View.VISIBLE);
        } else {
            holder.tvStreak.setVisibility(View.GONE);
        }

        // Триггер-условие
        if (habit.getTriggerCondition() != null && !habit.getTriggerCondition().isEmpty()) {
            holder.tvTrigger.setText("📌 " + habit.getTriggerCondition());
            holder.tvTrigger.setVisibility(View.VISIBLE);
        } else {
            holder.tvTrigger.setVisibility(View.GONE);
        }

        // Частота
        String frequencyText = getFrequencyText(habit.getFrequency());
        holder.tvFrequency.setText(frequencyText);

        // Время напоминания
        if (habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()) {
            holder.tvReminder.setText("⏰ " + habit.getReminderTime());
        } else {
            holder.tvReminder.setText("");
        }

        // Обработчик клика по карточке (редактирование)
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onHabitClick(habit);
            }
        });

        // Обработчик долгого клика (удаление)
        holder.cardView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onHabitLongClick(habit);
            }
            return true;
        });

        // Обработчик кнопки "Отметить выполнение"
        holder.btnComplete.setOnClickListener(v -> {
            if (completeClickListener != null) {
                completeClickListener.onCompleteClick(habit);
            }
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

    public void updateStreak(int position, int streak) {
        if (position >= 0 && position < habits.size()) {
            habits.get(position).setCurrentStreak(streak);
            notifyItemChanged(position);
        }
    }

    private String getFrequencyText(String frequency) {
        if (frequency == null) return "📅 Ежедневно";

        switch (frequency) {
            case "daily":
                return "📅 Ежедневно";
            case "weekly":
                return "📆 Еженедельно";
            case "custom":
                return "⚙️ Пользовательская";
            default:
                return "📅 Ежедневно";
        }
    }
}