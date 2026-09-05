package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * The type system uses only platform families so it remains offline-safe and
 * respects the user's font scale.  A serif display voice gives the product a
 * point of view; sans-serif keeps instructions easy to scan; monospace gives
 * measured values a stable, instrument-like rhythm.
 *
 * TODO: Bundle actual Inter/Plus Jakarta Sans font assets for a premium feel.
 *       When bundled, update these aliases to use the real FontFamily resources.
 */

/** Primary body/UI typeface — system sans-serif (typically Roboto on Android). */
val BodyFontFamily: FontFamily = FontFamily.SansSerif

/** Secondary typeface for titles — system sans-serif until real fonts are bundled. */
val TitleFontFamily: FontFamily = FontFamily.SansSerif

/** Modern, high-energy display voice for headlines — bold and friendly. */
val WellnessDisplayFontFamily: FontFamily = FontFamily.Default

/** Clean, modern numeric typeface for metrics, counts, and dates. */
val WellnessMetricFontFamily: FontFamily = FontFamily.Default

// Legacy aliases — keep in sync with the primary names above.
val InterFontFamily: FontFamily = BodyFontFamily
val PlusJakartaSansFontFamily: FontFamily = TitleFontFamily

/** Use for results, counts, dates, and other values that users compare. */
val WellnessMetricTextStyle = TextStyle(
    fontFamily = WellnessMetricFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.3).sp
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
