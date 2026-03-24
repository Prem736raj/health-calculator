// data/preferences/PlantPreferences.kt
package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PlantPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE)

    var isPlantVisible: Boolean
        get() = prefs.getBoolean(KEY_VISIBLE, true)
        set(value) = prefs.edit().putBoolean(KEY_VISIBLE, value).apply()

    var totalDaysTracked: Int
        get() = prefs.getInt(KEY_TOTAL_DAYS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_DAYS, value).apply()

    var lastTrackedDate: String
        get() = prefs.getString(KEY_LAST_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_DATE, value).apply()

    var plantName: String
        get() = prefs.getString(KEY_PLANT_NAME, "Buddy") ?: "Buddy"
        set(value) = prefs.edit().putString(KEY_PLANT_NAME, value).apply()

    companion object {
        private const val KEY_VISIBLE = "plant_visible"
        private const val KEY_TOTAL_DAYS = "total_days_tracked"
        private const val KEY_LAST_DATE = "last_tracked_date"
        private const val KEY_PLANT_NAME = "plant_name"
    }
}
