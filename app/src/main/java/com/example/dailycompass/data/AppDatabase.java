package com.example.dailycompass.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.dailycompass.models.CompletionMark;
import com.example.dailycompass.models.Habit;
import com.example.dailycompass.models.Milestone;

@Database(entities = {Habit.class, CompletionMark.class, Milestone.class},
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HabitDao habitDao();
    public abstract CompletionMarkDao completionMarkDao();
    public abstract MilestoneDao milestoneDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "daily_compass_db"
                    ).build();
                }
            }
        }
        return instance;
    }
}