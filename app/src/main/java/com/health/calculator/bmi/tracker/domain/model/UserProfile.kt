package com.health.calculator.bmi.tracker.domain.model

import com.health.calculator.bmi.tracker.data.model.ActivityLevel
import com.health.calculator.bmi.tracker.data.model.EthnicityRegion
import com.health.calculator.bmi.tracker.data.model.FrameSize
import com.health.calculator.bmi.tracker.data.model.HealthGoal

/**
 * Represents the user's health profile data.
 * Used across multiple calculators for personalized calculations.
 * Will be persisted via DataStore in a future prompt.
 */
data class UserProfile(
    val name: String = "",
    val profilePictureUri: String? = null,
    val dateOfBirthMillis: Long? = null,
    val gender: Gender = Gender.NOT_SPECIFIED,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val goalWeightKg: Float? = null,
    val activityLevel: ActivityLevel = ActivityLevel.NOT_SET,
    val healthGoals: List<HealthGoal> = emptyList(),
    val frameSize: FrameSize = FrameSize.MEDIUM,
    val ethnicityRegion: EthnicityRegion = EthnicityRegion.GENERAL,
    val useMetricSystem: Boolean = true,
    val profileSetupComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val age: Int?
        get() = com.health.calculator.bmi.tracker.data.model.calculateAge(dateOfBirthMillis)
}

/**
 * Biological sex/gender for medical calculations.
 * WHO guidelines use biological sex for metabolic formulas.
 */
enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    NOT_SPECIFIED("Not specified");

    companion object {
        fun fromString(value: String): Gender {
            return entries.find { it.name == value } ?: NOT_SPECIFIED
        }
    }
}
