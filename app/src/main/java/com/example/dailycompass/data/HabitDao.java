package com.example.dailycompass.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dailycompass.models.Habit;

import java.util.List;

@Dao
public interface HabitDao {

    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id DESC")
    List<Habit> getActiveHabits();

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY masteredDate DESC")
    List<Habit> getArchivedHabits();

    @Query("SELECT * FROM habits WHERE id = :habitId")
    Habit getHabitById(long habitId);

    @Query("UPDATE habits SET isArchived = 1, masteredDate = :masteredDate, " +
            "masteredMilestone = :milestone WHERE id = :habitId")
    void archiveHabit(long habitId, long masteredDate, int milestone);
}