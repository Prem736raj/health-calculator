// app/src/main/java/com/health/calculator/bmi/tracker/notifications/StreakProtectionChecker.kt
package com.health.calculator.bmi.tracker.notifications

import dagger.hilt.android.qualifiers.ApplicationContext

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.core.util.launchAsync
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import com.health.calculator.bmi.tracker.domain.engagement.WellnessEngagementPolicy
import kotlinx.coroutines.flow.first
import java.util.Calendar

class StreakProtectionScheduler(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEvening() {
        val intent = Intent(context, StreakProtectionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel() {
        val intent = Intent(context, StreakProtectionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val REQUEST_CODE = 9100
    }
}

class StreakProtectionReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        launchAsync {
            val engagementState = runCatching {
                InactivityRepository(context).getInactivityState().first()
            }.getOrNull() ?: return@launchAsync

            // The Settings toggle is the source of truth. A streak reminder is
            // never allowed to become a background high-priority notification.
            if (!engagementState.streakProtectionEnabled) return@launchAsync

            // Read streak state from the same Room/DataStore sources used by the
            // tracker screens. The old implementation read four SharedPreferences
            // keys that were never written, so this notification could never reflect
            // the user's real streak and could advertise a stale freeze count.
            val database = AppDatabase.getDatabase(context)
            val today = Calendar.getInstance()
            val startOfToday = startOfDay(today)
            val endOfToday = Calendar.getInstance().apply {
                timeInMillis = startOfToday
                add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis

            val waterLoggedToday = database.waterIntakeDao()
                .getTotalWaterForDaySync(startOfToday, endOfToday)
                ?.let { it > 0 }
                ?: false
            val currentWaterStreak = database.waterGamificationDao()
                .getStreakData()
                ?.currentStreak
                ?: 0
            val recentHistory = database.historyDao().getAllEntriesSync(MAX_HISTORY_ENTRIES)
            val anyActivityToday = recentHistory.any { startOfDay(it.timestamp) == startOfToday }
            val currentTrackingStreak = calculateCurrentTrackingStreak(
                recentHistory.map { it.timestamp },
                startOfToday,
                anyActivityToday
            )
            val freezeCount = InactivityRepository(context).getStreakFreezeCount().first()

            val hasStreakAtRisk = (currentWaterStreak > 2 && !waterLoggedToday) ||
                    (currentTrackingStreak > 2 && !anyActivityToday)

            if (!hasStreakAtRisk) return@launchAsync

            // Rate limit
            val rateLimiter = NotificationRateLimiter(context)
            if (!rateLimiter.shouldSendNotification(false, "STREAK_PROTECTION").allowed) return@launchAsync

            if (!NotificationPermission.canPost(context)) return@launchAsync

            sendStreakProtectionNotification(
                context,
                waterStreak = currentWaterStreak,
                trackingStreak = currentTrackingStreak,
                waterLoggedToday = waterLoggedToday,
                activityToday = anyActivityToday,
                freezeAvailable = freezeCount > 0
            )

            rateLimiter.recordNotificationSent("STREAK_PROTECTION")
        }
    }

    private fun sendStreakProtectionNotification(
        @ApplicationContext context: Context,
        waterStreak: Int,
        trackingStreak: Int,
        waterLoggedToday: Boolean,
        activityToday: Boolean,
        freezeAvailable: Boolean
    ) {
        NotificationChannelsManager.createAllChannels(context)

        val streakAtRisk = when {
            waterStreak > trackingStreak && !waterLoggedToday -> waterStreak
            !activityToday -> trackingStreak
            else -> waterStreak
        }

        val streakType = when {
            waterStreak > trackingStreak && !waterLoggedToday -> "water"
            !activityToday -> "tracking"
            else -> "water"
        }

        val title = WellnessEngagementPolicy.streakReminderTitle(streakAtRisk)
        val message = WellnessEngagementPolicy.streakReminderMessage(streakType)
        val freezeText = if (freezeAvailable) {
            "\n\nAn optional day buffer is available. You can also skip today—your history remains saved."
        } else ""

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_route", if (streakType == "water") "water_intake" else "home")
        }
        val openPending = PendingIntent.getActivity(
            context, 9101, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick water log
        val waterLogIntent = Intent(context, QuickActionReceiver::class.java).apply {
            action = "LOG_WATER"
            putExtra("reminder_id", "streak_protection")
            putExtra("action_value", "250")
        }
        val waterLogPending = PendingIntent.getBroadcast(
            context, 9102, waterLogIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_HEALTH_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message + freezeText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(0, "💧 Log Water Now", waterLogPending)

        if (freezeAvailable) {
            val freezeIntent = Intent(context, StreakFreezeReceiver::class.java).apply {
                putExtra("streak_type", streakType)
            }
            val freezePending = PendingIntent.getBroadcast(
                context, 9103, freezeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Use optional day buffer", freezePending)
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    private fun calculateCurrentTrackingStreak(
        timestamps: List<Long>,
        todayStart: Long,
        activityToday: Boolean
    ): Int {
        val activeDays = timestamps.map(::startOfDay).toSet()
        if (activeDays.isEmpty()) return 0

        val cursor = Calendar.getInstance().apply {
            timeInMillis = todayStart
            if (!activityToday) add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (activeDays.contains(cursor.timeInMillis)) {
            streak++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun startOfDay(calendar: Calendar): Long {
        return Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        const val NOTIFICATION_ID = 9110
        private const val MAX_HISTORY_ENTRIES = 1_000
    }
}

