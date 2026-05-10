package com.example.dailycompass.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "milestones",
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = CASCADE))
public class Milestone {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long habitId;
    private int targetValue;    // 7, 21 или 100
    private long achievedDate;  // дата достижения рубежа

    // Конструктор
    public Milestone(long habitId, int targetValue, long achievedDate) {
        this.habitId = habitId;
        this.targetValue = targetValue;
        this.achievedDate = achievedDate;
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getHabitId() { return habitId; }
    public void setHabitId(long habitId) { this.habitId = habitId; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public long getAchievedDate() { return achievedDate; }
    public void setAchievedDate(long achievedDate) { this.achievedDate = achievedDate; }
}