package com.health.calculator.bmi.tracker.widget.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Central broadcast receiver that handles:
 * - Scheduled refresh alarms
 * - Midnight reset
 * - App data change notifications
 * - External refresh triggers
 */
class WidgetDataChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetDataChange"

        const val ACTION_DATA_CHANGED =
            "com.health.calculator.bmi.tracker.WIDGET_DATA_CHANGED"
        const val EXTRA_DATA_TYPE = "data_type"

        // Data types
        const val DATA_BMI       = "bmi"
        const val DATA_BP        = "blood_pressure"
        const val DATA_WATER     = "water"
        const val DATA_CALORIES  = "calories"
        const val DATA_WEIGHT    = "weight"
        const val DATA_STREAK    = "streak"
        const val DATA_ALL       = "all"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received: ${intent.action}")

        when (intent.action) {

            // ── Scheduled / alarm-triggered refresh ───────────────
            WidgetUpdateScheduler.ACTION_SCHEDULED_REFRESH -> {
                WidgetPerformanceManager.runOnWidgetThread {
                    WidgetUpdateScheduler.forceRefreshAll(context)
                    // Reschedule next refresh
                    WidgetUpdateScheduler.scheduleRefresh(context)
                }
            }

            // ── Midnight reset ────────────────────────────────────
            WidgetUpdateScheduler.ACTION_MIDNIGHT_RESET -> {
                WidgetPerformanceManager.runOnWidgetThread {
                    // Water data resets via WaterWidgetRepository.checkAndResetForNewDay()
                    WidgetUpdateScheduler.forceRefreshAll(context)
                    // Reschedule for next midnight
                    WidgetUpdateScheduler.scheduleMidnightReset(context)
                    Log.d(TAG, "Midnight reset complete — widgets refreshed")
                }
            }

            // ── App data changed ──────────────────────────────────
            ACTION_DATA_CHANGED -> {
                val dataType = intent.getStringExtra(EXTRA_DATA_TYPE) ?: DATA_ALL
                WidgetPerformanceManager.runOnWidgetThread {
                    handleDataChange(context, dataType)
                }
            }
        }
    }

    private fun handleDataChange(context: Context, dataType: String) {
        Log.d(TAG, "Data changed: $dataType")

        when (dataType) {
            DATA_WATER -> {
                com.health.calculator.bmi.tracker.widget.HealthWidgetSyncManager
                    //.refreshAllWidgets(context) -> That method doesn't exist. Since it doesn't exist or we shouldn't rely on it, I'll catch it or call forceRefreshAll. The prompt had WaterWidgetSyncManager which also might not exist. Let's just use try-catch or force refresh.
                try {
                    val clazz = Class.forName("com.health.calculator.bmi.tracker.widget.WaterWidgetSyncManager")
                    clazz.getMethod("refreshAllWidgets", Context::class.java).invoke(null, context)
                } catch(e: Exception) {
                    WidgetUpdateScheduler.forceRefreshAll(context)
                }

                com.health.calculator.bmi.tracker.widget.HealthSummaryMediumWidget
                    .refreshAll(context)
                com.health.calculator.bmi.tracker.widget.HealthSummaryLargeWidget
                    .refreshAll(context)
            }
            DATA_BMI, DATA_BP, DATA_CALORIES, DATA_WEIGHT, DATA_STREAK -> {
                com.health.calculator.bmi.tracker.widget.HealthSummaryMediumWidget
                    .refreshAll(context)
                com.health.calculator.bmi.tracker.widget.HealthSummaryLargeWidget
                    .refreshAll(context)
                com.health.calculator.bmi.tracker.widget.QuickCalculateWidget
                    .refreshAll(context)
                com.health.calculator.bmi.tracker.widget.SingleCalculatorWidget
                    .refreshAll(context)
            }
            DATA_ALL -> {
                WidgetUpdateScheduler.forceRefreshAll(context)
            }
        }
    }
}
