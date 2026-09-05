// notification/WaterReminderScheduler.kt
package com.health.calculator.bmi.tracker.notification

import dagger.hilt.android.qualifiers.ApplicationContext

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.ReminderSchedulePolicy
import com.health.calculator.bmi.tracker.receiver.WaterReminderReceiver
import java.util.Calendar

class WaterReminderScheduler(@ApplicationContext private val context: Context) {

    companion object {
        private const val REQUEST_CODE = 6001
    }

    fun schedule(settings: WaterReminderSettings) {
        if (!settings.isEnabled) {
            cancel()
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            putExtra("frequency_minutes", settings.frequencyMinutes)
            putExtra("start_hour", settings.startHour)
            putExtra("start_minute", settings.startMinute)
            putExtra("end_hour", settings.endHour)
            putExtra("end_minute", settings.endMinute)
            putExtra("vibration", settings.enableVibration)
            putExtra("sound", settings.enableSound)
            putExtra("smart_skip", settings.smartSkipEnabled)
            putExtra("behind_nudge", settings.behindScheduleNudge)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next alarm time
        val nextAlarmTime = ReminderSchedulePolicy
            .nextWaterReminder(Calendar.getInstance(), settings)
            .timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextAlarmTime,
                pendingIntent
            )
        }
    }

    fun cancel() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

}

