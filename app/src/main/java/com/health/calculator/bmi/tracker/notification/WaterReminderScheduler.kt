// notification/WaterReminderScheduler.kt
package com.health.calculator.bmi.tracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.receiver.WaterReminderReceiver
import java.util.Calendar

class WaterReminderScheduler(private val context: Context) {

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
        val nextAlarmTime = calculateNextAlarmTime(settings)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm
            alarmManager.setAndAllowWhileIdle(
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

    private fun calculateNextAlarmTime(settings: WaterReminderSettings): Long {
        val now = Calendar.getInstance()
        val nextAlarm = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = settings.startHour * 60 + settings.startMinute
        val endMinutes = settings.endHour * 60 + settings.endMinute

        if (currentMinutes < startMinutes) {
            // Before start time — schedule at start
            nextAlarm.set(Calendar.HOUR_OF_DAY, settings.startHour)
            nextAlarm.set(Calendar.MINUTE, settings.startMinute)
        } else if (currentMinutes >= endMinutes) {
            // After end time — schedule at start tomorrow
            nextAlarm.add(Calendar.DAY_OF_YEAR, 1)
            nextAlarm.set(Calendar.HOUR_OF_DAY, settings.startHour)
            nextAlarm.set(Calendar.MINUTE, settings.startMinute)
        } else {
            // During active hours — schedule next interval
            val minutesSinceStart = currentMinutes - startMinutes
            val nextIntervalIndex = (minutesSinceStart / settings.frequencyMinutes) + 1
            val nextMinutes = startMinutes + (nextIntervalIndex * settings.frequencyMinutes)

            if (nextMinutes >= endMinutes) {
                // Next would be after end — schedule tomorrow
                nextAlarm.add(Calendar.DAY_OF_YEAR, 1)
                nextAlarm.set(Calendar.HOUR_OF_DAY, settings.startHour)
                nextAlarm.set(Calendar.MINUTE, settings.startMinute)
            } else {
                nextAlarm.set(Calendar.HOUR_OF_DAY, nextMinutes / 60)
                nextAlarm.set(Calendar.MINUTE, nextMinutes % 60)
            }
        }

        // Ensure it's in the future
        if (nextAlarm.timeInMillis <= now.timeInMillis) {
            nextAlarm.add(Calendar.MINUTE, settings.frequencyMinutes)
        }

        return nextAlarm.timeInMillis
    }
}
