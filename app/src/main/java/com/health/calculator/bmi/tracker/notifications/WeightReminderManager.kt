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
import java.util.Calendar

class WeightReminderReceiver : BroadcastReceiver() {
    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        if (!NotificationPermission.canPost(context)) return

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
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚖️ Weekly Weigh-in")
            .setContentText("Time for your weekly weigh-in! Track your progress consistently.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Time for your weekly weigh-in! Remember: same time, same conditions for best accuracy. " +
                                "Morning, after bathroom, before eating."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "weight_reminder_channel"
        const val NOTIFICATION_ID = 3001
    }
}

class WeightReminderManager @javax.inject.Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWeeklyReminder(dayOfWeek: Int, hour: Int, minute: Int) {
        val intent = Intent(context, WeightReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    fun cancelReminder() {
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

