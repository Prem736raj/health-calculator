package com.health.calculator.bmi.tracker.domain.usecases

import com.health.calculator.bmi.tracker.domain.model.UserProfile

data class ProfileCompletionResult(
    val percentage: Int,
    val completedFields: List<String>,
    val missingFields: List<String>,
    val suggestions: List<String>
)

class ProfileCompletionUseCase {

    fun calculate(profile: UserProfile): ProfileCompletionResult {
        val fields = mutableListOf<Pair<String, Boolean>>()

        fields.add("Display Name" to !profile.name.isBlank())
        fields.add("Profile Picture" to (profile.profilePictureUri != null && profile.profilePictureUri.isNotBlank()))
        fields.add("Date of Birth" to (profile.dateOfBirthMillis != null))
        fields.add("Gender" to (profile.gender != com.health.calculator.bmi.tracker.domain.model.Gender.NOT_SPECIFIED))
        fields.add("Height" to (profile.heightCm != null && profile.heightCm > 0))
        fields.add("Weight" to (profile.weightKg != null && profile.weightKg > 0))
        fields.add("Goal Weight" to (profile.goalWeightKg != null && profile.goalWeightKg > 0))
        fields.add("Activity Level" to (profile.activityLevel != com.health.calculator.bmi.tracker.data.model.ActivityLevel.NOT_SET))
        fields.add("Health Goals" to (profile.healthGoals.isNotEmpty()))
        fields.add("Frame Size" to true) // Assuming default Medium is a valid start, but maybe we want explicit?
        fields.add("Ethnicity/Region" to true) // Same here

        val completed = fields.filter { it.second }.map { it.first }
        val missing = fields.filter { !it.second }.map { it.first }
        val percentage = if (fields.isEmpty()) 0 else (completed.size * 100) / fields.size

        val suggestions = mutableListOf<String>()
        if ("Height" in missing || "Weight" in missing) {
            suggestions.add("Add height and weight for accurate BMI calculation")
        }
        if ("Date of Birth" in missing) {
            suggestions.add("Add your birthdate for age-adjusted health metrics")
        }
        if ("Activity Level" in missing) {
            suggestions.add("Set activity level for accurate calorie calculations")
        }
        if ("Health Goals" in missing) {
            suggestions.add("Select health goals to personalize your plan")
        }

        return ProfileCompletionResult(
            percentage = percentage,
            completedFields = completed,
            missingFields = missing,
            suggestions = suggestions
        )
    }
}
