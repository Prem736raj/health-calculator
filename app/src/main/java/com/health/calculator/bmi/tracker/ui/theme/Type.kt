package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * The type system uses platform families so it remains offline-safe and
 * respects the user's font scale. Sans-serif keeps instructions easy to scan;
 * monospace gives measured values a stable, instrument-like rhythm. The
 * hierarchy is deliberately carried by size, weight and spacing rather than
 * by making every surface bold.
 */

/** Primary body/UI typeface — system sans-serif (typically Roboto on Android). */
val BodyFontFamily: FontFamily = FontFamily.SansSerif

/** Secondary typeface for titles — system sans-serif until real fonts are bundled. */
val TitleFontFamily: FontFamily = FontFamily.SansSerif

/** Friendly editorial display voice for headlines. */
val WellnessDisplayFontFamily: FontFamily = FontFamily.Serif

/** Tabular numeric voice for metrics, counts, dates and calculator results. */
val WellnessMetricFontFamily: FontFamily = FontFamily.Monospace

// Legacy aliases — keep in sync with the primary names above.
val InterFontFamily: FontFamily = BodyFontFamily
val PlusJakartaSansFontFamily: FontFamily = TitleFontFamily

/** Use for results, counts, dates, and other values that users compare. */
val WellnessMetricTextStyle = TextStyle(
    fontFamily = WellnessMetricFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.2).sp,
    fontFeatureSettings = "tnum"
)

val WellnessMetricLargeTextStyle = WellnessMetricTextStyle.copy(
    fontSize = 32.sp,
    lineHeight = 38.sp,
    fontWeight = FontWeight.SemiBold
)

val WellnessMetricSmallTextStyle = WellnessMetricTextStyle.copy(
    fontSize = 16.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold
)

val HealthTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = WellnessDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
