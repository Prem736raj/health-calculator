package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bmiInputMemoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bmi_input_memory"
)

data class BMILastUsedInput(
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val age: Int = 0,
    val isMale: Boolean = true,
    val wasUnitKg: Boolean = true,
    val wasUnitCm: Boolean = true,
    val timestamp: Long = 0L
) {
    val isValid: Boolean
        get() = weightKg > 0f && heightCm > 0f && age > 0 && timestamp > 0L

    val weightLbs: Float get() = weightKg * 2.20462f

    val heightFeet: Int get() = (heightCm / 2.54 / 12).toInt()
    val heightInches: Int get() = ((heightCm / 2.54) % 12).toInt()

    fun displayWeight(useKg: Boolean): String {
        return if (useKg) String.format("%.1f kg", weightKg)
        else String.format("%.1f lbs", weightLbs)
    }

    fun displayHeight(useCm: Boolean): String {
        return if (useCm) String.format("%.0f cm", heightCm)
        else "${heightFeet}ft ${heightInches}in"
    }
}

class BMIInputMemoryPreferences(private val context: Context) {

    companion object {
        private val LAST_WEIGHT_KG = floatPreferencesKey("last_weight_kg")
        private val LAST_HEIGHT_CM = floatPreferencesKey("last_height_cm")
        private val LAST_AGE = intPreferencesKey("last_age")
        private val LAST_IS_MALE = booleanPreferencesKey("last_is_male")
        private val LAST_WAS_UNIT_KG = booleanPreferencesKey("last_was_unit_kg")
        private val LAST_WAS_UNIT_CM = booleanPreferencesKey("last_was_unit_cm")
        private val LAST_TIMESTAMP = longPreferencesKey("last_timestamp")
    }

    val lastUsedInputFlow: Flow<BMILastUsedInput> = context.bmiInputMemoryDataStore.data.map { prefs ->
        BMILastUsedInput(
            weightKg = prefs[LAST_WEIGHT_KG] ?: 0f,
            heightCm = prefs[LAST_HEIGHT_CM] ?: 0f,
            age = prefs[LAST_AGE] ?: 0,
            isMale = prefs[LAST_IS_MALE] ?: true,
            wasUnitKg = prefs[LAST_WAS_UNIT_KG] ?: true,
            wasUnitCm = prefs[LAST_WAS_UNIT_CM] ?: true,
            timestamp = prefs[LAST_TIMESTAMP] ?: 0L
        )
    }

    suspend fun saveLastUsedInput(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        isMale: Boolean,
        wasUnitKg: Boolean,
        wasUnitCm: Boolean
    ) {
        context.bmiInputMemoryDataStore.edit { prefs ->
            prefs[LAST_WEIGHT_KG] = weightKg
            prefs[LAST_HEIGHT_CM] = heightCm
            prefs[LAST_AGE] = age
            prefs[LAST_IS_MALE] = isMale
            prefs[LAST_WAS_UNIT_KG] = wasUnitKg
            prefs[LAST_WAS_UNIT_CM] = wasUnitCm
            prefs[LAST_TIMESTAMP] = System.currentTimeMillis()
        }
    }
}
