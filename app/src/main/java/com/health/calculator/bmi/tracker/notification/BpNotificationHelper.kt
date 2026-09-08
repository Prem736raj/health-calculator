// notification/BpNotificationHelper.kt
package com.health.calculator.bmi.tracker.notification

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
import com.health.calculator.bmi.tracker.notifications.NotificationPermission
import java.time.Instant
import java.time.ZoneId

class BpNotificationHelper(@ApplicationContext private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bp_reminder_channel"
        const val CHANNEL_NAME = "BP Measurement Reminders"
        const val MORNING_REMINDER_ID = 2001
        const val EVENING_REMINDER_ID = 2002
        const val DOCTOR_REMINDER_ID = 2003
        const val STREAK_REMINDER_ID = 2004
        const val EXTRA_NOTIFICATION_TYPE = "bp_notification_type"
        const val EXTRA_MESSAGE = "bp_reminder_message"
        const val EXTRA_HOUR = "bp_reminder_hour"
        const val EXTRA_MINUTE = "bp_reminder_minute"
        const val TYPE_MORNING = "morning"
        const val TYPE_EVENING = "evening"
        const val TYPE_DOCTOR = "doctor"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to check your blood pressure"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleMorningReminder(hour: Int, minute: Int, message: String) {
        scheduleDaily(hour, minute, MORNING_REMINDER_ID, TYPE_MORNING, message)
    }

    fun scheduleEveningReminder(hour: Int, minute: Int, message: String) {
        scheduleDaily(hour, minute, EVENING_REMINDER_ID, TYPE_EVENING, message)
    }

    fun cancelMorningReminder() {
        cancelAlarm(MORNING_REMINDER_ID, TYPE_MORNING)
    }

    fun cancelEveningReminder() {
        cancelAlarm(EVENING_REMINDER_ID, TYPE_EVENING)
    }

    fun scheduleDoctorReminder(timestamp: Long, note: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BpReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_DOCTOR)
            putExtra(EXTRA_MESSAGE, note.ifEmpty { "Doctor appointment reminder for blood pressure follow-up" })
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DOCTOR_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (timestamp <= System.currentTimeMillis()) {
            // Also remove any previously scheduled appointment at this
            // request code when a user edits it to a past time.
            alarmManager.cancel(pendingIntent)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
        }
    }

    fun cancelDoctorReminder() {
        cancelAlarm(DOCTOR_REMINDER_ID, TYPE_DOCTOR)
    }

    internal fun scheduleDailyForReceiver(
        hour: Int,
        minute: Int,
        requestCode: Int,
        type: String,
        message: String
    ) = scheduleDaily(hour, minute, requestCode, type, message)

    private fun scheduleDaily(hour: Int, minute: Int, requestCode: Int, type: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BpReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // A repeating RTC alarm stores a fixed elapsed interval and can drift
        // when the timezone or DST offset changes. Recompute the next local
        // wall-clock occurrence every time instead.
        val triggerAtMillis = LocalReminderSchedulePolicy.nextOccurrenceMillis(
            hour = hour,
            minute = minute,
            now = Instant.now(),
            zone = ZoneId.systemDefault()
        )
        // Replace any repeating alarm left by a pre-upgrade build before
        // installing the one-shot schedule with the same PendingIntent.
        alarmManager.cancel(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(requestCode: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BpReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class BpReminderReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        val type = intent.getStringExtra(BpNotificationHelper.EXTRA_NOTIFICATION_TYPE) ?: return
        val message = intent.getStringExtra(BpNotificationHelper.EXTRA_MESSAGE)
            ?.trim()
            ?.take(160)
            ?.ifBlank { null }
            ?: "A blood-pressure check-in is scheduled."

        val (title, notificationId) = when (type) {
            BpNotificationHelper.TYPE_MORNING -> "Morning blood-pressure check-in" to BpNotificationHelper.MORNING_REMINDER_ID
            BpNotificationHelper.TYPE_EVENING -> "Evening blood-pressure check-in" to BpNotificationHelper.EVENING_REMINDER_ID
            BpNotificationHelper.TYPE_DOCTOR -> "Appointment reminder" to BpNotificationHelper.DOCTOR_REMINDER_ID
            else -> "BP Reminder" to BpNotificationHelper.STREAK_REMINDER_ID
        }

        // Intent to open app directly to BP calculator
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "blood_pressure")
        }

        val pendingOpenIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, BpNotificationHelper.CHANNEL_ID)
            .setSmallIcon(com.health.calculator.bmi.tracker.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingOpenIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        if (NotificationPermission.canPost(context)) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }

        // Re-arm only the daily reminders. The one-shot design means this
        // calculation uses the current timezone/DST rules after each delivery.
        if (type == BpNotificationHelper.TYPE_MORNING || type == BpNotificationHelper.TYPE_EVENING) {
            val hour = intent.getIntExtra(BpNotificationHelper.EXTRA_HOUR, -1)
            val minute = intent.getIntExtra(BpNotificationHelper.EXTRA_MINUTE, -1)
            if (hour in 0..23 && minute in 0..59) {
                BpNotificationHelper(context).scheduleDailyForReceiver(
                    hour = hour,
                    minute = minute,
                    requestCode = if (type == BpNotificationHelper.TYPE_MORNING) {
                        BpNotificationHelper.MORNING_REMINDER_ID
                    } else {
                        BpNotificationHelper.EVENING_REMINDER_ID
                    },
                    type = type,
                    message = message
                )
            }
        }
    }
}

/** Pure wall-clock policy shared by the scheduler and unit tests. */
internal object LocalReminderSchedulePolicy {
    fun nextOccurrenceMillis(
        hour: Int,
        minute: Int,
        now: Instant,
        zone: ZoneId
    ): Long {
        require(hour in 0..23) { "hour must be between 0 and 23" }
        require(minute in 0..59) { "minute must be between 0 and 59" }

        val localNow = now.atZone(zone)
        var target = localNow.toLocalDate().atTime(hour, minute).atZone(zone)
        if (!target.isAfter(localNow)) {
            target = localNow.toLocalDate().plusDays(1).atTime(hour, minute).atZone(zone)
        }
        return target.toInstant().toEpochMilli()
    }
}

