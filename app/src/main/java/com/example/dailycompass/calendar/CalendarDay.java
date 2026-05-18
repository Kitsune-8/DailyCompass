package com.example.dailycompass.calendar;

public class CalendarDay {
    private int dayNumber;
    private boolean isCurrentMonth;
    private boolean isToday;

    public CalendarDay(int dayNumber, boolean isCurrentMonth, boolean isToday) {
        this.dayNumber = dayNumber;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public boolean isToday() {
        return isToday;
    }
}