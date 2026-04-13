package com.health.calculator.bmi.tracker.widget.core

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Tracks the state of each widget instance:
 * HEALTHY, EMPTY, STALE, SETUP_REQUIRED, ERROR
 */
object WidgetStateManager {

    private const val PREFS_NAME          = "widget_state_prefs"
    private const val STALE_THRESHOLD_MS  = 24 * 60 * 60 * 1000L  // data older than 24h = stale
    private const val KEY_LAST_DATA_TIME  = "last_data_time_"
    private const val KEY_HAS_EVER_SETUP  = "has_setup_"
    private const val KEY_APP_CLEARED     = "app_cleared_"
    private const val KEY_WIDGET_VERSION  = "widget_version_"

    private const val CURRENT_VERSION = 3 // bump when data schema changes

    enum class WidgetState {
        HEALTHY,         // Has fresh data, showing normally
        EMPTY,           // No data yet — user hasn't used the calculator
        STALE,           // Data is old — show "Tap to refresh"
        SETUP_REQUIRED,  // App was cleared or first run — needs setup
        ERROR            // Something went wrong
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Mark widget data as updated ───────────────────────────────────

    fun markDataUpdated(context: Context, widgetId: Int) {
        prefs(context).edit().apply {
            putLong(KEY_LAST_DATA_TIME + widgetId, System.currentTimeMillis())
            putBoolean(KEY_HAS_EVER_SETUP + widgetId, true)
            putBoolean(KEY_APP_CLEARED + widgetId, false)
            putInt(KEY_WIDGET_VERSION + widgetId, CURRENT_VERSION)
            apply()
        }
    }

    fun markSetupRequired(context: Context, widgetId: Int) {
        prefs(context).edit()
            .putBoolean(KEY_APP_CLEARED + widgetId, true)
            .apply()
    }

    fun clearWidgetState(context: Context, widgetId: Int) {
        prefs(context).edit().apply {
            remove(KEY_LAST_DATA_TIME + widgetId)
            remove(KEY_HAS_EVER_SETUP + widgetId)
            remove(KEY_APP_CLEARED + widgetId)
            remove(KEY_WIDGET_VERSION + widgetId)
            apply()
        }
    }

    // ── Evaluate current state ────────────────────────────────────────

    fun getState(context: Context, widgetId: Int, hasData: Boolean): WidgetState {
        val p = prefs(context)

        // Check schema version
        val version = p.getInt(KEY_WIDGET_VERSION + widgetId, 0)
        if (version < CURRENT_VERSION && p.getBoolean(KEY_HAS_EVER_SETUP + widgetId, false)) {
            // Migrate: mark as needing refresh but not fully broken
            markDataUpdated(context, widgetId)
        }

        // App was cleared
        if (p.getBoolean(KEY_APP_CLEARED + widgetId, false)) {
            return WidgetState.SETUP_REQUIRED
        }

        // Never set up
        if (!p.getBoolean(KEY_HAS_EVER_SETUP + widgetId, false)) {
            return if (hasData) WidgetState.HEALTHY else WidgetState.EMPTY
        }

        // No data
        if (!hasData) return WidgetState.EMPTY

        // Check staleness
        val lastUpdate = p.getLong(KEY_LAST_DATA_TIME + widgetId, 0L)
        val age        = System.currentTimeMillis() - lastUpdate
        if (age > STALE_THRESHOLD_MS) return WidgetState.STALE

        return WidgetState.HEALTHY
    }

    // ── Convenience: check all widgets after app-clear ────────────────

    fun onAppDataCleared(context: Context) {
        // Called when user clears app data from Settings
        // We detect this by checking if our main data prefs are gone
        val healthPrefs = context.getSharedPreferences(
            "health_summary_widget_prefs", Context.MODE_PRIVATE
        )
        if (!healthPrefs.contains("bmi_value")) {
            // App data was cleared — all widgets need setup
            val statePrefs = prefs(context)
            val allKeys    = statePrefs.all.keys
                .filter { it.startsWith(KEY_HAS_EVER_SETUP) }
            allKeys.forEach { key ->
                val id = key.removePrefix(KEY_HAS_EVER_SETUP).toIntOrNull() ?: return@forEach
                markSetupRequired(context, id)
            }
        }
    }

    // ── Format "time ago" for stale message ──────────────────────────

    fun getLastUpdatedText(context: Context, widgetId: Int): String {
        val lastUpdate = prefs(context).getLong(KEY_LAST_DATA_TIME + widgetId, 0L)
        if (lastUpdate == 0L) return "Never updated"

        val diffMs  = System.currentTimeMillis() - lastUpdate
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours   = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days    = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            minutes < 2  -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24   -> "$hours hr ago"
            else         -> "$days day${if (days > 1) "s" else ""} ago"
        }
    }
}
