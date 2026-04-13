package com.health.calculator.bmi.tracker.widget.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.health.calculator.bmi.tracker.widget.*
import java.util.Calendar

/**
 * Manages all widget alarm scheduling:
 * - 30-min refresh cycles
 * - Midnight reset
 * - Boot recovery
 * - Data-change triggered updates
 */
object WidgetUpdateScheduler {

    private const val TAG = "WidgetScheduler"

    // Request codes
    private const val RC_WATER_MIDNIGHT   = 7001
    private const val RC_WIDGET_REFRESH   = 7002
    private const val RC_DAILY_REFRESH    = 7003

    // Actions
    const val ACTION_SCHEDULED_REFRESH =
        "com.health.calculator.bmi.tracker.SCHEDULED_WIDGET_REFRESH"
    const val ACTION_MIDNIGHT_RESET    =
        "com.health.calculator.bmi.tracker.MIDNIGHT_WIDGET_RESET"

    // ── Schedule all widget alarms ────────────────────────────────────

    fun scheduleAll(context: Context) {
        scheduleMidnightReset(context)
        scheduleDailyRefresh(context)
        Log.d(TAG, "All widget alarms scheduled")
    }

    // ── 30-min refresh ────────────────────────────────────────────────

    fun scheduleRefresh(context: Context, delayMs: Long = 30 * 60 * 1000L) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WidgetDataChangeReceiver::class.java).apply {
            action = ACTION_SCHEDULED_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, RC_WIDGET_REFRESH, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + delayMs

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    // ── Midnight reset ────────────────────────────────────────────────

    fun scheduleMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 10)
            set(Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, WidgetDataChangeReceiver::class.java).apply {
            action = ACTION_MIDNIGHT_RESET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, RC_WATER_MIDNIGHT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent
                )
            }
            Log.d(TAG, "Midnight reset scheduled for ${midnight.time}")
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pendingIntent)
        }
    }

    // ── Daily full refresh at 6 AM ─────────────────────────────────

    private fun scheduleDailyRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val sixAM = Calendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= 6) add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, WidgetDataChangeReceiver::class.java).apply {
            action = ACTION_SCHEDULED_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, RC_DAILY_REFRESH, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                sixAM.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule daily refresh", e)
        }
    }

    // ── Cancel all alarms ─────────────────────────────────────────────

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent       = Intent(context, WidgetDataChangeReceiver::class.java)

        listOf(RC_WATER_MIDNIGHT, RC_WIDGET_REFRESH, RC_DAILY_REFRESH).forEach { rc ->
            val pi = PendingIntent.getBroadcast(
                context, rc, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
        }
        Log.d(TAG, "All widget alarms cancelled")
    }

    // ── Force immediate refresh of all widget families ────────────────

    fun forceRefreshAll(context: Context) {
        WidgetPerformanceManager.runOnWidgetThread {
            val manager = AppWidgetManager.getInstance(context)

            // Water widgets
            refreshFamily(context, manager, WaterIntakeSmallWidget::class.java) { id ->
                try {
                    val updateSmallMethod = Class.forName("com.health.calculator.bmi.tracker.widget.WaterIntakeWidget")
                        .getMethod("updateSmallWidget", Context::class.java, AppWidgetManager::class.java, Int::class.java)
                    updateSmallMethod.invoke(null, context, manager, id)
                } catch(e: Exception) {
                    WaterIntakeSmallWidget.updateWidget(context, manager, id)
                }
            }
            refreshFamily(context, manager, WaterIntakeMediumWidget::class.java) { id ->
                try {
                    val updateMedMethod = Class.forName("com.health.calculator.bmi.tracker.widget.WaterIntakeWidget")
                        .getMethod("updateMediumWidget", Context::class.java, AppWidgetManager::class.java, Int::class.java)
                    updateMedMethod.invoke(null, context, manager, id)
                } catch(e: Exception) {
                    WaterIntakeMediumWidget.updateWidget(context, manager, id)
                }
            }

            // Health summary widgets
            refreshFamily(context, manager, HealthSummaryMediumWidget::class.java) { id ->
                HealthSummaryMediumWidget.updateWidget(context, manager, id)
            }
            refreshFamily(context, manager, HealthSummaryLargeWidget::class.java) { id ->
                HealthSummaryLargeWidget.updateWidget(context, manager, id)
            }

            // Quick calc widgets
            refreshFamily(context, manager, QuickCalculateWidget::class.java) { id ->
                QuickCalculateWidget.updateWidget(context, manager, id)
            }
            refreshFamily(context, manager, SingleCalculatorWidget::class.java) { id ->
                SingleCalculatorWidget.updateWidget(context, manager, id)
            }
            refreshFamily(context, manager, StreakWidget::class.java) { id ->
                StreakWidget.updateWidget(context, manager, id)
            }
        }
    }

    private fun <T : android.appwidget.AppWidgetProvider> refreshFamily(
        context: Context,
        manager: AppWidgetManager,
        clazz: Class<T>,
        updater: (Int) -> Unit
    ) {
        try {
            val component = ComponentName(context, clazz)
            manager.getAppWidgetIds(component).forEach { id ->
                // Do not record update here, the updater should handle it or it's implicitly handled.
                updater(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh ${clazz.simpleName}", e)
        }
    }
}
