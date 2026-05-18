package com.example.dailycompass.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.dailycompass.models.CompletionMark;

import java.util.List;

@Dao
public interface CompletionMarkDao {

    @Insert
    void insert(CompletionMark mark);

    @Update
    void update(CompletionMark mark);

    @Query("SELECT * FROM completion_marks WHERE habitId = :habitId AND date = :date")
    CompletionMark getMarkByDate(long habitId, long date);

    @Query("SELECT * FROM completion_marks WHERE habitId = :habitId ORDER BY date DESC")
    List<CompletionMark> getMarksForHabit(long habitId);

    @Query("SELECT * FROM completion_marks WHERE habitId = :habitId " +
            "AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    List<CompletionMark> getMarksBetweenDates(long habitId, long startDate, long endDate);

    @Query("SELECT COUNT(*) FROM completion_marks WHERE habitId = :habitId")
    int getTotalMarksCount(long habitId);
}