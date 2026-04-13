package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PRIMARY PALETTE - Calming Teal/Blue (Medical Trust)
// ============================================================================

// Light Theme Primary
val PrimaryLight = Color(0xFF5B5CFF)          // Electric indigo
val OnPrimaryLight = Color(0xFFFFFFFF)         // White text on primary
val PrimaryContainerLight = Color(0xFFE5E4FF)  // Soft violet container
val OnPrimaryContainerLight = Color(0xFF1C1D7A) // Indigo text on container

// Dark Theme Primary
val PrimaryDark = Color(0xFFB3B2FF)            // Soft indigo for dark surfaces
val OnPrimaryDark = Color(0xFF27285E)
val PrimaryContainerDark = Color(0xFF34358D)
val OnPrimaryContainerDark = Color(0xFFE5E4FF)

// ============================================================================
// SECONDARY PALETTE - Warm Slate Blue (Complementary calm)
// ============================================================================

// Light Theme Secondary
val SecondaryLight = Color(0xFF00A6A6)         // Aqua accent
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFC6F8F7)
val OnSecondaryContainerLight = Color(0xFF004C4C)

// Dark Theme Secondary
val SecondaryDark = Color(0xFF7CE2E1)
val OnSecondaryDark = Color(0xFF003737)
val SecondaryContainerDark = Color(0xFF005E5E)
val OnSecondaryContainerDark = Color(0xFFC6F8F7)

// ============================================================================
// TERTIARY PALETTE - Gentle Purple (Premium accent)
// ============================================================================

// Light Theme Tertiary
val TertiaryLight = Color(0xFFFF6B8B)          // Energetic coral
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFDCE5)
val OnTertiaryContainerLight = Color(0xFF5B1025)

// Dark Theme Tertiary
val TertiaryDark = Color(0xFFFFB2C5)
val OnTertiaryDark = Color(0xFF5A1230)
val TertiaryContainerDark = Color(0xFF7C2A45)
val OnTertiaryContainerDark = Color(0xFFFFDCE5)

// ============================================================================
// ERROR PALETTE
// ============================================================================

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ============================================================================
// BACKGROUND & SURFACE - Light Theme
// ============================================================================

val BackgroundLight = Color(0xFFF5F7FF)
val OnBackgroundLight = Color(0xFF15173A)
val SurfaceLight = Color(0xFFFCFCFF)
val OnSurfaceLight = Color(0xFF1C1D3E)
val SurfaceVariantLight = Color(0xFFE8EBFF)
val OnSurfaceVariantLight = Color(0xFF4A4D73)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF2A2D53)
val InverseOnSurfaceLight = Color(0xFFF0F1FF)
val InversePrimaryLight = PrimaryDark

// ============================================================================
// BACKGROUND & SURFACE - Dark Theme
// ============================================================================

val BackgroundDark = Color(0xFF0D0F23)
val OnBackgroundDark = Color(0xFFE5E7FF)
val SurfaceDark = Color(0xFF12152B)
val OnSurfaceDark = Color(0xFFE5E7FF)
val SurfaceVariantDark = Color(0xFF2A2E4F)
val OnSurfaceVariantDark = Color(0xFFBCC1E8)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE5E7FF)
val InverseOnSurfaceDark = Color(0xFF24274B)
val InversePrimaryDark = PrimaryLight

// ============================================================================
// OUTLINE
// ============================================================================

val OutlineLight = Color(0xFF7A7FB0)
val OutlineVariantLight = Color(0xFFCACFF5)

val OutlineDark = Color(0xFF9096C9)
val OutlineVariantDark = Color(0xFF353A61)

// ============================================================================
// SCRIM
// ============================================================================

val ScrimLight = Color(0xFF000000)
val ScrimDark = Color(0xFF000000)

// ============================================================================
// HEALTH CATEGORY COLORS - Used across all calculators
// ============================================================================

