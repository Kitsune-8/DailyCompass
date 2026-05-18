package com.example.dailycompass.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.dailycompass.R;
import com.example.dailycompass.habits.HabitsActivity;
import com.example.dailycompass.habits.MasteredActivity;

import java.util.Random;

public class NotificationHelper {

    private static final String REMINDER_CHANNEL_ID = "habit_reminders";
    private static final String REMINDER_CHANNEL_NAME = "Напоминания о привычках";
    private static final String MILESTONE_CHANNEL_ID = "habit_milestones";
    private static final String MILESTONE_CHANNEL_NAME = "Достижения привычек";

    private static final String[] CONGRATULATIONS = {
            "Отлично! 🎉", "Поздравляем! 🏆", "Великолепно! ⭐", "Так держать! 💪",
            "Вы молодец! 👏", "Потрясающий результат! 🔥", "Гордимся вами! 🌟",
            "Продолжайте в том же духе! 📈", "Новый рекорд! 🥇", "Вы на верном пути! 🎯"
    };

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
            "Верь в себя! У тебя всё получится! ✨"
    };

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reminderChannel = new NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    REMINDER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Напоминания о необходимости выполнить привычки");
            reminderChannel.enableVibration(true);
            reminderChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationChannel milestoneChannel = new NotificationChannel(
                    MILESTONE_CHANNEL_ID,
                    MILESTONE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            milestoneChannel.setDescription("Уведомления о достижении контрольных рубежей");
            milestoneChannel.enableVibration(true);
            milestoneChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(reminderChannel);
                manager.createNotificationChannel(milestoneChannel);
            }
        }
    }

    public static void sendReminderNotification(Context context, String habitName, String message) {
        Intent intent = new Intent(context, HabitsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String randomPhrase = MOTIVATIONAL_PHRASES[new Random().nextInt(MOTIVATIONAL_PHRASES.length)];
        String fullMessage = habitName + "! " + randomPhrase;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle("⏰ Напоминание о привычке")
                .setContentText(fullMessage)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(fullMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }

    public static void sendMilestoneNotification(Context context, String habitName, int days, int milestone) {
        String randomCongrats = CONGRATULATIONS[new Random().nextInt(CONGRATULATIONS.length)];
        String title = "🎉 " + randomCongrats + " 🎉";
        String message = "Вы выполнили привычку \"" + habitName + "\" " + days + " дней подряд!\nДостигнут рубеж: " + milestone + " дней!";

        Intent intent = new Intent(context, HabitsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MILESTONE_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }

    public static void sendMasteredNotification(Context context, String habitName, int days) {
        String title = "🏆 Новая освоенная привычка! 🏆";
        String message = "Поздравляем! Привычка \"" + habitName + "\" успешно освоена за " + days + " дней!";

        Intent intent = new Intent(context, MasteredActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MILESTONE_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }
}