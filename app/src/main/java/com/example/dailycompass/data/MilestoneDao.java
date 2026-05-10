package com.example.dailycompass.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.dailycompass.models.Milestone;

import java.util.List;

@Dao
public interface MilestoneDao {

    @Insert
    void insert(Milestone milestone);

    @Query("SELECT * FROM milestones WHERE habitId = :habitId ORDER BY achievedDate ASC")
    List<Milestone> getMilestonesForHabit(long habitId);

    @Query("SELECT * FROM milestones WHERE habitId = :habitId AND targetValue = :targetValue")
    Milestone getMilestoneByValue(long habitId, int targetValue);

    @Query("SELECT COUNT(*) > 0 FROM milestones WHERE habitId = :habitId AND targetValue = :targetValue")
    boolean isMilestoneAchieved(long habitId, int targetValue);
}