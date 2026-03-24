package com.health.calculator.bmi.tracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.model.ActivityLevel
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.HealthGoal
import com.health.calculator.bmi.tracker.data.model.FrameSize
import com.health.calculator.bmi.tracker.data.model.EthnicityRegion
import com.health.calculator.bmi.tracker.data.model.HeightUnit
import com.health.calculator.bmi.tracker.data.model.ProfileData
import com.health.calculator.bmi.tracker.data.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Singleton DataStore instance for the entire application.
 * Uses Preferences DataStore for key-value storage of profile data.
 */
private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "health_calculator_profile"
)

/**
 * Handles reading and writing user profile data to DataStore.
 * All profile fields are stored as individual preference keys so they
 * can be updated independently without serialization overhead.
 *
 * Data persists across app restarts and is stored in the app's
 * private storage directory.
 */
class ProfileDataStore(private val context: Context) {

    // ─── Preference Keys ──────────────────────────────────────────────────
    private companion object {
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_DATE_OF_BIRTH = longPreferencesKey("date_of_birth_millis")
        val KEY_GENDER = stringPreferencesKey("gender")
        val KEY_HEIGHT_CM = doublePreferencesKey("height_cm")
        val KEY_HEIGHT_UNIT = stringPreferencesKey("height_unit")
        val KEY_WEIGHT_KG = doublePreferencesKey("weight_kg")
        val KEY_WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val KEY_ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val KEY_GOAL_WEIGHT_KG = doublePreferencesKey("goal_weight_kg")
        val KEY_HEALTH_GOAL = stringPreferencesKey("health_goal") // Legacy, keeping for migration if needed
        val KEY_HEALTH_GOALS = stringPreferencesKey("health_goals")
        val KEY_PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
        val KEY_FRAME_SIZE = stringPreferencesKey("frame_size")
        val KEY_ETHNICITY_REGION = stringPreferencesKey("ethnicity_region")
        val KEY_LAST_UPDATED = longPreferencesKey("last_updated_millis")
    }

    // ─── Read Profile ─────────────────────────────────────────────────────

    /**
     * Returns a Flow of the complete ProfileData, automatically emitting
     * updates whenever any profile field changes in DataStore.
     */
    val profileFlow: Flow<ProfileData> = context.profileDataStore.data.map { preferences ->
        ProfileData(
            displayName = preferences[KEY_DISPLAY_NAME] ?: "",
            dateOfBirthMillis = preferences[KEY_DATE_OF_BIRTH],
            gender = preferences[KEY_GENDER]?.let {
                try { Gender.valueOf(it) } catch (_: Exception) { Gender.NOT_SET }
            } ?: Gender.NOT_SET,
            heightCm = preferences[KEY_HEIGHT_CM] ?: 0.0,
            heightUnit = preferences[KEY_HEIGHT_UNIT]?.let {
                try { HeightUnit.valueOf(it) } catch (_: Exception) { HeightUnit.CM }
            } ?: HeightUnit.CM,
            weightKg = preferences[KEY_WEIGHT_KG] ?: 0.0,
            weightUnit = preferences[KEY_WEIGHT_UNIT]?.let {
                try { WeightUnit.valueOf(it) } catch (_: Exception) { WeightUnit.KG }
            } ?: WeightUnit.KG,
            activityLevel = preferences[KEY_ACTIVITY_LEVEL]?.let {
                try { ActivityLevel.valueOf(it) } catch (_: Exception) { ActivityLevel.NOT_SET }
            } ?: ActivityLevel.NOT_SET,
            goalWeightKg = preferences[KEY_GOAL_WEIGHT_KG] ?: 0.0,
            healthGoals = preferences[KEY_HEALTH_GOALS]?.let { encoded ->
                encoded.split(",").mapNotNull { 
                    try { HealthGoal.valueOf(it) } catch (_: Exception) { null }
                }
            } ?: preferences[KEY_HEALTH_GOAL]?.let { 
                try { listOf(HealthGoal.valueOf(it)) } catch (_: Exception) { emptyList<HealthGoal>() }
            } ?: emptyList(),
            profilePictureUri = preferences[KEY_PROFILE_PICTURE_URI],
            frameSize = preferences[KEY_FRAME_SIZE]?.let {
                try { FrameSize.valueOf(it) } catch (_: Exception) { FrameSize.MEDIUM }
            } ?: FrameSize.MEDIUM,
            ethnicityRegion = preferences[KEY_ETHNICITY_REGION]?.let {
                try { EthnicityRegion.valueOf(it) } catch (_: Exception) { EthnicityRegion.GENERAL }
            } ?: EthnicityRegion.GENERAL,
            lastUpdatedMillis = preferences[KEY_LAST_UPDATED] ?: System.currentTimeMillis()
        )
    }

    // ─── Write Profile ────────────────────────────────────────────────────

    /**
     * Saves the complete profile data to DataStore.
     * This is an atomic operation — all fields are written together.
     */
    suspend fun saveProfile(profile: ProfileData) {
        context.profileDataStore.edit { preferences ->
            preferences[KEY_DISPLAY_NAME] = profile.displayName
            profile.dateOfBirthMillis?.let {
                preferences[KEY_DATE_OF_BIRTH] = it
            } ?: preferences.remove(KEY_DATE_OF_BIRTH)
            preferences[KEY_GENDER] = profile.gender.name
            preferences[KEY_HEIGHT_CM] = profile.heightCm
            preferences[KEY_HEIGHT_UNIT] = profile.heightUnit.name
            preferences[KEY_WEIGHT_KG] = profile.weightKg
            preferences[KEY_WEIGHT_UNIT] = profile.weightUnit.name
            preferences[KEY_ACTIVITY_LEVEL] = profile.activityLevel.name
            preferences[KEY_GOAL_WEIGHT_KG] = profile.goalWeightKg
            preferences[KEY_HEALTH_GOALS] = profile.healthGoals.joinToString(",") { it.name }
            preferences[KEY_PROFILE_PICTURE_URI] = profile.profilePictureUri ?: ""
            preferences[KEY_FRAME_SIZE] = profile.frameSize.name
            preferences[KEY_ETHNICITY_REGION] = profile.ethnicityRegion.name
            preferences[KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Clears all profile data from DataStore.
     */
    suspend fun clearProfile() {
        context.profileDataStore.edit { it.clear() }
    }
}
