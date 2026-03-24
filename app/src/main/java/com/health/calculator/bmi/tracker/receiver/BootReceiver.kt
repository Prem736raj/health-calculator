// receiver/BootReceiver.kt
package com.health.calculator.bmi.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = WaterReminderPreferences(context)
            val settings = prefs.load()

            if (settings.isEnabled) {
                WaterReminderScheduler(context).schedule(settings)
            }
        }
    }
}
