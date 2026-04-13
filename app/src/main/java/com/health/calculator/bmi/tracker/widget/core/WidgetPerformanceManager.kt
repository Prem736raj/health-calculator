package com.health.calculator.bmi.tracker.widget.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages widget update throttling and battery efficiency:
 * - Prevents redundant updates
 * - Throttles updates to max every 30 min (background)
 * - Immediate updates when triggered by user action
 * - Tracks update frequency for debugging
 */
object WidgetPerformanceManager {

    private const val TAG                = "WidgetPerf"
    private const val PREFS_NAME         = "widget_performance_prefs"
    private const val MIN_UPDATE_INTERVAL= 30 * 60 * 1000L  // 30 minutes
    private const val USER_ACTION_BYPASS = "user_action_bypass_"
    private const val LAST_UPDATE_KEY    = "last_update_"
    private const val UPDATE_COUNT_KEY   = "update_count_"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Throttle gate: true = allow update ───────────────────────────

    fun shouldUpdate(
        context: Context,
        widgetId: Int,
        isUserTriggered: Boolean = false
    ): Boolean {
        val p = prefs(context)

        // Always allow user-triggered updates (tap, refresh)
        if (isUserTriggered) {
            clearBypass(context, widgetId)
            return true
        }

        // Check bypass flag (set after user action)
        if (p.getBoolean(USER_ACTION_BYPASS + widgetId, false)) {
            clearBypass(context, widgetId)
            return true
        }

        // Throttle background updates
        val lastUpdate = p.getLong(LAST_UPDATE_KEY + widgetId, 0L)
        val elapsed    = System.currentTimeMillis() - lastUpdate

        return if (elapsed >= MIN_UPDATE_INTERVAL) {
            true
        } else {
            Log.d(TAG, "Widget $widgetId skipped update — ${elapsed / 1000}s since last (min: ${MIN_UPDATE_INTERVAL / 1000}s)")
            false
        }
    }

    fun recordUpdate(context: Context, widgetId: Int) {
        val p     = prefs(context)
        val count = p.getInt(UPDATE_COUNT_KEY + widgetId, 0)
        p.edit().apply {
            putLong(LAST_UPDATE_KEY + widgetId, System.currentTimeMillis())
            putInt(UPDATE_COUNT_KEY + widgetId, count + 1)
            apply()
        }
        Log.d(TAG, "Widget $widgetId updated (total: ${count + 1})")
    }

    fun markUserAction(context: Context, widgetId: Int) {
        prefs(context).edit()
            .putBoolean(USER_ACTION_BYPASS + widgetId, true)
            .apply()
    }

    private fun clearBypass(context: Context, widgetId: Int) {
        prefs(context).edit()
            .remove(USER_ACTION_BYPASS + widgetId)
            .apply()
    }

    // ── Efficient batch update: only update changed widgets ───────────

    fun getWidgetsThatNeedUpdate(
        context: Context,
        widgetIds: IntArray,
        isUserTriggered: Boolean
    ): IntArray {
        return widgetIds.filter { id ->
            shouldUpdate(context, id, isUserTriggered)
        }.toIntArray()
    }

    // ── Memory-efficient bitmap cache ─────────────────────────────────

    private val bitmapCache = androidx.collection.LruCache<String, android.graphics.Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    )

    fun getCachedBitmap(key: String): android.graphics.Bitmap? = bitmapCache.get(key)

    fun cacheBitmap(key: String, bitmap: android.graphics.Bitmap) {
        bitmapCache.put(key, bitmap)
    }

    fun clearBitmapCache() {
        bitmapCache.evictAll()
    }

    // ── Background thread executor for widget updates ─────────────────

    private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()

    fun runOnWidgetThread(block: () -> Unit) {
        executor.execute {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Widget update failed", e)
            }
        }
    }
}
