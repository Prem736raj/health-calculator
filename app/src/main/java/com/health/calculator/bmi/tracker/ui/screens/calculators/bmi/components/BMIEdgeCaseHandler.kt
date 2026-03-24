package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EdgeCaseMessage(
    val title: String,
    val message: String,
    val emoji: String,
    val severity: EdgeCaseSeverity,
    val showSeekHelp: Boolean = false
)

enum class EdgeCaseSeverity {
    WARNING, CONCERN, CRITICAL
}

object BMIEdgeCaseHandler {

    fun getEdgeCaseMessage(bmi: Float): EdgeCaseMessage? {
        return when {
            bmi < 10f -> EdgeCaseMessage(
                title = "Critically Low BMI",
                emoji = "🚨",
                message = "A BMI below 10 indicates a potentially life-threatening " +
                        "situation. This level of underweight requires immediate medical " +
                        "attention. Please reach out to a healthcare professional or " +
                        "emergency services right away. You are not alone — help is available.",
                severity = EdgeCaseSeverity.CRITICAL,
                showSeekHelp = true
            )
            bmi in 10f..12f -> EdgeCaseMessage(
                title = "Dangerously Low BMI",
                emoji = "⚠️",
                message = "A BMI this low is a serious health concern that requires " +
                        "urgent medical care. Your body needs proper nutrition and " +
                        "medical support to function safely. Please consult a doctor " +
                        "as soon as possible.",
                severity = EdgeCaseSeverity.CRITICAL,
                showSeekHelp = true
            )
            bmi in 12f..14f -> EdgeCaseMessage(
                title = "Severely Low BMI",
                emoji = "⚠️",
                message = "This BMI level indicates severe underweight that poses " +
                        "significant health risks. Medical guidance is strongly " +
                        "recommended to safely address nutritional needs.",
                severity = EdgeCaseSeverity.CONCERN,
                showSeekHelp = true
            )
            bmi in 50f..60f -> EdgeCaseMessage(
                title = "Extremely High BMI",
                emoji = "🩺",
                message = "A BMI at this level indicates extreme obesity with very " +
                        "serious health implications. Please seek comprehensive medical " +
                        "care. A team of healthcare specialists can help create a safe, " +
                        "effective plan. Remember — seeking help is a sign of strength.",
                severity = EdgeCaseSeverity.CRITICAL,
                showSeekHelp = true
            )
            bmi in 60f..80f -> EdgeCaseMessage(
                title = "Critically High BMI",
                emoji = "🚨",
                message = "This BMI level represents a critical health situation that " +
                        "requires immediate medical intervention. Please contact your " +
                        "healthcare provider urgently. Specialized medical support is " +
                        "available and can help.",
                severity = EdgeCaseSeverity.CRITICAL,
                showSeekHelp = true
            )
            bmi > 80f -> EdgeCaseMessage(
                title = "Extreme BMI Value",
                emoji = "⚠️",
                message = "This BMI value is unusually extreme. Please verify your " +
                        "weight and height inputs are correct. If accurate, immediate " +
                        "medical attention is essential.",
                severity = EdgeCaseSeverity.CRITICAL,
                showSeekHelp = true
            )
            else -> null
        }
    }

    fun validateWeight(weightKg: Float): String? {
        return when {
            weightKg <= 0f -> "Please enter a valid weight"
            weightKg < 2f -> "Weight seems too low. Please check your input"
            weightKg > 500f -> "Weight exceeds maximum range. Please verify"
            weightKg < 10f -> "Weight is unusually low. Please verify"
            weightKg > 350f -> "Weight is unusually high. Please verify"
            else -> null
        }
    }

    fun validateHeight(heightCm: Float): String? {
        return when {
            heightCm <= 0f -> "Please enter a valid height"
            heightCm < 30f -> "Height seems too low. Please check your input"
            heightCm > 280f -> "Height exceeds maximum range. Please verify"
            heightCm < 50f -> "Height is unusually low. Please verify"
            heightCm > 250f -> "Height is unusually high. Please verify"
            else -> null
        }
    }

    fun validateAge(age: Int): String? {
        return when {
            age <= 0 -> "Please enter a valid age"
            age < 2 -> "BMI calculation requires age 2 or above"
            age > 120 -> "Please enter a valid age (2-120)"
            else -> null
        }
    }

    fun validateInputs(weightKg: Float, heightCm: Float, age: Int): ValidationResult {
        val weightError = validateWeight(weightKg)
        val heightError = validateHeight(heightCm)
        val ageError = validateAge(age)

        return ValidationResult(
            isValid = weightError == null && heightError == null && ageError == null,
            weightError = weightError,
            heightError = heightError,
            ageError = ageError
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val weightError: String?,
    val heightError: String?,
    val ageError: String?
) {
    val hasAnyError: Boolean get() = !isValid
    val errors: List<String> get() = listOfNotNull(weightError, heightError, ageError)
}

// ============================================================
// Edge Case Warning Card UI
// ============================================================
@Composable
fun EdgeCaseWarningCard(
    edgeCaseMessage: EdgeCaseMessage,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (edgeCaseMessage.severity) {
        EdgeCaseSeverity.WARNING -> Color(0xFFFFF3E0)
        EdgeCaseSeverity.CONCERN -> Color(0xFFFCE4EC)
        EdgeCaseSeverity.CRITICAL -> Color(0xFFFFEBEE)
    }
    val borderColor = when (edgeCaseMessage.severity) {
        EdgeCaseSeverity.WARNING -> Color(0xFFFF9800)
        EdgeCaseSeverity.CONCERN -> Color(0xFFF44336)
        EdgeCaseSeverity.CRITICAL -> Color(0xFFB71C1C)
    }
    val iconColor = when (edgeCaseMessage.severity) {
        EdgeCaseSeverity.WARNING -> Color(0xFFE65100)
        EdgeCaseSeverity.CONCERN -> Color(0xFFC62828)
        EdgeCaseSeverity.CRITICAL -> Color(0xFFB71C1C)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = edgeCaseMessage.emoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = edgeCaseMessage.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    if (edgeCaseMessage.severity == EdgeCaseSeverity.CRITICAL) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = borderColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "IMMEDIATE ATTENTION NEEDED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = borderColor,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = edgeCaseMessage.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            if (edgeCaseMessage.showSeekHelp) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocalHospital,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Please Seek Help",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                            Text(
                                text = "Contact your healthcare provider, visit an " +
                                        "emergency room, or call a health helpline.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Validation Error Summary Card
// ============================================================
@Composable
fun ValidationErrorSummary(
    validationResult: ValidationResult,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && validationResult.hasAnyError,
        enter = fadeIn(tween(300)) + expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Please fix the following:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                validationResult.errors.forEach { error ->
                    Row(
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
