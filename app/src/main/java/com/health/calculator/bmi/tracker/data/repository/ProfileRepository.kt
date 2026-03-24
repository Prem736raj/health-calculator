package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.model.ProfileData
import com.health.calculator.bmi.tracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository layer for profile data access.
 * Acts as a clean abstraction between the ViewModel and the DataStore,
 * following Clean Architecture principles.
 */
class ProfileRepository(
    private val profileDataStore: ProfileDataStore
) {
    /**
     * Observable stream of the current profile data.
     * Emits the latest profile whenever it changes.
     */
    val profileFlow: Flow<ProfileData> = profileDataStore.profileFlow

    /**
     * Returns a Flow of the UserProfile domain model.
     */
    fun getProfile(): Flow<UserProfile> = profileDataStore.profileFlow.map { it.toDomain() }

    /**
     * Saves the given profile data persistently.
     */
    suspend fun saveProfile(userProfile: UserProfile) {
        profileDataStore.saveProfile(userProfile.toData())
    }

    // ─── Mapping Helpers ──────────────────────────────────────────────────

    private fun ProfileData.toDomain(): UserProfile {
        return UserProfile(
            name = displayName,
            profilePictureUri = profilePictureUri,
            dateOfBirthMillis = dateOfBirthMillis,
            gender = com.health.calculator.bmi.tracker.domain.model.Gender.fromString(gender.name),
            heightCm = heightCm.toFloat(),
            weightKg = weightKg.toFloat(),
            goalWeightKg = goalWeightKg.toFloat(),
            activityLevel = activityLevel,
            healthGoals = healthGoals,
            frameSize = frameSize,
            ethnicityRegion = ethnicityRegion,
            useMetricSystem = weightUnit == com.health.calculator.bmi.tracker.data.model.WeightUnit.KG,
            profileSetupComplete = heightCm > 0 && weightKg > 0,
            updatedAt = lastUpdatedMillis
        )
    }

    private fun UserProfile.toData(): ProfileData {
        return ProfileData(
            displayName = name,
            profilePictureUri = profilePictureUri,
            dateOfBirthMillis = dateOfBirthMillis,
            gender = try { com.health.calculator.bmi.tracker.data.model.Gender.valueOf(gender.name) } catch (_: Exception) { com.health.calculator.bmi.tracker.data.model.Gender.NOT_SET },
            heightCm = heightCm?.toDouble() ?: 0.0,
            weightKg = weightKg?.toDouble() ?: 0.0,
            goalWeightKg = goalWeightKg?.toDouble() ?: 0.0,
            activityLevel = activityLevel,
            healthGoals = healthGoals,
            frameSize = frameSize,
            ethnicityRegion = ethnicityRegion,
            weightUnit = if (useMetricSystem) com.health.calculator.bmi.tracker.data.model.WeightUnit.KG else com.health.calculator.bmi.tracker.data.model.WeightUnit.LBS,
            heightUnit = if (useMetricSystem) com.health.calculator.bmi.tracker.data.model.HeightUnit.CM else com.health.calculator.bmi.tracker.data.model.HeightUnit.FEET_INCHES,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    /**
     * Clears all stored profile data, resetting to defaults.
     */
    suspend fun clearProfile() {
        profileDataStore.clearProfile()
    }
}
