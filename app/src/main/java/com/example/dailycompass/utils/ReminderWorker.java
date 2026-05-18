package com.example.dailycompass.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dailycompass.data.AppDatabase;
import com.example.dailycompass.data.HabitDao;
import com.example.dailycompass.models.Habit;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";
    private ExecutorService executorService;

    // Массив подбадривающих фраз
    private static final String[] MOTIVATIONAL_PHRASES = {
            "Пора приступить к выполнению! 💪",
            "Маленький шаг сегодня — большой результат завтра! 🌟",
            "Не откладывай на потом! Сделай это сейчас! ⏰",
            "Ты сможешь! Всего одна привычка сегодня! 🎯",
            "Время для привычки! Помни о своей цели! 🎯",
            "Каждый день приближает тебя к успеху! 🚀",
            "Не теряй momentum! Выполни привычку сегодня! 📈",
            "Ты на верном пути! Продолжай в том же духе! 🌈",
            "Маленькие шаги создают большие изменения! 🦋",
            "Верь в себя! У тебя всё получится! ✨",
            "Сегодня отличный день для новых достижений! ☀️",
            "Твоя будущая версия поблагодарит тебя за это! 🌟"
    };

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        executorService = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            checkAndSendReminders();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при отправке напоминаний", e);
            return Result.retry();
        }
    }

    private void checkAndSendReminders() {
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        HabitDao habitDao = database.habitDao();

        // Получаем текущее время
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        String currentTime = String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));

        Log.d(TAG, "Проверка напоминаний в " + currentTime);

        // Получаем все активные привычки
        List<Habit> habits = habitDao.getActiveHabits();

        for (Habit habit : habits) {
            String reminderTime = habit.getReminderTime();
            if (reminderTime != null && !reminderTime.isEmpty()) {
                Log.d(TAG, "Привычка: " + habit.getName() + ", время: " + reminderTime);

                // Сравниваем время (игнорируем секунды)
                if (currentTime.equals(reminderTime)) {
                    // Выбираем случайную фразу
                    String randomPhrase = MOTIVATIONAL_PHRASES[new Random().nextInt(MOTIVATIONAL_PHRASES.length)];
                    String message = habit.getName() + "! " + randomPhrase;

                    // Отправляем уведомление
                    NotificationHelper.sendReminderNotification(
                            getApplicationContext(),
                            habit.getName(),
                            message
                    );
                    Log.d(TAG, "Отправлено напоминание для привычки: " + habit.getName());
                }
            }
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}