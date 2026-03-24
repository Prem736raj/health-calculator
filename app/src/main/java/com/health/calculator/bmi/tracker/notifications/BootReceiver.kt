package com.health.calculator.bmi.tracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Reschedule Re-engagement Alarms
            InactivityCheckScheduler(context).scheduleDaily()
            StreakProtectionScheduler(context).scheduleEvening()

            // Signal the app to reschedule reminders on next launch
            context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("needs_reschedule", true)
                .apply()
        }
    }
}
