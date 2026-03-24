package com.health.calculator.bmi.tracker.data.model

enum class EthnicityRegion(val displayName: String, val description: String) {
    GENERAL("General / Not Specified", "Standard WHO cutoffs"),
    CAUCASIAN("Caucasian / European", "Standard WHO cutoffs"),
    AFRICAN("African / African American", "Standard WHO cutoffs"),
    SOUTH_ASIAN("South Asian", "Lower BMI/waist cutoffs apply"),
    EAST_ASIAN("East Asian / Chinese", "Lower BMI/waist cutoffs apply"),
    SOUTHEAST_ASIAN("Southeast Asian", "Lower BMI/waist cutoffs apply"),
    JAPANESE("Japanese", "Japan-specific cutoffs"),
    HISPANIC("Hispanic / Latino", "Standard WHO cutoffs"),
    MIDDLE_EASTERN("Middle Eastern", "Standard WHO cutoffs"),
    PACIFIC_ISLANDER("Pacific Islander", "Higher BMI cutoffs may apply"),
    INDIGENOUS("Indigenous / Native", "Standard WHO cutoffs"),
    MIXED("Mixed / Other", "Standard WHO cutoffs");

    companion object {
        fun fromName(name: String): EthnicityRegion {
            return entries.find { it.name == name } ?: GENERAL
        }
    }
}
