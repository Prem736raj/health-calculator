package com.health.calculator.bmi.tracker.notifications

import dagger.hilt.android.qualifiers.ApplicationContext

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.data.preferences.WeightReminderPreferences
import com.health.calculator.bmi.tracker.data.preferences.WeightReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.ReminderSchedulePolicy
import java.util.Calendar

class WeightReminderReceiver : BroadcastReceiver() {
    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        val reminderPreferences = WeightReminderPreferences(context)
        if (!reminderPreferences.load().enabled) return
        val reminderManager = WeightReminderManager(context)
        if (!NotificationPermission.canPost(context)) {
            // Keep the preference and next occurrence intact if Android
            // notification access is denied.
            reminderManager.restoreIfEnabled()
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weight Tracking Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly weigh-in reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "weight_tracking")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.health.calculator.bmi.tracker.R.drawable.ic_notification)
            .setContentTitle("Weekly weight check-in")
            .setContentText("If you planned a check-in today, you can record it when convenient.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "If you planned a check-in today, you can record it when convenient. " +
                                "Comparing measurements under similar conditions can make trends easier to read."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        // Alarms are deliberately one-shot so a timezone or clock change does
        // not leave a repeating alarm pointing at the wrong local time.
        reminderManager.restoreIfEnabled()
    }

    companion object {
        const val CHANNEL_ID = "weight_reminder_channel"
        const val NOTIFICATION_ID = 3001
    }
}

class WeightReminderManager @javax.inject.Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferences = WeightReminderPreferences(context)

    fun scheduleWeeklyReminder(dayOfWeek: Int, hour: Int, minute: Int) {
        val settings = WeightReminderSettings(
            enabled = true,
            dayOfWeek = dayOfWeek,
            hour = hour,
            minute = minute
        )
        preferences.save(settings)
        schedule(settings)
    }

    /** Restore an already-enabled reminder without changing its preference. */
    fun restoreIfEnabled() {
        preferences.load().takeIf { it.enabled }?.let(::schedule)
    }

    /** Schedule one inexact occurrence; the receiver schedules the next week. */
    fun schedule(settings: WeightReminderSettings = preferences.load()) {
        if (!settings.enabled) {
            cancelAlarm()
            return
        }

        val intent = Intent(context, WeightReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = ReminderSchedulePolicy.nextWeeklyOccurrence(
            Calendar.getInstance(),
            settings.dayOfWeek,
            settings.hour,
            settings.minute
        )

        // A one-shot inexact alarm is restored by BootReceiver and avoids
        // creating a repeating schedule that can drift after timezone changes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelReminder() {
        preferences.save(preferences.load().copy(enabled = false))
        cancelAlarm()
    }

    /** Stop delivery while keeping the user's chosen weekly time for later. */
    fun pauseReminder() {
        cancelAlarm()
    }

    private fun cancelAlarm() {
        val intent = Intent(context, WeightReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val REQUEST_CODE = 3001
    }
}

