package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BloodPressureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<BloodPressureEntity>)

    @Delete
    suspend fun deleteReading(reading: BloodPressureEntity)

    @Query("DELETE FROM blood_pressure_readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)

    @Query("DELETE FROM blood_pressure_readings WHERE averageGroupId = :groupId")
    suspend fun deleteReadingsByGroupId(groupId: String)

    @Query("SELECT * FROM blood_pressure_readings ORDER BY measurementTimestamp DESC")
    fun getAllReadings(): Flow<List<BloodPressureEntity>>

    @Query("SELECT * FROM blood_pressure_readings WHERE isPartOfAverage = 0 ORDER BY measurementTimestamp DESC")
    fun getMainReadings(): Flow<List<BloodPressureEntity>>

    @Query("SELECT * FROM blood_pressure_readings WHERE id = :id")
    suspend fun getReadingById(id: Long): BloodPressureEntity?

    @Query("SELECT * FROM blood_pressure_readings WHERE averageGroupId = :groupId ORDER BY measurementTimestamp ASC")
    suspend fun getReadingsByGroupId(groupId: String): List<BloodPressureEntity>

    @Query("SELECT * FROM blood_pressure_readings ORDER BY measurementTimestamp DESC LIMIT 1")
    suspend fun getLatestReading(): BloodPressureEntity?

    @Query("SELECT * FROM blood_pressure_readings ORDER BY measurementTimestamp DESC LIMIT 1")
    fun getLatestReadingFlow(): Flow<BloodPressureEntity?>

    @Query("SELECT * FROM blood_pressure_readings ORDER BY measurementTimestamp DESC LIMIT :limit")
    suspend fun getRecentReadings(limit: Int): List<BloodPressureEntity>

    @Query("SELECT COUNT(*) FROM blood_pressure_readings WHERE isPartOfAverage = 0")
    fun getReadingsCount(): Flow<Int>

    @Query("UPDATE blood_pressure_readings SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Query("DELETE FROM blood_pressure_readings")
    suspend fun deleteAll()
}
