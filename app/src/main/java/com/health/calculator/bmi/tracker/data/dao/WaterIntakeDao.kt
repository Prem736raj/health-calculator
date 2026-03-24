// data/dao/WaterIntakeDao.kt
package com.health.calculator.bmi.tracker.data.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculation
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {

    // Calculations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: WaterIntakeCalculation): Long

    @Query("SELECT * FROM water_intake_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<WaterIntakeCalculation>>

    @Query("SELECT * FROM water_intake_calculations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCalculation(): WaterIntakeCalculation?

    @Delete
    suspend fun deleteCalculation(calculation: WaterIntakeCalculation)

    @Query("DELETE FROM water_intake_calculations")
    suspend fun deleteAllCalculations()

    // Daily Water Log
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterIntakeLog): Long

    @Query("SELECT * FROM water_intake_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getWaterLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterIntakeLog>>

    @Query("SELECT SUM(amountMl) FROM water_intake_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getTotalWaterForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query("SELECT * FROM water_intake_log ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterIntakeLog>>

    @Delete
    suspend fun deleteWaterLog(log: WaterIntakeLog)

    @Query("DELETE FROM water_intake_log")
    suspend fun deleteAllWaterLogs()

    @Query("SELECT * FROM water_intake_log WHERE id = :id")
    suspend fun getWaterLogById(id: Long): WaterIntakeLog?

    @Query("SELECT * FROM water_intake_log ORDER BY timestamp ASC")
    fun getAllWaterLogsAsc(): Flow<List<WaterIntakeLog>>

    @Query("""
        SELECT * FROM water_intake_log 
        WHERE timestamp >= :startTime AND timestamp <= :endTime 
        ORDER BY timestamp ASC
    """)
    fun getWaterLogsBetween(startTime: Long, endTime: Long): Flow<List<WaterIntakeLog>>

    @Query("SELECT SUM(amountMl) FROM water_intake_log WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getTotalWaterForDaySync(startOfDay: Long, endOfDay: Long): Int?

    @Query("SELECT SUM(amountMl) FROM water_intake_log WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalWaterBetweenSync(startTime: Long, endTime: Long): Int?
}
