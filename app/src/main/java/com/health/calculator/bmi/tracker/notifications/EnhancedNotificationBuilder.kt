// notifications/EnhancedNotificationBuilder.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.data.models.Reminder
import com.health.calculator.bmi.tracker.data.models.ReminderCategory

class EnhancedNotificationBuilder(private val context: Context) {

    fun buildNotification(
        reminder: Reminder,
        content: SmartNotificationContent
    ): android.app.Notification {
        val channelId = NotificationChannelsManager.getChannelIdForCategory(reminder.category)
        
        // Main tap intent (Deep Link)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("healthapp://navigate/${reminder.navigateRoute ?: "reminders"}")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText))
            .setSubText(content.subText)
            .setPriority(if (reminder.isHighPriority) NotificationCompat.PRIORITY_HIGH else content.priority)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Add Vibration
        if (reminder.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 100, 250))
        }

        // Add Sound
        if (reminder.soundUri != null) {
            builder.setSound(Uri.parse(reminder.soundUri))
        }

        // Add Action Buttons based on category
        addActions(builder, reminder)

        return builder.build()
    }

    private fun addActions(builder: NotificationCompat.Builder, reminder: Reminder) {
        val category = try {
            ReminderCategory.valueOf(reminder.category)
        } catch (e: Exception) {
            ReminderCategory.CUSTOM
        }

        // Standard Snoop Action for most reminders
        if (category != ReminderCategory.MEDICATION) {
            builder.addAction(
                0, // Icon (optional)
                "Snooze 15m",
                getSnoozePendingIntent(reminder, 15)
            )
        }

        when (category) {
            ReminderCategory.WATER_INTAKE -> {
                builder.addAction(
                    0, "Log 250ml",
                    getQuickActionPendingIntent(reminder, "LOG_WATER", "250")
                )
                builder.addAction(
                    0, "Log 500ml",
                    getQuickActionPendingIntent(reminder, "LOG_WATER", "500")
                )
            }
            ReminderCategory.CALORIE_LOGGING -> {
                builder.addAction(
                    0, "Log Meal",
                    getQuickActionPendingIntent(reminder, "LOG_MEAL", "")
                )
            }
            ReminderCategory.BLOOD_PRESSURE, ReminderCategory.WEIGHT_CHECK -> {
                builder.addAction(
                    0, "Open Tracker",
                    getMainPendingIntent(reminder)
                )
            }
            ReminderCategory.MEDICATION -> {
                builder.addAction(
                    0, "Taken",
                    getQuickActionPendingIntent(reminder, "MED_TAKEN", "")
                )
                builder.addAction(
                    0, "Skip",
                    getQuickActionPendingIntent(reminder, "MED_SKIP", "")
                )
            }
            else -> {}
        }
    }

    private fun getSnoozePendingIntent(reminder: Reminder, minutes: Int): PendingIntent {
        val intent = Intent(context, SnoozedReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("snooze_minutes", minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getQuickActionPendingIntent(reminder: Reminder, action: String, value: String): PendingIntent {
        val intent = Intent(context, QuickActionReceiver::class.java).apply {
            setAction(action)
            putExtra("reminder_id", reminder.id)
            putExtra("action_value", value)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode() + action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getMainPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("healthapp://navigate/${reminder.navigateRoute ?: "reminders"}")
        }
        return PendingIntent.getActivity(
            context,
            reminder.id.hashCode() + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
