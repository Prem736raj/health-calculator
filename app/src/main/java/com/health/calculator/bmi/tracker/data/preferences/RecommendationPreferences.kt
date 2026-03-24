package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val Context.recommendationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "recommendation_preferences"
)

class RecommendationPreferencesManager(
    private val context: Context
) {
    companion object {
        private val DISMISSED_RECOMMENDATIONS_KEY = stringPreferencesKey("dismissed_recommendations")
    }

    private val gson = Gson()

    /**
     * Get dismissed recommendations as map of id -> dismissedUntil timestamp
     */
    val dismissedRecommendations: Flow<Map<String, Long>> = context.recommendationDataStore.data
        .map { preferences ->
            val jsonString = preferences[DISMISSED_RECOMMENDATIONS_KEY] ?: "{}"
            try {
                val type = object : TypeToken<Map<String, Long>>() {}.type
                gson.fromJson<Map<String, Long>>(jsonString, type)
            } catch (e: Exception) {
                emptyMap()
            }
        }

    /**
     * Dismiss a recommendation (won't show again for 7 days)
     */
    suspend fun dismissRecommendation(id: String, dismissUntil: Long) {
        context.recommendationDataStore.edit { preferences ->
            val currentJson = preferences[DISMISSED_RECOMMENDATIONS_KEY] ?: "{}"
            val current = try {
                val type = object : TypeToken<Map<String, Long>>() {}.type
                gson.fromJson<Map<String, Long>>(currentJson, type).toMutableMap()
            } catch (e: Exception) {
                mutableMapOf()
            }
            
            current[id] = dismissUntil
            
            // Clean up expired dismissals
            val now = System.currentTimeMillis()
            val cleaned = current.filterValues { it > now }
            
            preferences[DISMISSED_RECOMMENDATIONS_KEY] = gson.toJson(cleaned)
        }
    }

    /**
     * Clear all dismissed recommendations
     */
    suspend fun clearAllDismissed() {
        context.recommendationDataStore.edit { preferences ->
            preferences[DISMISSED_RECOMMENDATIONS_KEY] = "{}"
        }
    }

    /**
     * Check if a recommendation is currently dismissed
     */
    suspend fun isRecommendationDismissed(id: String): Boolean {
        var dismissed = false
        context.recommendationDataStore.edit { preferences ->
            val jsonString = preferences[DISMISSED_RECOMMENDATIONS_KEY] ?: "{}"
            val map = try {
                val type = object : TypeToken<Map<String, Long>>() {}.type
                gson.fromJson<Map<String, Long>>(jsonString, type)
            } catch (e: Exception) {
                emptyMap()
            }
            val dismissedUntil = map[id]
            dismissed = dismissedUntil != null && dismissedUntil > System.currentTimeMillis()
        }
        return dismissed
    }
}
