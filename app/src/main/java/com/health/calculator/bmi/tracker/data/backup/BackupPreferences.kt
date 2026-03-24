package com.health.calculator.bmi.tracker.data.backup

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.backupPrefsStore: DataStore<Preferences> by preferencesDataStore(
    name = "backup_prefs"
)

class BackupPreferences(
    private val context: Context
) {
    private object Keys {
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only_backup")
        val LAST_DRIVE_BACKUP_TIME = longPreferencesKey("last_drive_backup_time")
    }

    private val dataStore = context.backupPrefsStore

    companion object {
        @Volatile
        private var INSTANCE: BackupPreferences? = null

        fun getInstance(context: Context): BackupPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackupPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val lastBackupTime: Flow<Long> = dataStore.data.map { it[Keys.LAST_BACKUP_TIME] ?: 0L }

    val autoBackupEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_BACKUP_ENABLED] ?: false }

    val autoBackupFrequency: Flow<BackupFrequency> = dataStore.data.map { prefs ->
        try {
            BackupFrequency.valueOf(prefs[Keys.AUTO_BACKUP_FREQUENCY] ?: BackupFrequency.WEEKLY.name)
        } catch (e: Exception) {
            BackupFrequency.WEEKLY
        }
    }

    val wifiOnly: Flow<Boolean> = dataStore.data.map { it[Keys.WIFI_ONLY] ?: true }

    val lastDriveBackupTime: Flow<Long> = dataStore.data.map { it[Keys.LAST_DRIVE_BACKUP_TIME] ?: 0L }

    suspend fun updateLastBackupTime(time: Long) {
        dataStore.edit { it[Keys.LAST_BACKUP_TIME] = time }
    }

    suspend fun updateAutoBackup(enabled: Boolean, frequency: BackupFrequency, wifiOnly: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_ENABLED] = enabled
            prefs[Keys.AUTO_BACKUP_FREQUENCY] = frequency.name
            prefs[Keys.WIFI_ONLY] = wifiOnly
        }
    }

    suspend fun updateDriveBackupTime(time: Long) {
        dataStore.edit { it[Keys.LAST_DRIVE_BACKUP_TIME] = time }
    }
}
