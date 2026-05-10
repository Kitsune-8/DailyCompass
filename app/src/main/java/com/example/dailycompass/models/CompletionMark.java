package com.example.dailycompass.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "completion_marks",
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = CASCADE))
public class CompletionMark {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long habitId;
    private long date;      // timestamp (начало дня)
    private String note;    // заметка о выполнении

    // Конструктор
    public CompletionMark(long habitId, long date, String note) {
        this.habitId = habitId;
        this.date = date;
        this.note = note;
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getHabitId() { return habitId; }
    public void setHabitId(long habitId) { this.habitId = habitId; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}