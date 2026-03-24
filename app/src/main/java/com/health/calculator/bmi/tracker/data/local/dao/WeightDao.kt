package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(entry: WeightEntry): Long

    @Delete
    suspend fun deleteWeight(entry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY dateMillis DESC")
    fun getAllWeights(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY dateMillis DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries ORDER BY dateMillis ASC LIMIT 1")
    fun getFirstWeight(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE dateMillis >= :startMillis ORDER BY dateMillis ASC")
    fun getWeightsSince(startMillis: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis ASC")
    fun getWeightsInRange(startMillis: Long, endMillis: Long): Flow<List<WeightEntry>>

    @Query("SELECT MIN(weightKg) FROM weight_entries")
    fun getLowestWeight(): Flow<Double?>

    @Query("SELECT MAX(weightKg) FROM weight_entries")
    fun getHighestWeight(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM weight_entries")
    fun getEntryCount(): Flow<Int>

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
