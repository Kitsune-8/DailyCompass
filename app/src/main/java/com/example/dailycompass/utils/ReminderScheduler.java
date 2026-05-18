package com.example.dailycompass.utils;

import android.content.Context;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final long REPEAT_INTERVAL = 15;
    private static final TimeUnit REPEAT_UNIT = TimeUnit.MINUTES;

    public static void scheduleReminders(Context context) {
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                REPEAT_INTERVAL,
                REPEAT_UNIT
        )
                .addTag("habit_reminders")
                .build();

        WorkManager.getInstance(context).enqueue(reminderWork);
    }

    public static void cancelAllReminders(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_reminders");
    }
}