package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Health Metrics Tracker visual language.
 *
 * The palette is intentionally warm and quiet.  Action blue-green, progress
 * green, and the clay accent have separate jobs so an icon is not
 * automatically mistaken for a button or a warning.  Keep new UI on these
 * role tokens instead of adding one-off colours in feature screens.
 */

// Light color scheme
val PrimaryLight = Color(0xFF2B6F68)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD8EDE5)
val OnPrimaryContainerLight = Color(0xFF183B3A)

val SecondaryLight = Color(0xFF5F6B66)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE4E9E3)
val OnSecondaryContainerLight = Color(0xFF1C2824)

val TertiaryLight = Color(0xFFA34E35)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFE0D4)
val OnTertiaryContainerLight = Color(0xFF3E160D)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF7F5F0)
val OnBackgroundLight = Color(0xFF183B3A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF183B3A)
val SurfaceVariantLight = Color(0xFFE6E2D9)
val OnSurfaceVariantLight = Color(0xFF48534E)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF2A3431)
val InverseOnSurfaceLight = Color(0xFFF1F5F1)
val InversePrimaryLight = Color(0xFF8FD4C2)

// Dark color scheme
val PrimaryDark = Color(0xFF8FD4C2)
val OnPrimaryDark = Color(0xFF0E3630)
val PrimaryContainerDark = Color(0xFF29403C)
val OnPrimaryContainerDark = Color(0xFFD8F0E8)

val SecondaryDark = Color(0xFFBCC9C2)
val OnSecondaryDark = Color(0xFF20312C)
val SecondaryContainerDark = Color(0xFF3B4B45)
val OnSecondaryContainerDark = Color(0xFFE2EEE8)

val TertiaryDark = Color(0xFFF0A07D)
val OnTertiaryDark = Color(0xFF431C11)
val TertiaryContainerDark = Color(0xFF5E2B1D)
val OnTertiaryContainerDark = Color(0xFFFFDCCF)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF121A19)
val OnBackgroundDark = Color(0xFFE5F0EB)
val SurfaceDark = Color(0xFF1B2624)
val OnSurfaceDark = Color(0xFFE5F0EB)
val SurfaceVariantDark = Color(0xFF263632)
val OnSurfaceVariantDark = Color(0xFFC9D8D1)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE5F0EB)
val InverseOnSurfaceDark = Color(0xFF2A3431)
val InversePrimaryDark = PrimaryLight

val OutlineLight = Color(0xFF6F7E77)
val OutlineVariantLight = Color(0xFFCAD2CA)
val OutlineDark = Color(0xFF91A29A)
val OutlineVariantDark = Color(0xFF41534D)
val ScrimLight = Color(0xFF000000)
val ScrimDark = Color(0xFF000000)

/** Explicit roles used by the redesigned surfaces. */
object WellnessPalette {
    val ActionLight = PrimaryLight
    val ActionDark = PrimaryDark
    val OnTrackLight = Color(0xFFB9D7C6)
    val OnTrackDark = Color(0xFF29403C)
    val WarmAccentLight = Color(0xFFD77A5B)
    val WarmAccentDark = Color(0xFFF0A07D)
    val QuietBorderLight = Color(0xFFE6E2D9)
    val QuietBorderDark = Color(0xFF41534D)
}

/** Semantic colors for interpretation, never as the only signal of meaning. */
object HealthColors {
    val Healthy = Color(0xFF307D68)
    val HealthyLight = Color(0xFFD8EDE2)
    val HealthyDark = Color(0xFF8FD4C2)

    val Good = Color(0xFF3C7180)
    val GoodLight = Color(0xFFDCECEF)
    val GoodDark = Color(0xFF9BCDD2)

    val Warning = Color(0xFFA66300)
    val WarningLight = Color(0xFFFFE9C7)
    val WarningDark = Color(0xFFFFC978)

    val Caution = Color(0xFFB45309)
    val CautionLight = Color(0xFFFFE6D2)
    val CautionDark = Color(0xFFFFB77D)

    val Danger = Color(0xFFBA1A1A)
    val DangerLight = Color(0xFFFFDAD6)
    val DangerDark = Color(0xFFFFB4AB)

    val Severe = Color(0xFF7A4E78)
    val SevereLight = Color(0xFFF0DDEB)
    val SevereDark = Color(0xFFE0B7D7)

    val BelowNormal = Color(0xFF267C8A)
    val BelowNormalLight = Color(0xFFD6F0F3)
    val BelowNormalDark = Color(0xFF93D5DE)

    val Info = Color(0xFF3C7180)
    val InfoLight = Color(0xFFDCECEF)
    val InfoDark = Color(0xFF9BCDD2)
}

/** A restrained chart palette that remains legible in both themes. */
object ChartColors {
    val Primary = Color(0xFF2B6F68)
    val Secondary = Color(0xFF7A4E78)
    val Tertiary = Color(0xFFD77A5B)
    val Accent1 = Color(0xFFB46A14)
    val Accent2 = Color(0xFFA66300)
    val Accent3 = Color(0xFF307D68)

    val GridLine = Color(0xFFD8DDD8)
    val GridLineDark = Color(0xFF34443F)
    val GradientStart = Primary
    val GradientEnd = Color(0x332B6F68)
    val GradientStartDark = Color(0xFF8FD4C2)
    val GradientEndDark = Color(0x338FD4C2)
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
