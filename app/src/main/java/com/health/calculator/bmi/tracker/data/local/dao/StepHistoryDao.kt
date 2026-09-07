package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.health.calculator.bmi.tracker.data.model.StepHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StepHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: StepHistoryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<StepHistoryEntry>)

    @Query("SELECT * FROM step_history ORDER BY dayStartMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<StepHistoryEntry>>

    @Query("SELECT * FROM step_history WHERE dayStartMillis BETWEEN :startMillis AND :endMillis ORDER BY dayStartMillis ASC")
    suspend fun getInRange(startMillis: Long, endMillis: Long): List<StepHistoryEntry>

    @Query("DELETE FROM step_history WHERE dayStartMillis < :cutoffMillis")
    suspend fun deleteBefore(cutoffMillis: Long): Int
}
