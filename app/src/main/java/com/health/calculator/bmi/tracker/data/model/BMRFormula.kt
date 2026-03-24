// File: com/health/calculator/bmi/tracker/data/model/BMRFormula.kt
package com.health.calculator.bmi.tracker.data.model

enum class BMRFormula(
    val displayName: String,
    val year: String,
    val description: String,
    val requiresBodyFat: Boolean,
    val isRecommended: Boolean
) {
    HARRIS_BENEDICT_ORIGINAL(
        displayName = "Harris-Benedict Original",
        year = "1919",
        description = "The original BMR equation, widely used for over a century. May overestimate by 5-15% compared to modern formulas.",
        requiresBodyFat = false,
        isRecommended = false
    ),
    HARRIS_BENEDICT_REVISED(
        displayName = "Harris-Benedict Revised",
        year = "1984",
        description = "Updated version by Roza & Shizgal with improved accuracy over the original equation.",
        requiresBodyFat = false,
        isRecommended = false
    ),
    MIFFLIN_ST_JEOR(
        displayName = "Mifflin-St Jeor",
        year = "1990",
        description = "Considered the most accurate for most adults. Recommended by the Academy of Nutrition and Dietetics.",
        requiresBodyFat = false,
        isRecommended = true
    ),
    WHO_FAO_UNU(
        displayName = "WHO/FAO/UNU",
        year = "1985",
        description = "Developed by the World Health Organization. Uses age-grouped equations for global applicability.",
        requiresBodyFat = false,
        isRecommended = false
    ),
    KATCH_MCARDLE(
        displayName = "Katch-McArdle",
        year = "1996",
        description = "Uses lean body mass for calculation, making it more accurate for people who know their body fat percentage.",
        requiresBodyFat = true,
        isRecommended = false
    ),
    CUNNINGHAM(
        displayName = "Cunningham",
        year = "1991",
        description = "Similar to Katch-McArdle but designed for athletic populations. Best for active individuals.",
        requiresBodyFat = true,
        isRecommended = false
    );

    val tag: String
        get() = if (isRecommended) "⭐ Recommended" else year
}

data class BMRInputState(
    val weightText: String = "",
    val weightKg: Float = 0f,
    val isUnitKg: Boolean = true,
    val heightText: String = "",
    val heightCm: Float = 0f,
    val heightFeetText: String = "",
    val heightInchesText: String = "",
    val isUnitCm: Boolean = true,
    val ageText: String = "",
    val age: Int = 0,
    val isMale: Boolean = true,
    val selectedFormula: BMRFormula = BMRFormula.MIFFLIN_ST_JEOR,
    val bodyFatText: String = "",
    val bodyFatPercentage: Float = 0f,
    val isProfileDataUsed: Boolean = false,
    val hasModifiedProfile: Boolean = false
)

data class BMRValidationState(
    val weightError: String? = null,
    val heightError: String? = null,
    val ageError: String? = null,
    val bodyFatError: String? = null,
    val hasAttemptedCalculation: Boolean = false
) {
    val isValid: Boolean
        get() = weightError == null && heightError == null &&
                ageError == null && bodyFatError == null

    val hasAnyError: Boolean
        get() = !isValid
}
