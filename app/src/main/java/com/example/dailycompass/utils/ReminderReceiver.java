package com.example.dailycompass.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String habitName = intent.getStringExtra("habit_name");
        String reminderTime = intent.getStringExtra("reminder_time");

        Log.d(TAG, "Получен сигнал будильника! habitName=" + habitName + ", reminderTime=" + reminderTime);

        if (habitName != null) {
            String message = habitName + "! Пора приступить к выполнению! 💪";
            NotificationHelper.sendReminderNotification(context, habitName, message);
            Log.d(TAG, "Уведомление отправлено для: " + habitName);
        }
    }
}