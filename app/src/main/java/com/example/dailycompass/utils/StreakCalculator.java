package com.example.dailycompass.utils;

import com.example.dailycompass.models.CompletionMark;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class StreakCalculator {

    /**
     * Рассчитывает текущую непрерывную серию выполнения привычки
     * @param marks список всех отметок выполнения для привычки
     * @return длина текущей серии в днях
     */
    public static int calculateCurrentStreak(List<CompletionMark> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        // Проверяем, есть ли отметка за сегодня
        boolean hasToday = false;
        for (CompletionMark mark : marks) {
            if (mark.getDate() == today) {
                hasToday = true;
                break;
            }
        }

        if (!hasToday) {
            // Проверяем вчера
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            long yesterday = calendar.getTimeInMillis();
            boolean hasYesterday = false;
            for (CompletionMark mark : marks) {
                if (mark.getDate() == yesterday) {
                    hasYesterday = true;
                    break;
                }
            }
            if (!hasYesterday) {
                return 0;
            }
        }

        // Считаем серию
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int streak = 0;
        for (int i = 0; i < 365; i++) {
            long date = calendar.getTimeInMillis();
            boolean found = false;
            for (CompletionMark mark : marks) {
                if (mark.getDate() == date) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
            streak++;
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }

    /**
     * Проверяет, достигнут ли новый рубеж
     * @param currentStreak текущая серия
     * @param alreadyAchieved уже достигнутые рубежи
     * @return новый рубеж (7,21,100) или 0 если нет
     */
    public static int checkNewMilestone(int currentStreak, List<Integer> alreadyAchieved) {
        int[] milestones = {7, 21, 100};

        for (int milestone : milestones) {
            if (currentStreak >= milestone && !alreadyAchieved.contains(milestone)) {
                return milestone;
            }
        }
        return 0;
    }
}