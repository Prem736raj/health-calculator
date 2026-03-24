package com.health.calculator.bmi.tracker.ui.screens.whr

import com.health.calculator.bmi.tracker.data.model.Gender

object WhrEdgeCaseHandler {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val warningMessage: String? = null
    )

    fun validateInputs(
        waistValue: String,
        hipValue: String,
        ageValue: String,
        useMetric: Boolean
    ): ValidationResult {
        val waist = waistValue.toFloatOrNull()
        val hip = hipValue.toFloatOrNull()
        val age = ageValue.toIntOrNull()

        // Empty checks
        if (waistValue.isBlank()) return ValidationResult(false, "Please enter waist measurement")
        if (hipValue.isBlank()) return ValidationResult(false, "Please enter hip measurement")
        if (ageValue.isBlank()) return ValidationResult(false, "Please enter your age")

        // Numeric checks
        if (waist == null) return ValidationResult(false, "Waist must be a valid number")
        if (hip == null) return ValidationResult(false, "Hip must be a valid number")
        if (age == null) return ValidationResult(false, "Age must be a valid number")

        // Zero/negative
        if (waist <= 0) return ValidationResult(false, "Waist must be greater than zero")
        if (hip <= 0) return ValidationResult(false, "Hip must be greater than zero")
        if (age <= 0) return ValidationResult(false, "Age must be greater than zero")

        // Range checks
        if (useMetric) {
            if (waist < 40f || waist > 200f)
                return ValidationResult(false, "Waist should be between 40-200 cm")
            if (hip < 50f || hip > 200f)
                return ValidationResult(false, "Hip should be between 50-200 cm")
        } else {
            if (waist < 16f || waist > 80f)
                return ValidationResult(false, "Waist should be between 16-80 inches")
            if (hip < 20f || hip > 80f)
                return ValidationResult(false, "Hip should be between 20-80 inches")
        }

        if (age < 2 || age > 120)
            return ValidationResult(false, "Age must be between 2 and 120")

        // Equal waist and hip
        if (waist == hip) {
            return ValidationResult(
                true,
                warningMessage = "Waist and hip are equal (WHR = 1.00). This is unusual — please double-check your measurements."
            )
        }

        // Waist > hip warning
        if (waist > hip) {
            return ValidationResult(
                true,
                warningMessage = "Waist is larger than hip, indicating an apple body shape. Please verify measurements are correct."
            )
        }

        // Very extreme ratios
        val ratio = waist / hip
        if (ratio > 1.5f) {
            return ValidationResult(
                true,
                warningMessage = "Very high WHR (${String.format("%.2f", ratio)}). Please verify your measurements."
            )
        }
        if (ratio < 0.5f) {
            return ValidationResult(
                true,
                warningMessage = "Very low WHR (${String.format("%.2f", ratio)}). Please verify your measurements."
            )
        }

        return ValidationResult(true)
    }

    fun getEdgeCaseMessage(whr: Float, gender: Gender): String? {
        return when {
            whr >= 1.5f -> "This is an unusually high WHR. If your measurements are correct, please consult a healthcare provider."
            whr <= 0.5f -> "This is an unusually low WHR. Please verify your measurements are correct."
            whr == 1.0f -> "Your waist and hip measurements are equal. Consider re-measuring to ensure accuracy."
            else -> null
        }
    }

    fun handleMissingHeight(): String {
        return "Height data is needed for Waist-to-Height Ratio. You can add it in your profile or enter it manually."
    }
}
