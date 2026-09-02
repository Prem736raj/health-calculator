package com.health.calculator.bmi.tracker.data.model

import com.health.calculator.bmi.tracker.data.export.ExportDisclosurePolicy

/**
 * Privacy configuration for sharing health summaries.
 * Allows users to toggle specific data points before sharing.
 */
data class ProfileShareConfig(
    val includeName: Boolean = true,
    val includeAge: Boolean = true,
    val includeGender: Boolean = false,
    val includeBmi: Boolean = true,
    val includeBp: Boolean = true,
    val includeWhr: Boolean = false,
    val includeBmr: Boolean = false,
    val includeWaterStreak: Boolean = true,
    val includeHealthScore: Boolean = true,
    val includeWeight: Boolean = false,
    val includeCalories: Boolean = false
)

/**
 * Generates a formatted plain-text summary of health data based on the provided configuration.
 */
fun generateShareText(
    profile: FamilyProfile,
    overview: HealthOverview,
    config: ProfileShareConfig
): String {
    val parts = mutableListOf<String>()
    parts.add("📊 My Health Summary")
    parts.add("━━━━━━━━━━━━━━━━━━")

    if (config.includeName && profile.displayName.isNotBlank()) {
        parts.add("👤 ${profile.displayName}")
    }
    if (config.includeAge && profile.age != null) {
        parts.add("🎂 ${profile.age} years old")
    }
    if (config.includeGender && profile.gender != Gender.NOT_SET) {
        parts.add("⚧ ${profile.gender.displayName}")
    }
    if (config.includeHealthScore && overview.healthScore >= 0) {
        val label = when {
            overview.healthScore >= 80 -> "More complete logging"
            overview.healthScore >= 60 -> "Regular logging"
            overview.healthScore >= 40 -> "Some logging"
            else -> "Start with a few logs"
        }
        parts.add("🏆 Wellness Score: ${overview.healthScore}/100 (logging consistency: $label)")
    }

    parts.add("")

    if (config.includeBmi && overview.latestBmi != null) {
        parts.add("⚖️ BMI: ${overview.latestBmi.value} (${overview.latestBmi.category})")
    }
    if (config.includeBp && overview.latestBp != null) {
        parts.add("❤️ BP: ${overview.latestBp.value} (${overview.latestBp.category})")
    }
    if (config.includeWhr && overview.latestWhr != null) {
        parts.add("📏 WHR: ${overview.latestWhr.value} (${overview.latestWhr.category})")
    }
    if (config.includeBmr && overview.latestBmr != null) {
        parts.add("🔥 BMR: ${overview.latestBmr.value}")
    }
    if (config.includeWaterStreak && overview.waterStreak > 0) {
        parts.add("💧 Water Streak: ${overview.waterStreak} days")
    }

    parts.add("")
    parts.add(ExportDisclosurePolicy.shareFooter())

    return parts.joinToString("\n")
}
