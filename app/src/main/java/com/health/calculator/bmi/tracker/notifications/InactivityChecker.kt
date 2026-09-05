// app/src/main/java/com/health/calculator/bmi/tracker/notifications/InactivityChecker.kt
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
import com.health.calculator.bmi.tracker.data.models.InactivityLevel
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class InactivityCheckScheduler(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDaily() {
        val intent = Intent(context, InactivityCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 11) // Check at 11 AM
            set(Calendar.MINUTE, 0)
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
        val intent = Intent(context, InactivityCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val REQUEST_CODE = 9000
    }
}

class InactivityCheckReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        launchAsync {
            val state = runCatching {
                InactivityRepository(context).getInactivityState().first()
            }.getOrNull() ?: return@launchAsync

            // Re-engagement is explicitly opt-in. This also keeps the receiver
            // in sync with the Settings toggle instead of relying on a stale
            // SharedPreferences flag.
            if (!state.inactivityNotificationsEnabled) return@launchAsync

            val prefs = context.getSharedPreferences("inactivity_quick", Context.MODE_PRIVATE)
            val lastOpen = prefs.getLong("last_open", state.lastAppOpenTime)

            val now = System.currentTimeMillis()
            val daysInactive = ((now - lastOpen) / (24 * 60 * 60 * 1000)).toInt()
            val lastLevel = maxOf(
                state.lastInactivityNotificationLevel,
                prefs.getInt("last_notif_level", 0)
            )

            val level = InactivityLevel.forDays(daysInactive) ?: return@launchAsync
            val levelNumber = InactivityLevel.getLevelNumber(level)

            // Only send if we haven't sent this level yet
            if (levelNumber <= lastLevel) return@launchAsync

            // Don't send after 30 days
            if (daysInactive > 35) return@launchAsync

            // Rate limit check
            val rateLimiter = NotificationRateLimiter(context)
            if (!rateLimiter.shouldSendNotification(false, "INACTIVITY").allowed) return@launchAsync

            if (!NotificationPermission.canPost(context)) return@launchAsync

            sendInactivityNotification(context, level)

            prefs.edit().putInt("last_notif_level", levelNumber).apply()
            runCatching { InactivityRepository(context).updateNotificationLevel(levelNumber) }
            rateLimiter.recordNotificationSent("INACTIVITY")
        }
    }

    private fun sendInactivityNotification(@ApplicationContext context: Context, level: InactivityLevel) {
        NotificationChannelsManager.createAllChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_inactivity", true)
            putExtra("show_welcome_back", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 9001, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick water log action
        val waterIntent = Intent(context, QuickActionReceiver::class.java).apply {
            action = "LOG_WATER"
            putExtra("reminder_id", "inactivity_${level.days}")
            putExtra("action_value", "250")
        }
        val waterPending = PendingIntent.getBroadcast(
            context, 9002, waterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationChannelsManager.CHANNEL_HEALTH_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(level.title)
            .setContentText(level.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(level.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(0, "💧 Quick Water Log", waterPending)
            .addAction(0, "${level.emoji} Open App", pendingIntent)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val NOTIFICATION_ID = 9010
    }
}

