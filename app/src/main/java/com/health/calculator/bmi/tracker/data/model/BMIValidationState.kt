package com.health.calculator.bmi.tracker.data.model

data class BMIValidationState(
    val weightError: String? = null,
    val heightError: String? = null,
    val ageError: String? = null,
    val hasAttemptedCalculation: Boolean = false,
    val isCalculating: Boolean = false,
    val showResults: Boolean = false
) {
    val isValid: Boolean
        get() = weightError == null && heightError == null && ageError == null

    val hasAnyError: Boolean
        get() = weightError != null || heightError != null || ageError != null

    val shouldShakeWeight: Boolean
        get() = hasAttemptedCalculation && weightError != null

    val shouldShakeHeight: Boolean
        get() = hasAttemptedCalculation && heightError != null

    val shouldShakeAge: Boolean
        get() = hasAttemptedCalculation && ageError != null
}
