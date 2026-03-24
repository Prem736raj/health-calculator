// notifications/NotificationStatistics.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class NotificationStatistics(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_stats",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SENT_COUNT = "sent_count"
        private const val KEY_TAP_COUNT = "tap_count"
        private const val KEY_ACTION_COUNT = "action_count"
        private const val KEY_CATEGORY_STATS = "category_stats"
    }

    fun recordSent(category: String) {
        increment(KEY_SENT_COUNT)
        incrementCategoryStat(category, "sent")
    }

    fun recordTap(category: String) {
        increment(KEY_TAP_COUNT)
        incrementCategoryStat(category, "tap")
    }

    fun recordAction(category: String, actionType: String) {
        increment(KEY_ACTION_COUNT)
        incrementCategoryStat(category, "action_$actionType")
    }

    fun getStats(): StatsSummary {
        val sent = prefs.getInt(KEY_SENT_COUNT, 0)
        val tapped = prefs.getInt(KEY_TAP_COUNT, 0)
        val actions = prefs.getInt(KEY_ACTION_COUNT, 0)
        val tapRate = if (sent > 0) (tapped.toFloat() / sent * 100) else 0f
        
        return StatsSummary(sent, tapped, actions, tapRate)
    }

    private fun increment(key: String) {
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private fun incrementCategoryStat(category: String, type: String) {
        val jsonStr = prefs.getString(KEY_CATEGORY_STATS, "{}")
        val json = JSONObject(jsonStr)
        
        val catJson = json.optJSONObject(category) ?: JSONObject()
        catJson.put(type, catJson.optInt(type, 0) + 1)
        json.put(category, catJson)
        
        prefs.edit().putString(KEY_CATEGORY_STATS, json.toString()).apply()
    }

    data class StatsSummary(
        val totalSent: Int,
        val totalTapped: Int,
        val totalActions: Int,
        val tapRate: Float
    )
}
