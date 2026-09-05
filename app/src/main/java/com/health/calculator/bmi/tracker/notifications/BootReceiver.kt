package com.health.calculator.bmi.tracker.notifications

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
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
                    val appSettings = runCatching {
                        SettingsDataStore(context).settingsFlow.first()
                    }.getOrNull()

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

                    // Restore water intake reminders if configured and enabled.
                    // The Settings master switch is the source of truth for
                    // whether a child reminder may run in the background.
                    runCatching {
                        val waterPrefs = WaterReminderPreferences(context)
                        val waterSettings = waterPrefs.load()
                        if (appSettings?.remindersEnabled == true &&
                            appSettings.waterReminderEnabled &&
                            waterSettings.isEnabled
                        ) {
                            WaterReminderScheduler(context).schedule(waterSettings)
                        } else {
                            WaterReminderScheduler(context).cancel()
                        }
                    }

                    // BP reminders and the weekly weigh-in use separate
                    // preference stores. Restore them after reboot instead of
                    // leaving the user with a toggle that silently stopped.
                    runCatching {
                        val bpPreferences = com.health.calculator.bmi.tracker.data.preferences.BpReminderPreferences(context)
                        val bpSettings = bpPreferences.settingsFlow.first()
                        val bpNotifications = com.health.calculator.bmi.tracker.notification.BpNotificationHelper(context)
                        if (bpSettings.morningReminderEnabled) {
                            bpNotifications.scheduleMorningReminder(
                                bpSettings.morningReminderHour,
                                bpSettings.morningReminderMinute,
                                bpSettings.customReminderMessage
                            )
                        } else {
                            bpNotifications.cancelMorningReminder()
                        }
                        if (bpSettings.eveningReminderEnabled) {
                            bpNotifications.scheduleEveningReminder(
                                bpSettings.eveningReminderHour,
                                bpSettings.eveningReminderMinute,
                                bpSettings.customReminderMessage
                            )
                        } else {
                            bpNotifications.cancelEveningReminder()
                        }
                        if (bpSettings.doctorReminderEnabled &&
                            bpSettings.doctorReminderTimestamp > System.currentTimeMillis()
                        ) {
                            bpNotifications.scheduleDoctorReminder(
                                bpSettings.doctorReminderTimestamp,
                                bpSettings.doctorReminderNote
                            )
                        } else {
                            bpNotifications.cancelDoctorReminder()
                        }
                    }

                    runCatching {
                        val weightManager = WeightReminderManager(context)
                        if (appSettings?.remindersEnabled == true &&
                            appSettings.weightReminderEnabled
                        ) {
                            weightManager.restoreIfEnabled()
                        } else {
                            // Do not rewrite the child preference on boot. The
                            // master switch can pause delivery while keeping
                            // the user's selected weekly schedule for later.
                            weightManager.pauseReminder()
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
