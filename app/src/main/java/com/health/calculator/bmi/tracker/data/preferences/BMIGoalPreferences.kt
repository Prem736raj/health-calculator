package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.model.BMIGoalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bmiGoalDataStore: DataStore<Preferences> by preferencesDataStore(name = "bmi_goal_prefs")

class BMIGoalPreferences(private val context: Context) {

    companion object {
        private val TARGET_BMI = floatPreferencesKey("target_bmi")
        private val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        private val IS_GOAL_SET = booleanPreferencesKey("is_goal_set")
        private val GOAL_SET_DATE = longPreferencesKey("goal_set_date")
        private val STARTING_BMI = floatPreferencesKey("starting_bmi")
        private val STARTING_WEIGHT = floatPreferencesKey("starting_weight")
        private val HEIGHT_CM = floatPreferencesKey("goal_height_cm")
    }

    val bmiGoalFlow: Flow<BMIGoalData> = context.bmiGoalDataStore.data.map { prefs ->
        BMIGoalData(
            targetBMI = prefs[TARGET_BMI] ?: BMIGoalData.NORMAL_BMI_MID,
            targetWeight = prefs[TARGET_WEIGHT] ?: 0f,
            isGoalSet = prefs[IS_GOAL_SET] ?: false,
            goalSetDateMillis = prefs[GOAL_SET_DATE] ?: System.currentTimeMillis(),
            startingBMI = prefs[STARTING_BMI] ?: 0f,
            startingWeight = prefs[STARTING_WEIGHT] ?: 0f,
            heightCm = prefs[HEIGHT_CM] ?: 0f
        )
    }

    suspend fun saveGoal(goalData: BMIGoalData) {
        context.bmiGoalDataStore.edit { prefs ->
            prefs[TARGET_BMI] = goalData.targetBMI
            prefs[TARGET_WEIGHT] = goalData.targetWeight
            prefs[IS_GOAL_SET] = true
            prefs[GOAL_SET_DATE] = goalData.goalSetDateMillis
            prefs[STARTING_BMI] = goalData.startingBMI
            prefs[STARTING_WEIGHT] = goalData.startingWeight
            prefs[HEIGHT_CM] = goalData.heightCm
        }
    }

    suspend fun clearGoal() {
        context.bmiGoalDataStore.edit { prefs ->
            prefs[IS_GOAL_SET] = false
        }
    }

    suspend fun updateGoalProgress(currentWeight: Float, currentBMI: Float) {
        context.bmiGoalDataStore.edit { prefs ->
            // We don't modify starting values, only current is tracked via the parameters
            // The ViewModel handles combining saved goal with current values
        }
    }
}
