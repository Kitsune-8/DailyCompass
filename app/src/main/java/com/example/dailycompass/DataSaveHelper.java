package com.example.dailycompass;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.dailycompass.utils.NotificationHelper;
import com.example.dailycompass.utils.ReminderAlarmManager;

public class DataSaveHelper extends Application {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences("DailyCompassPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        NotificationHelper.createNotificationChannels(this);
        ReminderAlarmManager.scheduleAllReminders(this);

        startAutoSave();
    }

    private void startAutoSave() {
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                editor.putLong("autoSaveTime", System.currentTimeMillis());
                editor.putBoolean("autoSaveFlag", true);
                editor.apply();
                handler.postDelayed(this, 30000);
            }
        };

        handler.postDelayed(autoSaveRunnable, 30000);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        editor.putLong("emergencySaveTime", System.currentTimeMillis());
        editor.putBoolean("emergencySave", true);
        editor.apply();
    }
}