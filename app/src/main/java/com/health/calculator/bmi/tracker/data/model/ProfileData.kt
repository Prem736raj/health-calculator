package com.health.calculator.bmi.tracker.data.model

/**
 * Represents the user's complete health profile data.
 * This data is shared across all calculators and stored locally using DataStore.
 *
 * All measurements are stored in metric units internally:
 * - Height in centimeters
 * - Weight in kilograms
 *
 * Display unit preferences are stored separately so the UI can show
 * the user's preferred units while calculations always use metric.
 */
data class ProfileData(
    /** User's display name (optional) */
    val displayName: String = "",

    /** Profile picture URI (string representation) */
    val profilePictureUri: String? = null,

    /** Date of birth stored as epoch milliseconds, null if not set */
    val dateOfBirthMillis: Long? = null,

    /** Biological gender for health calculations */
    val gender: Gender = Gender.NOT_SET,

    /** Height in centimeters (internal storage unit) */
    val heightCm: Double = 0.0,

    /** Height display preference */
    val heightUnit: HeightUnit = HeightUnit.CM,

    /** Current weight in kilograms (internal storage unit) */
    val weightKg: Double = 0.0,

    /** Weight display preference */
    val weightUnit: WeightUnit = WeightUnit.KG,

    /** User's physical activity level */
    val activityLevel: ActivityLevel = ActivityLevel.NOT_SET,

    /** Goal weight in kilograms (optional) */
    val goalWeightKg: Double = 0.0,

    /** User's primary health goals */
    val healthGoals: List<HealthGoal> = emptyList(),

    /** User's body frame size */
    val frameSize: FrameSize = FrameSize.MEDIUM,

    /** User's ethnicity/region for adjusted cutoffs */
    val ethnicityRegion: EthnicityRegion = EthnicityRegion.GENERAL,

    /** Timestamp when profile was last updated */
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

/**
 * Biological gender options used in health calculations.
 * Medical formulas often differ by biological sex.
 */
enum class Gender(val displayName: String) {
    NOT_SET("Not Set"),
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other")
}

/**
 * Height display unit preference.
 */
enum class HeightUnit(val displayName: String) {
    CM("cm"),
    FEET_INCHES("ft/in")
}

/**
 * Weight display unit preference.
 */
enum class WeightUnit(val displayName: String) {
    KG("kg"),
    LBS("lbs")
}

/**
 * Physical activity level categories based on WHO guidelines.
 * Used in BMR/TDEE calculations with standard multipliers.
 */
enum class ActivityLevel(
    val displayName: String,
    val description: String,
    val emoji: String,
    /** TDEE multiplier applied to BMR */
    val multiplier: Double,
    val shortName: String = "",
    val examples: String = ""
) {
    NOT_SET(
        "Not Set",
        "Select your activity level",
        "❓",
        1.0,
        "None",
        ""
    ),
    SEDENTARY(
        "Sedentary",
        "Little or no exercise, desk job",
        "🪑",
        1.2,
        "Sedentary",
        "Office job, mostly sitting, minimal physical activity"
    ),
    LIGHTLY_ACTIVE(
        "Lightly Active",
        "Light exercise 1-3 days/week",
        "🚶",
        1.375,
        "Light",
        "Walking, light housework, casual sports occasionally"
    ),
    MODERATELY_ACTIVE(
        "Moderately Active",
        "Moderate exercise 3-5 days/week",
        "🏃",
        1.55,
        "Moderate",
        "Jogging, cycling, swimming, gym sessions regularly"
    ),
    VERY_ACTIVE(
        "Very Active",
        "Hard exercise 6-7 days/week",
        "💪",
        1.725,
        "Very Active",
        "Intense training, sports teams, physical demanding routine"
    ),
    EXTREMELY_ACTIVE(
        "Extremely Active",
        "Very hard exercise, physical job",
        "🏋️",
        1.9,
        "Extreme",
        "Construction work, military training, athlete in training"
    );

    companion object {
        fun fromProfileString(activityString: String?): ActivityLevel? {
            if (activityString == null) return null
            return entries.find { 
                it.name.equals(activityString, ignoreCase = true) || 
                it.displayName.equals(activityString, ignoreCase = true) 
            } ?: when {
                activityString.contains("sedentary", ignoreCase = true) -> SEDENTARY
                activityString.contains("lightly", ignoreCase = true) -> LIGHTLY_ACTIVE
                activityString.contains("light", ignoreCase = true) -> LIGHTLY_ACTIVE
                activityString.contains("moderately", ignoreCase = true) -> MODERATELY_ACTIVE
                activityString.contains("moderate", ignoreCase = true) -> MODERATELY_ACTIVE
                activityString.contains("very", ignoreCase = true) -> VERY_ACTIVE
                activityString.contains("extremely", ignoreCase = true) -> EXTREMELY_ACTIVE
                activityString.contains("extreme", ignoreCase = true) -> EXTREMELY_ACTIVE
                else -> null
            }
        }
    }
}

/**
 * Health goal categories that influence calculator recommendations.
 */
enum class HealthGoal(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    NOT_SET(
        displayName = "Not Set",
        description = "Select your health goal",
        emoji = "❓"
    ),
    WEIGHT_LOSS(
        displayName = "Weight Loss",
        description = "Reduce body weight safely",
        emoji = "📉"
    ),
    MUSCLE_GAIN(
        displayName = "Muscle Gain",
        description = "Build lean muscle mass",
        emoji = "💪"
    ),
    MAINTAIN_WEIGHT(
        displayName = "Maintain Weight",
        description = "Keep current weight stable",
        emoji = "⚖️"
    ),
    GENERAL_HEALTH(
        displayName = "General Health",
        description = "Overall wellness improvement",
        emoji = "❤️"
    )
}

// ─── Unit Conversion Helpers ──────────────────────────────────────────────────

/**
 * Converts centimeters to feet and inches.
 * @return Pair of (feet, inches) where inches is rounded to 1 decimal
 */
fun cmToFeetInches(cm: Double): Pair<Int, Double> {
    val totalInches = cm / 2.54
    val feet = totalInches.toInt() / 12
    val inches = totalInches - (feet * 12)
    return Pair(feet, Math.round(inches * 10.0) / 10.0)
}

/**
 * Converts feet and inches to centimeters.
 */
fun feetInchesToCm(feet: Int, inches: Double): Double {
    val totalInches = (feet * 12) + inches
    return Math.round(totalInches * 2.54 * 10.0) / 10.0
}

/**
 * Converts kilograms to pounds.
 */
fun kgToLbs(kg: Double): Double {
    return Math.round(kg * 2.20462 * 10.0) / 10.0
}

/**
 * Converts pounds to kilograms.
 */
fun lbsToKg(lbs: Double): Double {
    return Math.round(lbs / 2.20462 * 10.0) / 10.0
}

/**
 * Calculates age in years from date of birth milliseconds.
 * Returns null if dateOfBirthMillis is null.
 */
fun calculateAge(dateOfBirthMillis: Long?): Int? {
    if (dateOfBirthMillis == null) return null
    val now = System.currentTimeMillis()
    val diffMillis = now - dateOfBirthMillis
    val diffYears = diffMillis / (365.25 * 24 * 60 * 60 * 1000)
    return diffYears.toInt()
}
