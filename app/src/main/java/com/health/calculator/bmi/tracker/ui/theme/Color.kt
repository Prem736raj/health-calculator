package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * ─── VIBRANT WELLNESS PALETTE ──────────────────────────────────────────
 *
 * Every feature area has its own colour personality so the dashboard
 * reads as a curated, editorial surface — not a template.
 *
 *   Water  → ocean blues         Calories → warm sunset coral
 *   Weight → fresh lime-green    Heart    → living rose
 *   Steps  → sky cyan            BP       → deep magenta
 *   BMI    → rich violet         Sleep    → twilight indigo
 *
 * Rule: never use a one-off Color() literal in a feature screen.
 * Add it here first, give it a name, and explain its job.
 * ────────────────────────────────────────────────────────────────────────
 */

// ─── LIGHT SCHEME ──────────────────────────────────────────────────────

val PrimaryLight = Color(0xFF6C3CE1)        // Royal violet — bold, premium, unmistakable
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEDE4FF)
val OnPrimaryContainerLight = Color(0xFF22005D)

val SecondaryLight = Color(0xFF2ABBA7)       // Teal-mint — fresh & approachable
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCFFF6)
val OnSecondaryContainerLight = Color(0xFF00201B)

val TertiaryLight = Color(0xFFFF6B6B)        // Living coral — warm, playful, human
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFE2E0)
val OnTertiaryContainerLight = Color(0xFF3B0907)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFAF8FF)      // Hint-of-violet white — not flat white
val OnBackgroundLight = Color(0xFF1B1B2F)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1B1B2F)
val SurfaceVariantLight = Color(0xFFF0EDF6)
val OnSurfaceVariantLight = Color(0xFF49454F)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF313033)
val InverseOnSurfaceLight = Color(0xFFF4EFF4)
val InversePrimaryLight = Color(0xFFD0BCFF)

// ─── DARK SCHEME ───────────────────────────────────────────────────────

val PrimaryDark = Color(0xFFD0BCFF)          // Soft lilac — glows on dark surfaces
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEDE4FF)

val SecondaryDark = Color(0xFF70EFDE)        // Electric mint — pops beautifully
val OnSecondaryDark = Color(0xFF003731)
val SecondaryContainerDark = Color(0xFF005048)
val OnSecondaryContainerDark = Color(0xFFCCFFF6)

val TertiaryDark = Color(0xFFFFB4AB)         // Peach coral — warm even in dark
val OnTertiaryDark = Color(0xFF561E19)
val TertiaryContainerDark = Color(0xFF73342D)
val OnTertiaryContainerDark = Color(0xFFFFDAD6)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF0F0D1A)       // Deep space violet — rich, not boring gray
val OnBackgroundDark = Color(0xFFE6E1E5)
val SurfaceDark = Color(0xFF1C1B2E)          // Dark purple-slate
val OnSurfaceDark = Color(0xFFE6E1E5)
val SurfaceVariantDark = Color(0xFF2A2840)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE6E1E5)
val InverseOnSurfaceDark = Color(0xFF313033)
val InversePrimaryDark = PrimaryLight

val OutlineLight = Color(0xFF79747E)
val OutlineVariantLight = Color(0xFFE0DCE4)
val OutlineDark = Color(0xFF938F99)
val OutlineVariantDark = Color(0xFF49454F)
val ScrimLight = Color(0xFF000000)
val ScrimDark = Color(0xFF000000)

// ─── FEATURE GRADIENTS ─────────────────────────────────────────────────

/**
 * Each tracker/feature pair includes a start colour, an end colour, and a
 * text-on-gradient colour so every card can paint its own identity while
 * staying legible.
 */
object FeatureColors {
    // Water — deep ocean → bright sky
    val WaterStart = Color(0xFF0EA5E9)
    val WaterEnd = Color(0xFF38BDF8)
    val WaterDeep = Color(0xFF0284C7)
    val OnWater = Color(0xFFFFFFFF)

    // Weight — fresh green → lime
    val WeightStart = Color(0xFF10B981)
    val WeightEnd = Color(0xFF34D399)
    val WeightDeep = Color(0xFF059669)
    val OnWeight = Color(0xFFFFFFFF)

    // Calories — sunset orange → warm coral
    val CalorieStart = Color(0xFFF97316)
    val CalorieEnd = Color(0xFFFB923C)
    val CalorieDeep = Color(0xFFEA580C)
    val OnCalorie = Color(0xFFFFFFFF)

    // Steps — cyan → turquoise
    val StepsStart = Color(0xFF06B6D4)
    val StepsEnd = Color(0xFF22D3EE)
    val StepsDeep = Color(0xFF0891B2)
    val OnSteps = Color(0xFFFFFFFF)

