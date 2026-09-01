package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Health Metrics Tracker brand palette.
 *
 * The blue-green primary is calm and action-oriented without looking clinical.
 * Foreground/container pairs are intentionally kept together so screens do not
 * have to invent one-off colors for normal, dark, or empty states.
 */

// Light color scheme
val PrimaryLight = Color(0xFF2F6B68)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD1EEEA)
val OnPrimaryContainerLight = Color(0xFF0B302E)

val SecondaryLight = Color(0xFF4F6470)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD7E7EC)
val OnSecondaryContainerLight = Color(0xFF0C2027)

val TertiaryLight = Color(0xFF6D5A8D)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFEADDFF)
val OnTertiaryContainerLight = Color(0xFF28143F)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF7FAF9)
val OnBackgroundLight = Color(0xFF17211F)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF17211F)
val SurfaceVariantLight = Color(0xFFE1EBE8)
val OnSurfaceVariantLight = Color(0xFF41514E)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF2B3130)
val InverseOnSurfaceLight = Color(0xFFECF2F0)
val InversePrimaryLight = Color(0xFF9DD9D2)

// Dark color scheme
val PrimaryDark = Color(0xFF9DD9D2)
val OnPrimaryDark = Color(0xFF073331)
val PrimaryContainerDark = Color(0xFF174A47)
val OnPrimaryContainerDark = Color(0xFFB9F1EA)

val SecondaryDark = Color(0xFFB7CBD2)
val OnSecondaryDark = Color(0xFF21343B)
val SecondaryContainerDark = Color(0xFF364A51)
val OnSecondaryContainerDark = Color(0xFFD3E8EF)

val TertiaryDark = Color(0xFFD9BDF6)
val OnTertiaryDark = Color(0xFF3A2550)
val TertiaryContainerDark = Color(0xFF533D6B)
val OnTertiaryContainerDark = Color(0xFFF0DBFF)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF101615)
val OnBackgroundDark = Color(0xFFE0E9E6)
val SurfaceDark = Color(0xFF18201F)
val OnSurfaceDark = Color(0xFFE0E9E6)
val SurfaceVariantDark = Color(0xFF293331)
val OnSurfaceVariantDark = Color(0xFFBFCBC7)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE0E9E6)
val InverseOnSurfaceDark = Color(0xFF2B3130)
val InversePrimaryDark = PrimaryLight

val OutlineLight = Color(0xFF71817E)
val OutlineVariantLight = Color(0xFFC1CECA)
val OutlineDark = Color(0xFF899793)
val OutlineVariantDark = Color(0xFF3F4A47)
val ScrimLight = Color(0xFF000000)
val ScrimDark = Color(0xFF000000)

/** Semantic colors for interpretation, never as the only signal of meaning. */
object HealthColors {
    val Healthy = Color(0xFF2E7D6F)
    val HealthyLight = Color(0xFFD8F1EB)
    val HealthyDark = Color(0xFF8DD3C7)

    val Good = Color(0xFF2F6B8A)
    val GoodLight = Color(0xFFDCEEF5)
    val GoodDark = Color(0xFF9ACDE2)

    val Warning = Color(0xFFA66300)
    val WarningLight = Color(0xFFFFE9C7)
    val WarningDark = Color(0xFFFFC978)

    val Caution = Color(0xFFB45309)
    val CautionLight = Color(0xFFFFE6D2)
    val CautionDark = Color(0xFFFFB77D)

    val Danger = Color(0xFFBA1A1A)
    val DangerLight = Color(0xFFFFDAD6)
    val DangerDark = Color(0xFFFFB4AB)

    val Severe = Color(0xFF6B3C88)
    val SevereLight = Color(0xFFF0DDF7)
    val SevereDark = Color(0xFFDDB4ED)

    val BelowNormal = Color(0xFF267C8A)
    val BelowNormalLight = Color(0xFFD6F0F3)
    val BelowNormalDark = Color(0xFF93D5DE)

    val Info = Color(0xFF2F6B8A)
    val InfoLight = Color(0xFFDCEEF5)
    val InfoDark = Color(0xFF9ACDE2)
}

/** A restrained chart palette that remains legible in both themes. */
object ChartColors {
    val Primary = Color(0xFF2F6B68)
    val Secondary = Color(0xFF6D5A8D)
    val Tertiary = Color(0xFFA94D6A)
    val Accent1 = Color(0xFFB45309)
    val Accent2 = Color(0xFFA66300)
    val Accent3 = Color(0xFF2E7D6F)

    val GridLine = Color(0xFFD8E2DF)
    val GridLineDark = Color(0xFF34413E)
    val GradientStart = Primary
    val GradientEnd = Color(0x332F6B68)
    val GradientStartDark = Color(0xFF9DD9D2)
    val GradientEndDark = Color(0x339DD9D2)
}

/** Stable accents for calculator discovery cards; interpretation stays semantic. */
object CalculatorColors {
    val BMI = Color(0xFF2E7D6F)
    val BMR = Color(0xFFB46A14)
    val BloodPressure = Color(0xFFB94A5A)
    val WaistToHip = Color(0xFF6D5A8D)
    val WaterIntake = Color(0xFF267C8A)
    val MetabolicSyndrome = Color(0xFFB45309)
    val BSA = Color(0xFF39718A)
    val IdealWeight = Color(0xFF2E7D6F)
    val DailyCalorie = Color(0xFFB46A14)
    val HeartRateZone = Color(0xFFB94A5A)
}

// Compatibility aliases used by older feature screens.
val HealthGreen = HealthColors.Healthy
val HealthYellow = HealthColors.Warning
val HealthOrange = HealthColors.Caution
val HealthRed = HealthColors.Danger
val HealthBlue = HealthColors.Info
val HealthTeal = HealthColors.BelowNormal
