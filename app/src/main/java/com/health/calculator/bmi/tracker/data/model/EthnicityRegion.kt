package com.health.calculator.bmi.tracker.data.model

enum class EthnicityRegion(val displayName: String, val description: String) {
    GENERAL("General / Not Specified", "Use adult reference categories; ethnicity context is not specified"),
    CAUCASIAN("Caucasian / European", "Adult reference categories; waist cutoffs may differ by guideline"),
    AFRICAN("African / African American", "Adult reference categories; waist cutoffs may differ by guideline"),
    SOUTH_ASIAN("South Asian", "Lower BMI/waist cutoffs apply"),
    EAST_ASIAN("East Asian / Chinese", "Lower BMI/waist cutoffs apply"),
    SOUTHEAST_ASIAN("Southeast Asian", "Lower BMI/waist cutoffs apply"),
    JAPANESE("Japanese", "Japan-specific cutoffs"),
    HISPANIC("Hispanic / Latino", "Adult reference categories; waist cutoffs may differ by guideline"),
    MIDDLE_EASTERN("Middle Eastern", "Standard WHO cutoffs"),
    PACIFIC_ISLANDER("Pacific Islander", "Higher BMI cutoffs may apply"),
    INDIGENOUS("Indigenous / Native", "Adult reference categories; local guidance may differ"),
    MIXED("Mixed / Other", "Adult reference categories; local guidance may differ");

    companion object {
        fun fromName(name: String): EthnicityRegion {
            return entries.find { it.name == name } ?: GENERAL
        }
    }
}
