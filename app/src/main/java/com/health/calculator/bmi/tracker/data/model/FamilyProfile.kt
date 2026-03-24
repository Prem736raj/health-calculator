package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a family member's health profile in the Room database.
 * This allows multiple users to track their health independently on the same device.
 */
@Entity(tableName = "family_profiles")
data class FamilyProfile(
    @PrimaryKey
    val profileId: String = UUID.randomUUID().toString(),
    val displayName: String = "",
    val profilePictureUri: String? = null,
    val dateOfBirthMillis: Long? = null,
    val genderName: String = Gender.NOT_SET.name,
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val goalWeightKg: Double = 0.0,
    val activityLevelName: String = ActivityLevel.NOT_SET.name,
    val healthGoalNames: String = "", // Comma-separated HealthGoal names
    val frameSizeName: String = FrameSize.MEDIUM.name,
    val ethnicityRegionName: String = EthnicityRegion.GENERAL.name,
    val useMetricUnits: Boolean = true,
    val profileColor: Int = ProfileColor.BLUE.colorValue,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val sortOrder: Int = 0
) {
    val age: Int?
        get() = calculateAge(dateOfBirthMillis)

    val gender: Gender
        get() = runCatching { Gender.valueOf(genderName) }.getOrDefault(Gender.NOT_SET)

    val activityLevel: ActivityLevel
        get() = runCatching { ActivityLevel.valueOf(activityLevelName) }.getOrDefault(ActivityLevel.NOT_SET)

    val frameSize: FrameSize
        get() = runCatching { FrameSize.valueOf(frameSizeName) }.getOrDefault(FrameSize.MEDIUM)

    val ethnicityRegion: EthnicityRegion
        get() = runCatching { EthnicityRegion.valueOf(ethnicityRegionName) }.getOrDefault(EthnicityRegion.GENERAL)

    val healthGoals: List<HealthGoal>
        get() = healthGoalNames.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { runCatching { HealthGoal.valueOf(it.trim()) }.getOrNull() }

    val initials: String
        get() = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

    /**
     * Converts this FamilyProfile to a ProfileData object for compatibility with
     * existing components that expect ProfileData.
     */
    fun toProfileData(): ProfileData {
        return ProfileData(
            displayName = displayName,
            profilePictureUri = profilePictureUri,
            dateOfBirthMillis = dateOfBirthMillis,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            goalWeightKg = goalWeightKg,
            activityLevel = activityLevel,
            healthGoals = healthGoals,
            frameSize = frameSize,
            ethnicityRegion = ethnicityRegion,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    companion object {
        const val MAX_PROFILES = 5

        /**
         * Creates a FamilyProfile from an existing ProfileData object.
         * Useful for migration and initial setup.
         */
        fun fromProfileData(
            profile: ProfileData,
            profileId: String = UUID.randomUUID().toString(),
            color: Int = ProfileColor.BLUE.colorValue,
            isActive: Boolean = false,
            sortOrder: Int = 0
        ): FamilyProfile {
            return FamilyProfile(
                profileId = profileId,
                displayName = profile.displayName,
                profilePictureUri = profile.profilePictureUri,
                dateOfBirthMillis = profile.dateOfBirthMillis,
                genderName = profile.gender.name,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                goalWeightKg = profile.goalWeightKg,
                activityLevelName = profile.activityLevel.name,
                healthGoalNames = profile.healthGoals.joinToString(",") { it.name },
                frameSizeName = profile.frameSize.name,
                ethnicityRegionName = profile.ethnicityRegion.name,
                profileColor = color,
                isActive = isActive,
                sortOrder = sortOrder
            )
        }
    }
}

/**
 * Predefined colors for profile avatars to visually distinguish between users.
 */
enum class ProfileColor(val colorValue: Int, val label: String) {
    BLUE(0xFF2196F3.toInt(), "Blue"),
    TEAL(0xFF009688.toInt(), "Teal"),
    PURPLE(0xFF9C27B0.toInt(), "Purple"),
    ORANGE(0xFFFF9800.toInt(), "Orange"),
    GREEN(0xFF4CAF50.toInt(), "Green");

    companion object {
        fun fromValue(value: Int): ProfileColor {
            return entries.find { it.colorValue == value } ?: BLUE
        }

        fun getNextAvailable(usedColors: List<Int>): ProfileColor {
            return entries.firstOrNull { it.colorValue !in usedColors } ?: BLUE
        }
    }
}
