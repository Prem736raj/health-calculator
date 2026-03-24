package com.health.calculator.bmi.tracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.health.calculator.bmi.tracker.data.models.Reminder
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancelReminder(reminder)
            return
        }

        val times = reminder.getTimesList()
        val days = reminder.getDaysList()

        times.forEachIndexed { timeIndex, timeStr ->
            val parts = timeStr.split(":")
            if (parts.size != 2) return@forEachIndexed
            val hour = parts[0].toIntOrNull() ?: return@forEachIndexed
            val minute = parts[1].toIntOrNull() ?: return@forEachIndexed

            days.forEach { dayOfWeek ->
                val requestCode = generateRequestCode(reminder.id, timeIndex, dayOfWeek)

                val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                    putExtra(EXTRA_REMINDER_ID, reminder.id)
                    putExtra(EXTRA_TITLE, reminder.title)
                    putExtra(EXTRA_MESSAGE, reminder.message)
                    putExtra(EXTRA_CATEGORY, reminder.category)
                    putExtra(EXTRA_NAVIGATE_ROUTE, reminder.navigateRoute)
                    putExtra(EXTRA_HIGH_PRIORITY, reminder.isHighPriority)
                    putExtra(EXTRA_VIBRATE, reminder.vibrationEnabled)
                    putExtra(EXTRA_SOUND_URI, reminder.soundUri)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = getNextTriggerTime(dayOfWeek, hour, minute)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelReminder(reminder: Reminder) {
        val times = reminder.getTimesList()
        val days = reminder.getDaysList()

        times.forEachIndexed { timeIndex, _ ->
            days.forEach { dayOfWeek ->
                val requestCode = generateRequestCode(reminder.id, timeIndex, dayOfWeek)

                val intent = Intent(context, ReminderBroadcastReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun rescheduleAllReminders(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.isEnabled) {
                scheduleReminder(reminder)
            } else {
                cancelReminder(reminder)
            }
        }
    }

    private fun getNextTriggerTime(dayOfWeek: Int, hour: Int, minute: Int): Calendar {
        // Map our 1=Mon..7=Sun to Calendar.MONDAY..SUNDAY
        val calendarDay = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calendarDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar
    }

    private fun generateRequestCode(reminderId: String, timeIndex: Int, dayOfWeek: Int): Int {
        return (reminderId.hashCode() + timeIndex * 10 + dayOfWeek) and 0x7FFFFFFF
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_MESSAGE = "reminder_message"
        const val EXTRA_CATEGORY = "reminder_category"
        const val EXTRA_NAVIGATE_ROUTE = "reminder_navigate_route"
        const val EXTRA_HIGH_PRIORITY = "reminder_high_priority"
        const val EXTRA_VIBRATE = "reminder_vibrate"
        const val EXTRA_SOUND_URI = "reminder_sound_uri"
    }

    fun scheduleSnooze(reminder: Reminder, minutes: Int) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_TITLE, reminder.title)
            putExtra(EXTRA_MESSAGE, reminder.message)
            putExtra(EXTRA_CATEGORY, reminder.category)
            putExtra(EXTRA_NAVIGATE_ROUTE, reminder.navigateRoute)
            putExtra(EXTRA_HIGH_PRIORITY, reminder.isHighPriority)
            putExtra(EXTRA_VIBRATE, reminder.vibrationEnabled)
            putExtra(EXTRA_SOUND_URI, reminder.soundUri)
            putExtra("is_snooze", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode() + 999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