    // Heart Rate — rose → pink
    val HeartStart = Color(0xFFF43F5E)
    val HeartEnd = Color(0xFFFB7185)
    val HeartDeep = Color(0xFFE11D48)
    val OnHeart = Color(0xFFFFFFFF)

    // Blood Pressure — deep magenta → fuchsia
    val BpStart = Color(0xFFD946EF)
    val BpEnd = Color(0xFFE879F9)
    val BpDeep = Color(0xFFC026D3)
    val OnBp = Color(0xFFFFFFFF)

    // BMI — violet → purple
    val BmiStart = Color(0xFF8B5CF6)
    val BmiEnd = Color(0xFFA78BFA)
    val BmiDeep = Color(0xFF7C3AED)
    val OnBmi = Color(0xFFFFFFFF)

    // Sleep/Wellness — indigo → twilight
    val SleepStart = Color(0xFF6366F1)
    val SleepEnd = Color(0xFF818CF8)
    val SleepDeep = Color(0xFF4F46E5)
    val OnSleep = Color(0xFFFFFFFF)

    // AI Coach — vibrant violet → luminous fuchsia
    val AiStart = Color(0xFF8B5CF6)
    val AiEnd = Color(0xFFEC4899)
    val AiDeep = Color(0xFF7C3AED)
    val OnAi = Color(0xFFFFFFFF)
}

/** Explicit roles used by the redesigned surfaces. */
object WellnessPalette {
    val ActionLight = PrimaryLight
    val ActionDark = PrimaryDark
    val OnTrackLight = Color(0xFFD1FAE5)
    val OnTrackDark = Color(0xFF064E3B)
    val WarmAccentLight = Color(0xFFF97316)
    val WarmAccentDark = Color(0xFFFB923C)
    val QuietBorderLight = Color(0xFFE0DCE4)
    val QuietBorderDark = Color(0xFF49454F)
}

/** Semantic colors for interpretation, never as the only signal of meaning. */
object HealthColors {
    val Healthy = Color(0xFF10B981)
    val HealthyLight = Color(0xFFD1FAE5)
    val HealthyDark = Color(0xFF34D399)

    val Good = Color(0xFF0EA5E9)
    val GoodLight = Color(0xFFE0F2FE)
    val GoodDark = Color(0xFF38BDF8)

    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val WarningDark = Color(0xFFFBBF24)

    val Caution = Color(0xFFF97316)
    val CautionLight = Color(0xFFFFF7ED)
    val CautionDark = Color(0xFFFB923C)

    val Danger = Color(0xFFEF4444)
    val DangerLight = Color(0xFFFEE2E2)
    val DangerDark = Color(0xFFF87171)

    val Severe = Color(0xFFA855F7)
    val SevereLight = Color(0xFFF3E8FF)
    val SevereDark = Color(0xFFC084FC)

    val BelowNormal = Color(0xFF06B6D4)
    val BelowNormalLight = Color(0xFFCFFAFE)
    val BelowNormalDark = Color(0xFF22D3EE)

    val Info = Color(0xFF6366F1)
    val InfoLight = Color(0xFFEEF2FF)
    val InfoDark = Color(0xFF818CF8)
}

/** A vibrant chart palette that pops in both themes. */
object ChartColors {
    val Primary = Color(0xFF6C3CE1)
    val Secondary = Color(0xFFF43F5E)
    val Tertiary = Color(0xFFF97316)
    val Accent1 = Color(0xFF10B981)
    val Accent2 = Color(0xFF0EA5E9)
    val Accent3 = Color(0xFFA855F7)

    val GridLine = Color(0xFFE0DCE4)
    val GridLineDark = Color(0xFF49454F)
    val GradientStart = Primary
    val GradientEnd = Color(0x336C3CE1)
    val GradientStartDark = Color(0xFFD0BCFF)
    val GradientEndDark = Color(0x33D0BCFF)
}

/** Stable accents for calculator discovery cards — each feels unique. */
object CalculatorColors {
    val BMI = Color(0xFF8B5CF6)
    val BMR = Color(0xFFF59E0B)
    val BloodPressure = Color(0xFFF43F5E)
    val WaistToHip = Color(0xFFA855F7)
    val WaterIntake = Color(0xFF0EA5E9)
    val MetabolicSyndrome = Color(0xFFF97316)
    val BSA = Color(0xFF06B6D4)
    val IdealWeight = Color(0xFF10B981)
    val DailyCalorie = Color(0xFFEA580C)
    val HeartRateZone = Color(0xFFE11D48)
}

// Compatibility aliases used by older feature screens.
val HealthGreen = HealthColors.Healthy
val HealthYellow = HealthColors.Warning
val HealthOrange = HealthColors.Caution
val HealthRed = HealthColors.Danger
val HealthBlue = HealthColors.Info
val HealthTeal = HealthColors.BelowNormal
