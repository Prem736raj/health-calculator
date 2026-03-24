package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.models.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): Reminder?

    @Query("SELECT * FROM reminders WHERE category = :category")
    fun getRemindersByCategory(category: String): Flow<List<Reminder>>

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE reminders SET lastTriggered = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM reminders WHERE isEnabled = 1")
    fun getActiveCount(): Flow<Int>

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
