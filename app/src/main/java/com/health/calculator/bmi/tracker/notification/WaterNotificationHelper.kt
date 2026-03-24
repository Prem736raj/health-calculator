// notification/WaterNotificationHelper.kt
package com.health.calculator.bmi.tracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.receiver.WaterQuickLogReceiver

class WaterNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val CHANNEL_NAME = "Water Reminders"
        const val NOTIFICATION_ID = 5001
        const val QUICK_LOG_ACTION = "com.health.calculator.bmi.tracker.QUICK_LOG_WATER"
        const val QUICK_LOG_AMOUNT = 250
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to stay hydrated throughout the day"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        currentMl: Int,
        goalMl: Int,
        enableVibration: Boolean,
        enableSound: Boolean,
        isBehindSchedule: Boolean
    ) {
        createNotificationChannel()

        val percentage = if (goalMl > 0) ((currentMl.toFloat() / goalMl) * 100).toInt() else 0
        val remainingMl = (goalMl - currentMl).coerceAtLeast(0)
        val remainingL = remainingMl / 1000f

        // Choose notification text
        val (title, body) = when {
            isBehindSchedule -> Pair(
                "⚠️ You're behind on hydration!",
                "You've had ${currentMl}ml ($percentage%). Try to catch up — you need ${String.format("%.1f", remainingL)}L more today."
            )
            percentage >= 90 -> Pair(
                "💧 Almost there!",
                "You're at $percentage% of today's goal. Just ${remainingMl}ml to go!"
            )
            percentage >= 50 -> Pair(
                "💧 Time for a glass of water!",
                "You're at $percentage% of today's goal. Keep it up!"
            )
            percentage >= 25 -> Pair(
                "💧 Stay hydrated!",
                "You've had ${currentMl}ml so far. You need ${String.format("%.1f", remainingL)}L more today."
            )
            else -> Pair(
                "💧 Don't forget to drink water!",
                "You're only at $percentage% of today's goal. Time to hydrate!"
            )
        }

        // Open app intent
        val openIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("navigate_to", "water_tracking")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick log action intent
        val quickLogIntent = Intent(context, WaterQuickLogReceiver::class.java).apply {
            action = QUICK_LOG_ACTION
            putExtra("amount_ml", QUICK_LOG_AMOUNT)
        }
        val quickLogPendingIntent = PendingIntent.getBroadcast(
            context, 1, quickLogIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with actual water icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground, // Replace with water icon
                "Log ${QUICK_LOG_AMOUNT}ml 💧",
                quickLogPendingIntent
            )
            .setProgress(goalMl, currentMl.coerceAtMost(goalMl), false)

        if (!enableSound) {
            builder.setSilent(true)
        }

        if (!enableVibration) {
            builder.setVibrate(longArrayOf(0))
        }

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun cancelReminder() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
