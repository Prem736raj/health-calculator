package com.health.calculator.bmi.tracker.data.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.ParsedHistoryEntry
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.SettingsRepository
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import kotlinx.coroutines.flow.first

class AutoBackupWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(context)
            val historyRepository = HistoryRepository(database.historyDao())
            val localBackupManager = LocalBackupManager(context)
            val googleDriveManager = GoogleDriveBackupManager.getInstance(context)
            val backupPreferences = BackupPreferences.getInstance(context)
            val profileRepository = ProfileRepository(ProfileDataStore(context))
            val settingsRepository = SettingsRepository(SettingsDataStore(context))

            val entries = historyRepository.getAllEntries().first()
            if (entries.isEmpty()) return Result.success()

            val displayEntries = entries.map { it.toDisplayEntry() }

            // Create local backup first
            val profile = profileRepository.getProfile().first()
            val settings = settingsRepository.settingsFlow.first()
            val backupFile = localBackupManager.createLocalBackup(
                historyEntries =
                    displayEntries,
                profileData = mapOf(
                    "name" to profile.name,
                    "gender" to profile.gender.name,
                    "heightCm" to (profile.heightCm ?: 0f).toString(),
                    "weightKg" to (profile.weightKg ?: 0f).toString(),
                    "goalWeightKg" to (profile.goalWeightKg ?: 0f).toString()
                ),
                settingsData = mapOf(
                    "unitSystem" to settings.unitSystem.name,
                    "themeMode" to settings.themeMode.name,
                    "remindersEnabled" to settings.remindersEnabled.toString(),
                    "waterReminderEnabled" to settings.waterReminderEnabled.toString(),
                    "weightReminderEnabled" to settings.weightReminderEnabled.toString()
                ),
                achievementsData = emptyMap(),
                onProgress = { }
            )

            // Upload to Google Drive if signed in
            if (googleDriveManager.isSignedIn() && googleDriveManager.initDriveService()) {
                val encryptedData = backupFile.readBytes()
                googleDriveManager.uploadBackup(encryptedData, displayEntries.size) { }
            }

            backupPreferences.updateLastBackupTime(System.currentTimeMillis())

            showNotification("Backup complete", "${displayEntries.size} entries backed up")

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "auto_backup"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Auto Backup", NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Using existing launcher icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        nm.notify(9002, notification)
    }
}
