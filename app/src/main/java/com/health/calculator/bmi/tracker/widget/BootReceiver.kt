package com.health.calculator.bmi.tracker.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Reschedule midnight reset alarm after reboot
            WaterIntakeWidget().scheduleMidnightReset(context)
            // Refresh all widgets
            WaterWidgetSyncManager.refreshAllWidgets(context)
        }
    }
}
