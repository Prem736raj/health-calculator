// data/dao/UrineColorDao.kt
package com.health.calculator.bmi.tracker.data.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.UrineColorEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface UrineColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UrineColorEntry): Long

    @Query("SELECT * FROM urine_color_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<UrineColorEntry>>

    @Query("SELECT * FROM urine_color_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getForDay(startOfDay: Long, endOfDay: Long): Flow<List<UrineColorEntry>>

    @Query("SELECT * FROM urine_color_log ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): UrineColorEntry?

    @Query("SELECT * FROM urine_color_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<UrineColorEntry>>

    @Delete
    suspend fun delete(entry: UrineColorEntry)

    @Query("DELETE FROM urine_color_log")
    suspend fun deleteAll()
}
