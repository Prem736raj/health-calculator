package com.health.calculator.bmi.tracker.widget.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Handles device boot and package replacement.
 * Reschedules all alarms and refreshes all widgets.
 */
class WidgetBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetBoot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                Log.d(TAG, "Boot/update received: ${intent.action}")
                onDeviceBoot(context)
            }
        }
    }

    private fun onDeviceBoot(context: Context) {
        // Step 1: Reschedule all alarms
        WidgetUpdateScheduler.scheduleAll(context)

        // Step 2: Check if app data was cleared
        WidgetStateManager.onAppDataCleared(context)

        // Step 3: Refresh all widgets with persisted data
        WidgetPerformanceManager.runOnWidgetThread {
            try {
                WidgetUpdateScheduler.forceRefreshAll(context)
                Log.d(TAG, "All widgets refreshed after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Widget refresh after boot failed", e)
            }
        }
    }
}
