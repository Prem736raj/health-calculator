package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.exportScheduleStore: DataStore<Preferences> by preferencesDataStore(
    name = "export_schedule_prefs"
)

class ExportScheduleManager(private val context: Context) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("export_enabled")
        val FREQUENCY = stringPreferencesKey("export_frequency")
        val FORMAT = stringPreferencesKey("export_format")
        val EMAIL = stringPreferencesKey("export_email")
        val LAST_EXPORT = longPreferencesKey("last_export_time")
    }

    private val dataStore = context.exportScheduleStore

    companion object {
        @Volatile
        private var INSTANCE: ExportScheduleManager? = null

        fun getInstance(context: Context): ExportScheduleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExportScheduleManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val schedule: Flow<ExportSchedule> = dataStore.data.map { prefs ->
        ExportSchedule(
            enabled = prefs[Keys.ENABLED] ?: false,
            frequency = try {
                ExportFrequency.valueOf(prefs[Keys.FREQUENCY] ?: ExportFrequency.MONTHLY.name)
            } catch (e: Exception) {
                ExportFrequency.MONTHLY
            },
            format = try {
                ExportFormat.valueOf(prefs[Keys.FORMAT] ?: ExportFormat.PDF.name)
            } catch (e: Exception) {
                ExportFormat.PDF
            },
            emailAddress = prefs[Keys.EMAIL],
            lastExportTime = prefs[Keys.LAST_EXPORT] ?: 0L
        )
    }

    suspend fun updateSchedule(schedule: ExportSchedule) {
        dataStore.edit { prefs ->
            prefs[Keys.ENABLED] = schedule.enabled
            prefs[Keys.FREQUENCY] = schedule.frequency.name
            prefs[Keys.FORMAT] = schedule.format.name
            prefs[Keys.EMAIL] = schedule.emailAddress ?: ""
        }

        if (schedule.enabled) {
            scheduleExportWork(schedule)
        } else {
            cancelExportWork()
        }
    }

    suspend fun updateLastExportTime() {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_EXPORT] = System.currentTimeMillis()
        }
    }

    private fun scheduleExportWork(schedule: ExportSchedule) {
        val workRequest = PeriodicWorkRequestBuilder<AutoExportWorker>(
            schedule.frequency.days.toLong(), TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("auto_export")
            .setInputData(
                Data.Builder()
                    .putString("format", schedule.format.name)
                    .putString("email", schedule.emailAddress ?: "")
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "health_auto_export",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
    }

    private fun cancelExportWork() {
        WorkManager.getInstance(context)
            .cancelUniqueWork("health_auto_export")
    }
}
