package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PRIMARY PALETTE - Calming Teal/Blue (Medical Trust)
// ============================================================================

// Light Theme Primary
val PrimaryLight = Color(0xFF0D7C8F)          // Deep teal - main brand color
val OnPrimaryLight = Color(0xFFFFFFFF)         // White text on primary
val PrimaryContainerLight = Color(0xFFB3E8F0)  // Soft teal container
val OnPrimaryContainerLight = Color(0xFF053B44) // Dark teal text on container

// Dark Theme Primary
val PrimaryDark = Color(0xFF6DD4E5)            // Bright teal for dark backgrounds
val OnPrimaryDark = Color(0xFF003640)          // Very dark teal text on primary
val PrimaryContainerDark = Color(0xFF005F6E)   // Medium teal container
val OnPrimaryContainerDark = Color(0xFFB3E8F0) // Light teal text on container

// ============================================================================
// SECONDARY PALETTE - Warm Slate Blue (Complementary calm)
// ============================================================================

// Light Theme Secondary
val SecondaryLight = Color(0xFF4A6572)         // Slate blue-gray
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCDE5ED) // Soft blue-gray container
val OnSecondaryContainerLight = Color(0xFF1A2E38)

// Dark Theme Secondary
val SecondaryDark = Color(0xFFB4CAD6)          // Light slate
val OnSecondaryDark = Color(0xFF1F333D)
val SecondaryContainerDark = Color(0xFF354D58)
val OnSecondaryContainerDark = Color(0xFFCDE5ED)

// ============================================================================
// TERTIARY PALETTE - Gentle Purple (Premium accent)
// ============================================================================

// Light Theme Tertiary
val TertiaryLight = Color(0xFF6B5778)          // Muted purple
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFF0DBFF) // Soft lavender container
val OnTertiaryContainerLight = Color(0xFF261432)

// Dark Theme Tertiary
val TertiaryDark = Color(0xFFD7BDE4)           // Light lavender
val OnTertiaryDark = Color(0xFF3C2A48)
val TertiaryContainerDark = Color(0xFF534060)
val OnTertiaryContainerDark = Color(0xFFF0DBFF)

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

val BackgroundLight = Color(0xFFF8FAFB)        // Very subtle cool white
val OnBackgroundLight = Color(0xFF191C1D)       // Near-black text
val SurfaceLight = Color(0xFFF8FAFB)
val OnSurfaceLight = Color(0xFF191C1D)
val SurfaceVariantLight = Color(0xFFDBE4E7)    // Subtle blue-gray surface
val OnSurfaceVariantLight = Color(0xFF3F484B)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF2E3132)
val InverseOnSurfaceLight = Color(0xFFEFF1F2)
val InversePrimaryLight = PrimaryDark

// ============================================================================
// BACKGROUND & SURFACE - Dark Theme
// ============================================================================

val BackgroundDark = Color(0xFF0F1415)          // Very dark blue-black
val OnBackgroundDark = Color(0xFFE1E3E4)        // Light gray text
val SurfaceDark = Color(0xFF0F1415)
val OnSurfaceDark = Color(0xFFE1E3E4)
val SurfaceVariantDark = Color(0xFF3F484B)
val OnSurfaceVariantDark = Color(0xFFBFC8CB)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE1E3E4)
val InverseOnSurfaceDark = Color(0xFF2E3132)
val InversePrimaryDark = PrimaryLight

// ============================================================================
// OUTLINE
// ============================================================================

val OutlineLight = Color(0xFF6F797B)
val OutlineVariantLight = Color(0xFFBFC8CB)

val OutlineDark = Color(0xFF899295)
val OutlineVariantDark = Color(0xFF3F484B)

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
    val Primary = Color(0xFF0D7C8F)
    val Secondary = Color(0xFF4A6572)
    val Tertiary = Color(0xFF6B5778)
    val Accent1 = Color(0xFF26A69A)
    val Accent2 = Color(0xFF5C6BC0)
    val Accent3 = Color(0xFFFF7043)

    val GridLine = Color(0xFFE0E0E0)
    val GridLineDark = Color(0xFF424242)

    val GradientStart = Color(0xFF0D7C8F)
    val GradientEnd = Color(0x330D7C8F)
    val GradientStartDark = Color(0xFF6DD4E5)
    val GradientEndDark = Color(0x336DD4E5)
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