// Health Status Colors - Semantic colors for BMI, BP, risk levels, etc.
object HealthColors {
    // Healthy / Normal / Optimal
    val Healthy = Color(0xFF2E7D32)              // Forest green
    val HealthyLight = Color(0xFFE8F5E9)         // Soft green background
    val HealthyDark = Color(0xFF81C784)           // Bright green for dark theme

    // Good / Slightly above normal but acceptable
    val Good = Color(0xFF558B2F)                  // Olive green
    val GoodLight = Color(0xFFF1F8E9)
    val GoodDark = Color(0xFFAED581)

    // Warning / Slightly elevated risk
    val Warning = Color(0xFFF9A825)               // Amber/gold
    val WarningLight = Color(0xFFFFF8E1)
    val WarningDark = Color(0xFFFFD54F)

    // Caution / Moderately elevated risk
    val Caution = Color(0xFFEF6C00)               // Deep orange
    val CautionLight = Color(0xFFFFF3E0)
    val CautionDark = Color(0xFFFFB74D)

    // Danger / High risk
    val Danger = Color(0xFFC62828)                // Deep red
    val DangerLight = Color(0xFFFFEBEE)
    val DangerDark = Color(0xFFEF5350)

    // Severe / Very high risk
    val Severe = Color(0xFF7B1FA2)                // Deep purple (extreme/critical)
    val SevereLight = Color(0xFFF3E5F5)
    val SevereDark = Color(0xFFCE93D8)

    // Underweight / Below normal
    val BelowNormal = Color(0xFF1565C0)           // Blue
    val BelowNormalLight = Color(0xFFE3F2FD)
    val BelowNormalDark = Color(0xFF64B5F6)

    // Informational / Neutral
    val Info = Color(0xFF0277BD)                  // Light blue
    val InfoLight = Color(0xFFE1F5FE)
    val InfoDark = Color(0xFF4FC3F7)
}

// ============================================================================
// CHART & GRAPH COLORS
// ============================================================================

object ChartColors {
    val Primary = Color(0xFF5B5CFF)
    val Secondary = Color(0xFF00A6A6)
    val Tertiary = Color(0xFFFF6B8B)
    val Accent1 = Color(0xFF7F6BFF)
    val Accent2 = Color(0xFF2DC5FF)
    val Accent3 = Color(0xFFFF9852)

    val GridLine = Color(0xFFE0E0E0)
    val GridLineDark = Color(0xFF424242)

    val GradientStart = Color(0xFF5B5CFF)
    val GradientEnd = Color(0x335B5CFF)
    val GradientStartDark = Color(0xFFB3B2FF)
    val GradientEndDark = Color(0x33B3B2FF)
}

// ============================================================================
// CALCULATOR ICON COLORS - Unique color per calculator for visual distinction
// ============================================================================

object CalculatorColors {
    val BMI = Color(0xFF0D7C8F)                   // Teal (primary brand)
    val BMR = Color(0xFFE65100)                    // Deep orange (energy/fire)
    val BloodPressure = Color(0xFFC62828)          // Red (heart/blood)
    val WaistToHip = Color(0xFF2E7D32)             // Green (body measurement)
    val WaterIntake = Color(0xFF0277BD)             // Blue (water)
    val MetabolicSyndrome = Color(0xFF7B1FA2)      // Purple (complex/medical)
    val BSA = Color(0xFF00838F)                     // Cyan (body surface)
    val IdealWeight = Color(0xFF558B2F)             // Olive (fitness/ideal)
    val DailyCalorie = Color(0xFFFF8F00)            // Amber (food/energy)
    val HeartRateZone = Color(0xFFD32F2F)           // Bright red (heart rate)
}

// Health status colors for Metabolic Syndrome
val HealthGreen = Color(0xFF4CAF50)
val HealthYellow = Color(0xFFFFC107)
val HealthOrange = Color(0xFFFF9800)
val HealthRed = Color(0xFFF44336)
val HealthBlue = Color(0xFF2196F3)
val HealthTeal = Color(0xFF009688)
