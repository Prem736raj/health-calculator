package com.health.calculator.bmi.tracker.notifications

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import com.health.calculator.bmi.tracker.data.repository.ReminderRepository
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(@ApplicationContext context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val state = runCatching {
                        InactivityRepository(context).getInactivityState().first()
                    }.getOrNull()

                    // Restore only explicitly enabled re-engagement features.
                    if (state?.inactivityNotificationsEnabled == true) {
                        InactivityCheckScheduler(context).scheduleDaily()
                    } else {
                        InactivityCheckScheduler(context).cancel()
                    }
                    if (state?.streakProtectionEnabled == true) {
                        StreakProtectionScheduler(context).scheduleEvening()
                    } else {
                        StreakProtectionScheduler(context).cancel()
                    }

                    // Restore active general health reminders (BP, Weight, Custom)
                    runCatching {
                        val db = AppDatabase.getDatabase(context)
                        val reminderRepo = ReminderRepository(db.reminderDao(), context)
                        val activeReminders = reminderRepo.getActiveReminders().first()
                        val scheduler = ReminderScheduler(context)
                        activeReminders.forEach { reminder ->
                            if (reminder.isEnabled) {
                                scheduler.scheduleReminder(reminder)
                            }
                        }
                    }

                    // Restore water intake reminders if configured and enabled
                    runCatching {
                        val waterPrefs = WaterReminderPreferences(context)
                        val waterSettings = waterPrefs.load()
                        if (waterSettings.isEnabled) {
                            WaterReminderScheduler(context).schedule(waterSettings)
                        }
                    }

                    context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("needs_reschedule", false)
                        .apply()
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
