package com.health.calculator.bmi.tracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_prefs")

class SearchPreferences(private val context: Context) {
    private object Keys {
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val WATER_LOG_COUNT = intPreferencesKey("water_log_count")
        val BP_CHECK_COUNT = intPreferencesKey("bp_check_count")
        val CALORIE_LOG_COUNT = intPreferencesKey("calorie_log_count")
        val BMI_CALC_COUNT = intPreferencesKey("bmi_calc_count")
        val BMR_CALC_COUNT = intPreferencesKey("bmr_calc_count")
        val WHR_CALC_COUNT = intPreferencesKey("whr_calc_count")
        val HR_CALC_COUNT = intPreferencesKey("hr_calc_count")
        val IBW_CALC_COUNT = intPreferencesKey("ibw_calc_count")
        val BSA_CALC_COUNT = intPreferencesKey("bsa_calc_count")
        val MET_CALC_COUNT = intPreferencesKey("met_calc_count")
        val LAST_USED_FEATURE = stringPreferencesKey("last_used_feature")
    }

    private val dataStore = context.searchDataStore

    val recentSearches: Flow<List<String>> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.RECENT_SEARCHES] ?: ""
        if (raw.isEmpty()) emptyList()
        else raw.split("|||").filter { it.isNotBlank() }
    }

    suspend fun addRecentSearch(query: String) {
        dataStore.edit { prefs ->
            val raw = prefs[Keys.RECENT_SEARCHES] ?: ""
            val existing = if (raw.isEmpty()) mutableListOf()
            else raw.split("|||").toMutableList()

            existing.remove(query)
            existing.add(0, query)

            val trimmed = existing.take(10)
            prefs[Keys.RECENT_SEARCHES] = trimmed.joinToString("|||")
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { it[Keys.RECENT_SEARCHES] = "" }
    }

    suspend fun removeRecentSearch(query: String) {
        dataStore.edit { prefs ->
            val raw = prefs[Keys.RECENT_SEARCHES] ?: ""
            val existing = raw.split("|||").toMutableList()
            existing.remove(query)
            prefs[Keys.RECENT_SEARCHES] = existing.joinToString("|||")
        }
    }

    fun getUsageCounts(): Flow<Map<String, Int>> = dataStore.data.map { prefs ->
        mapOf(
            "water_log" to (prefs[Keys.WATER_LOG_COUNT] ?: 0),
            "bp_check" to (prefs[Keys.BP_CHECK_COUNT] ?: 0),
            "calorie_log" to (prefs[Keys.CALORIE_LOG_COUNT] ?: 0),
            "bmi_calc" to (prefs[Keys.BMI_CALC_COUNT] ?: 0),
            "bmr_calc" to (prefs[Keys.BMR_CALC_COUNT] ?: 0),
            "whr_calc" to (prefs[Keys.WHR_CALC_COUNT] ?: 0),
            "hr_calc" to (prefs[Keys.HR_CALC_COUNT] ?: 0),
            "ibw_calc" to (prefs[Keys.IBW_CALC_COUNT] ?: 0),
            "bsa_calc" to (prefs[Keys.BSA_CALC_COUNT] ?: 0),
            "met_calc" to (prefs[Keys.MET_CALC_COUNT] ?: 0)
        )
    }

    suspend fun incrementUsage(featureKey: String) {
        dataStore.edit { prefs ->
            val key = when (featureKey) {
                "water_log" -> Keys.WATER_LOG_COUNT
                "bp_check" -> Keys.BP_CHECK_COUNT
                "calorie_log" -> Keys.CALORIE_LOG_COUNT
                "bmi_calc" -> Keys.BMI_CALC_COUNT
                "bmr_calc" -> Keys.BMR_CALC_COUNT
                "whr_calc" -> Keys.WHR_CALC_COUNT
                "hr_calc" -> Keys.HR_CALC_COUNT
                "ibw_calc" -> Keys.IBW_CALC_COUNT
                "bsa_calc" -> Keys.BSA_CALC_COUNT
                "met_calc" -> Keys.MET_CALC_COUNT
                else -> return@edit
            }
            prefs[key] = (prefs[key] ?: 0) + 1
            prefs[Keys.LAST_USED_FEATURE] = featureKey
        }
    }

    val lastUsedFeature: Flow<String?> = dataStore.data.map { it[Keys.LAST_USED_FEATURE] }
}
