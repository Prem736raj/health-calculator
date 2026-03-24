// File: com/health/calculator/bmi/tracker/data/preferences/BMRLastValuePreferences.kt
package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bmrLastValueStore: DataStore<Preferences> by preferencesDataStore(
    name = "bmr_last_value"
)

data class BMRLastValue(
    val bmr: Float = 0f,
    val tdee: Float = 0f,
    val formulaName: String = "",
    val timestamp: Long = 0L
) {
    val isValid: Boolean get() = bmr > 0 && timestamp > 0
    val summaryText: String
        get() = if (isValid) "Last: ${bmr.toInt()} kcal/day" else ""
}

class BMRLastValuePreferences(private val context: Context) {

    companion object {
        private val LAST_BMR = floatPreferencesKey("last_bmr")
        private val LAST_TDEE = floatPreferencesKey("last_tdee")
        private val LAST_FORMULA = stringPreferencesKey("last_formula")
        private val LAST_TIMESTAMP = longPreferencesKey("last_timestamp")
    }

    val lastValueFlow: Flow<BMRLastValue> = context.bmrLastValueStore.data.map { prefs ->
        BMRLastValue(
            bmr = prefs[LAST_BMR] ?: 0f,
            tdee = prefs[LAST_TDEE] ?: 0f,
            formulaName = prefs[LAST_FORMULA] ?: "",
            timestamp = prefs[LAST_TIMESTAMP] ?: 0L
        )
    }

    suspend fun saveLastValue(bmr: Float, tdee: Float, formulaName: String) {
        context.bmrLastValueStore.edit { prefs ->
            prefs[LAST_BMR] = bmr
            prefs[LAST_TDEE] = tdee
            prefs[LAST_FORMULA] = formulaName
            prefs[LAST_TIMESTAMP] = System.currentTimeMillis()
        }
    }
}
