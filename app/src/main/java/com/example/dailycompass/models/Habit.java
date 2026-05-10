package com.example.dailycompass.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String frequency;      // daily, weekly, custom
    private String reminderTime;   // "14:30" или null
    private String icon;           // название иконки
    private String color;          // цвет в HEX
    private String triggerCondition; // триггер-условие
    private boolean isArchived;    // true = освоена (в архиве)
    private long createdAt;        // дата создания (timestamp)
    private int targetDays;        // целевой рубеж (7,21,100)
    private long masteredDate;     // дата освоения (0 если не освоена)
    private int masteredMilestone; // достигнутый рубеж (0 если не освоена)

    @Ignore
    private int currentStreak;     // текущая серия (не хранится в БД)

    // Конструктор
    public Habit(String name, String frequency, String reminderTime,
                 String icon, String color, String triggerCondition,
                 int targetDays) {
        this.name = name;
        this.frequency = frequency;
        this.reminderTime = reminderTime;
        this.icon = icon;
        this.color = color;
        this.triggerCondition = triggerCondition;
        this.isArchived = false;
        this.createdAt = System.currentTimeMillis();
        this.targetDays = targetDays;
        this.masteredDate = 0;
        this.masteredMilestone = 0;
        this.currentStreak = 0;
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getTargetDays() { return targetDays; }
    public void setTargetDays(int targetDays) { this.targetDays = targetDays; }

    public long getMasteredDate() { return masteredDate; }
    public void setMasteredDate(long masteredDate) { this.masteredDate = masteredDate; }

    public int getMasteredMilestone() { return masteredMilestone; }
    public void setMasteredMilestone(int masteredMilestone) { this.masteredMilestone = masteredMilestone; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
}