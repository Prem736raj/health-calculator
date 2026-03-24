// app/src/main/java/com/health/calculator/bmi/tracker/notifications/StreakProtectionChecker.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class StreakProtectionScheduler(private val context: Context) {

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

        alarmManager.setRepeating(
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

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = context.getSharedPreferences("streak_protection_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", true)
            if (!enabled) return@launch

            // Check if user has active streaks at risk
            val waterLoggedToday = prefs.getBoolean("water_logged_today_${getTodayKey()}", false)
            val anyActivityToday = prefs.getBoolean("activity_today_${getTodayKey()}", false)
            val currentWaterStreak = prefs.getInt("current_water_streak", 0)
            val currentTrackingStreak = prefs.getInt("current_tracking_streak", 0)

            val hasStreakAtRisk = (currentWaterStreak > 2 && !waterLoggedToday) ||
                    (currentTrackingStreak > 2 && !anyActivityToday)

            if (!hasStreakAtRisk) return@launch

            // Rate limit
            val rateLimiter = NotificationRateLimiter(context)
            if (!rateLimiter.shouldSendNotification(true, "STREAK_PROTECTION").allowed) return@launch

            sendStreakProtectionNotification(
                context,
                waterStreak = currentWaterStreak,
                trackingStreak = currentTrackingStreak,
                waterLoggedToday = waterLoggedToday,
                activityToday = anyActivityToday,
                freezeAvailable = prefs.getInt("streak_freeze_count", 1) > 0
            )

            rateLimiter.recordNotificationSent("STREAK_PROTECTION")
        }
    }

    private fun sendStreakProtectionNotification(
        context: Context,
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

        val title = "🔥 Your ${streakAtRisk}-day streak is at risk!"

        val message = when (streakType) {
            "water" -> "You haven't logged water today. A quick glass can save your streak!"
            else -> "Log something quick to keep your streak alive!"
        }

        val freezeText = if (freezeAvailable) "\n\n🛡️ Streak freeze is available to protect your streak!" else ""

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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .addAction(0, "💧 Log Water Now", waterLogPending)

        if (freezeAvailable) {
            val freezeIntent = Intent(context, StreakFreezeReceiver::class.java).apply {
                putExtra("streak_type", streakType)
            }
            val freezePending = PendingIntent.getBroadcast(
                context, 9103, freezeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "🛡️ Use Streak Freeze", freezePending)
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    companion object {
        const val NOTIFICATION_ID = 9110
    }
}
