// notifications/WeeklyReportScheduler.kt
package com.health.calculator.bmi.tracker.notifications

import android.app.AlarmManager
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

class WeeklyReportScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWeeklyReport(dayOfWeek: Int = Calendar.SUNDAY, hour: Int = 19, minute: Int = 0) {
        val intent = Intent(context, WeeklyReportReceiver::class.java)
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

        // Save preference
        context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("report_day", dayOfWeek)
            .putInt("report_hour", hour)
            .putInt("report_minute", minute)
            .putBoolean("report_enabled", true)
            .apply()
    }

    fun cancelWeeklyReport() {
        val intent = Intent(context, WeeklyReportReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("report_enabled", false)
            .apply()
    }

    fun isEnabled(): Boolean {
        return context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .getBoolean("report_enabled", false)
    }

    fun getScheduledDay(): Int {
        return context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .getInt("report_day", Calendar.SUNDAY)
    }

    fun getScheduledHour(): Int {
        return context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .getInt("report_hour", 19)
    }

    fun getScheduledMinute(): Int {
        return context.getSharedPreferences("weekly_report_prefs", Context.MODE_PRIVATE)
            .getInt("report_minute", 0)
    }

    companion object {
        const val REQUEST_CODE = 8000
    }
}

class WeeklyReportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            NotificationChannelsManager.createAllChannels(context)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_route", "weekly_report")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                WeeklyReportScheduler.REQUEST_CODE + 1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(
                context,
                NotificationChannelsManager.CHANNEL_WEEKLY_REPORTS
            )
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("\uD83D\uDCCA Your Weekly Health Report is Ready!")
                .setContentText("See how you did this week and plan for next week.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your weekly health summary has been generated. See your progress, trends, and personalized suggestions for next week!")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(0, "\uD83D\uDCCA View Report", pendingIntent)
                .build()

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as android.app.NotificationManager
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 8001
    }
}
