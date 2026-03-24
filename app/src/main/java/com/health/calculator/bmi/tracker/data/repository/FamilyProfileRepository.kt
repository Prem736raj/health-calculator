package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.dao.FamilyProfileDao
import com.health.calculator.bmi.tracker.data.model.FamilyProfile
import com.health.calculator.bmi.tracker.data.model.ProfileColor
import com.health.calculator.bmi.tracker.data.model.ProfileData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository for managing family profiles.
 * Provides a clean API for profile CRUD, switching, and migration.
 */
class FamilyProfileRepository(
    private val familyProfileDao: FamilyProfileDao,
    private val oldProfileRepository: ProfileRepository // For migration from DataStore
) {

    /**
     * Observable stream of all family profiles.
     */
    val allProfiles: Flow<List<FamilyProfile>> = familyProfileDao.getAllProfiles()

    /**
     * Observable stream of the currently active profile.
     */
    val activeProfile: Flow<FamilyProfile?> = familyProfileDao.getActiveProfile()

    /**
     * Observable stream of the currently active profile converted to ProfileData.
     * This allows legacy components to continue working with the active profile.
     */
    val activeProfileData: Flow<ProfileData?> = activeProfile.map { it?.toProfileData() }

    fun getProfileCount(): Flow<Int> = familyProfileDao.getProfileCount()

    /**
     * Creates a new profile. Automatically assigns a unique color and sets active if it's the first.
     */
    suspend fun createProfile(profile: FamilyProfile): Boolean {
        val count = familyProfileDao.getProfileCount().first()
        if (count >= FamilyProfile.MAX_PROFILES) return false

        val usedColors = familyProfileDao.getUsedColors()
        val assignedColor = if (profile.profileColor == ProfileColor.BLUE.colorValue
            && ProfileColor.BLUE.colorValue in usedColors
        ) {
            ProfileColor.getNextAvailable(usedColors).colorValue
        } else {
            profile.profileColor
        }

        val isFirst = count == 0
        familyProfileDao.insertProfile(
            profile.copy(
                profileColor = assignedColor,
                isActive = isFirst,
                sortOrder = count
            )
        )
        return true
    }

    /**
     * Switches the active profile to the one with the given ID.
     */
    suspend fun switchProfile(profileId: String) {
        familyProfileDao.deactivateAll()
        familyProfileDao.activateProfile(profileId)
    }

    /**
     * Updates an existing profile's data.
     */
    suspend fun updateProfile(profile: FamilyProfile) {
        familyProfileDao.updateProfile(profile)
    }

    /**
     * Deletes a profile. If it was active, another profile is automatically activated.
     */
    suspend fun deleteProfile(profileId: String) {
        val profile = familyProfileDao.getProfileById(profileId) ?: return
        familyProfileDao.deleteProfile(profile)

        // If the deleted profile was active, activate the next available one
        if (profile.isActive) {
            val remaining = familyProfileDao.getAllProfiles().first()
            remaining.firstOrNull()?.let {
                familyProfileDao.activateProfile(it.profileId)
            }
        }
    }

    suspend fun canAddProfile(): Boolean {
        val count = familyProfileDao.getProfileCount().first()
        return count < FamilyProfile.MAX_PROFILES
    }

    suspend fun getNextAvailableColor(): ProfileColor {
        val used = familyProfileDao.getUsedColors()
        return ProfileColor.getNextAvailable(used)
    }

    /**
     * Migrates the existing profile from DataStore to Room if no profiles exist yet.
     */
    suspend fun migrateFromDataStore() {
        val count = familyProfileDao.getProfileCount().first()
        if (count > 0) return // Already migrated or profiles exist

        val oldProfile = oldProfileRepository.profileFlow.first()
        
        // Only migrate if the old profile had some data (e.g., height or weight set)
        if (oldProfile.heightCm > 0 || oldProfile.weightKg > 0 || oldProfile.displayName.isNotBlank()) {
            val family = FamilyProfile.fromProfileData(
                profile = oldProfile,
                color = ProfileColor.BLUE.colorValue,
                isActive = true,
                sortOrder = 0
            )

            familyProfileDao.insertProfile(family)
            
            // Optionally clear old repository to prevent re-migration attempts
            // oldProfileRepository.clearProfile() 
        }
    }
}
