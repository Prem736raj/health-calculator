package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.local.dao.ReminderDao
import com.health.calculator.bmi.tracker.data.models.QuietHours
import com.health.calculator.bmi.tracker.data.models.Reminder
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reminder_preferences"
)

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val context: Context
) {
    private object Keys {
        val QUIET_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_START_HOUR = intPreferencesKey("quiet_start_hour")
        val QUIET_START_MIN = intPreferencesKey("quiet_start_minute")
        val QUIET_END_HOUR = intPreferencesKey("quiet_end_hour")
        val QUIET_END_MIN = intPreferencesKey("quiet_end_minute")
        val QUIET_EMERGENCY = booleanPreferencesKey("quiet_emergency_override")
    }

    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    fun getActiveReminders(): Flow<List<Reminder>> = reminderDao.getActiveReminders()

    fun getRemindersByCategory(category: ReminderCategory): Flow<List<Reminder>> =
        reminderDao.getRemindersByCategory(category.name)

    fun getActiveCount(): Flow<Int> = reminderDao.getActiveCount()

    suspend fun getReminderById(id: String): Reminder? = reminderDao.getReminderById(id)

    suspend fun saveReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun toggleReminder(id: String, enabled: Boolean) {
        reminderDao.setEnabled(id, enabled)
    }

    suspend fun deleteReminder(id: String) {
        reminderDao.deleteById(id)
    }

    suspend fun deleteAll() {
        reminderDao.deleteAll()
    }

    suspend fun updateLastTriggered(id: String, timestamp: Long = System.currentTimeMillis()) {
        reminderDao.updateLastTriggered(id, timestamp)
    }

    // Quiet Hours
    fun getQuietHoursFlow(): Flow<QuietHours> {
        return context.reminderPrefsDataStore.data.map { prefs ->
            QuietHours(
                isEnabled = prefs[Keys.QUIET_ENABLED] ?: false,
                startHour = prefs[Keys.QUIET_START_HOUR] ?: 22,
                startMinute = prefs[Keys.QUIET_START_MIN] ?: 0,
                endHour = prefs[Keys.QUIET_END_HOUR] ?: 7,
                endMinute = prefs[Keys.QUIET_END_MIN] ?: 0,
                allowEmergencyOverride = prefs[Keys.QUIET_EMERGENCY] ?: true
            )
        }
    }

    suspend fun getQuietHours(): QuietHours {
        val sp = context.getSharedPreferences("quiet_hours_prefs", Context.MODE_PRIVATE)
        return QuietHours(
            isEnabled = sp.getBoolean("quiet_enabled", false),
            startHour = sp.getInt("quiet_start_h", 22),
            startMinute = sp.getInt("quiet_start_m", 0),
            endHour = sp.getInt("quiet_end_h", 7),
            endMinute = sp.getInt("quiet_end_m", 0),
            allowEmergencyOverride = sp.getBoolean("quiet_emergency", true)
        )
    }

    suspend fun saveQuietHours(quietHours: QuietHours) {
        context.reminderPrefsDataStore.edit { prefs ->
            prefs[Keys.QUIET_ENABLED] = quietHours.isEnabled
            prefs[Keys.QUIET_START_HOUR] = quietHours.startHour
            prefs[Keys.QUIET_START_MIN] = quietHours.startMinute
            prefs[Keys.QUIET_END_HOUR] = quietHours.endHour
            prefs[Keys.QUIET_END_MIN] = quietHours.endMinute
            prefs[Keys.QUIET_EMERGENCY] = quietHours.allowEmergencyOverride
        }

        // Also save to standard SharedPrefs for BroadcastReceiver access (synchronous/lightweight)
        val sp = context.getSharedPreferences("quiet_hours_prefs", Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean("quiet_enabled", quietHours.isEnabled)
            .putInt("quiet_start_h", quietHours.startHour)
            .putInt("quiet_start_m", quietHours.startMinute)
            .putInt("quiet_end_h", quietHours.endHour)
            .putInt("quiet_end_m", quietHours.endMinute)
            .putBoolean("quiet_emergency", quietHours.allowEmergencyOverride)
            .apply()
    }
}
