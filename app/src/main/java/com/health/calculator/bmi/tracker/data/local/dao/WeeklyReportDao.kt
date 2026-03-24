// data/local/dao/WeeklyReportDao.kt
package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.models.WeeklyReport
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: WeeklyReport): Long

    @Update
    suspend fun updateReport(report: WeeklyReport)

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC")
    fun getAllReports(): Flow<List<WeeklyReport>>

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC LIMIT 1")
    suspend fun getLatestReport(): WeeklyReport?

    @Query("SELECT * FROM weekly_reports WHERE id = :id")
    suspend fun getReportById(id: Long): WeeklyReport?

    @Query("SELECT * FROM weekly_reports WHERE weekStartDate = :startDate LIMIT 1")
    suspend fun getReportForWeek(startDate: Long): WeeklyReport?

    @Query("SELECT * FROM weekly_reports ORDER BY weekStartDate DESC LIMIT 2")
    suspend fun getLastTwoReports(): List<WeeklyReport>

    @Query("SELECT * FROM weekly_reports WHERE isRead = 0 ORDER BY weekStartDate DESC")
    fun getUnreadReports(): Flow<List<WeeklyReport>>

    @Query("UPDATE weekly_reports SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("SELECT COUNT(*) FROM weekly_reports")
    fun getReportCount(): Flow<Int>

    @Query("DELETE FROM weekly_reports")
    suspend fun deleteAll()
}
