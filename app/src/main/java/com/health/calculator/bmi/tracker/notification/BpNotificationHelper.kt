// notification/BpNotificationHelper.kt
package com.health.calculator.bmi.tracker.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class BpNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "bp_reminder_channel"
        const val CHANNEL_NAME = "BP Measurement Reminders"
        const val MORNING_REMINDER_ID = 2001
        const val EVENING_REMINDER_ID = 2002
        const val DOCTOR_REMINDER_ID = 2003
        const val STREAK_REMINDER_ID = 2004
        const val EXTRA_NOTIFICATION_TYPE = "bp_notification_type"
        const val EXTRA_MESSAGE = "bp_reminder_message"
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
        // Note: Using setExactAndAllowWhileIdle for doctor reminders
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
        }
    }

    fun cancelDoctorReminder() {
        cancelAlarm(DOCTOR_REMINDER_ID, TYPE_DOCTOR)
    }

    private fun scheduleDaily(hour: Int, minute: Int, requestCode: Int, type: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BpReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
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

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(BpNotificationHelper.EXTRA_NOTIFICATION_TYPE) ?: return
        val message = intent.getStringExtra(BpNotificationHelper.EXTRA_MESSAGE)
            ?: "Time to check your blood pressure! 🩺"

        val (title, notificationId) = when (type) {
            BpNotificationHelper.TYPE_MORNING -> "🌅 Morning BP Check" to BpNotificationHelper.MORNING_REMINDER_ID
            BpNotificationHelper.TYPE_EVENING -> "🌆 Evening BP Check" to BpNotificationHelper.EVENING_REMINDER_ID
            BpNotificationHelper.TYPE_DOCTOR -> "🏥 Doctor Appointment" to BpNotificationHelper.DOCTOR_REMINDER_ID
            else -> "BP Reminder" to BpNotificationHelper.STREAK_REMINDER_ID
        }

        // Intent to open app directly to BP calculator
        val openIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
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
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingOpenIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}

class BpBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Note: In a real app, you'd trigger a WorkManager job here to re-schedule
            // based on DataStore settings. For simplicity in this prompt, it's a placeholder.
        }
    }
}
