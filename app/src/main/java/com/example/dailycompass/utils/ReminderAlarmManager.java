package com.example.dailycompass.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dailycompass.data.AppDatabase;
import com.example.dailycompass.data.HabitDao;
import com.example.dailycompass.models.Habit;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderAlarmManager {

    private static final String TAG = "ReminderAlarmManager";
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void scheduleAllReminders(Context context) {
        executorService.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(context);
            HabitDao habitDao = database.habitDao();
            List<Habit> habits = habitDao.getActiveHabits();

            Log.d(TAG, "Всего активных привычек: " + habits.size());

            for (Habit habit : habits) {
                String reminderTime = habit.getReminderTime();
                if (reminderTime != null && !reminderTime.isEmpty()) {
                    Log.d(TAG, "Установка будильника для: " + habit.getName() + " в " + reminderTime);
                    scheduleReminder(context, habit.getId(), habit.getName(), reminderTime);
                }
            }
        });
    }

    public static void scheduleReminder(Context context, long habitId, String habitName, String reminderTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        String[] timeParts = reminderTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        if (triggerTime <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            triggerTime = calendar.getTimeInMillis();
            Log.d(TAG, "Время уже прошло, устанавливаем на завтра");
        }

        Log.d(TAG, "Установка будильника: " + habitName + " в " + reminderTime);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("habit_id", habitId);
        intent.putExtra("habit_name", habitName);
        intent.putExtra("reminder_time", reminderTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) habitId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
                Log.d(TAG, "Будильник успешно установлен");
            } catch (SecurityException e) {
                Log.e(TAG, "Ошибка установки будильника: " + e.getMessage());
            }
        }
    }

    public static void cancelReminder(Context context, long habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) habitId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Будильник отменён для habitId: " + habitId);
        }
        pendingIntent.cancel();
    }

    public static void rescheduleReminder(Context context, long habitId, String habitName, String reminderTime) {
        cancelReminder(context, habitId);
        if (reminderTime != null && !reminderTime.isEmpty()) {
            scheduleReminder(context, habitId, habitName, reminderTime);
        }
    }
}