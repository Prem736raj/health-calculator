package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.unit.dp

/** Shared spacing tokens for new and refreshed screens. */
object HealthSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xLarge = 24.dp
    val xxLarge = 32.dp
    val screenHorizontal = 16.dp
    /** Minimum tappable size used by compact controls and icon actions. */
    val touchTarget = 48.dp
    /** Insets between a screen edge and its first content surface. */
    val screenVertical = 12.dp
    /** Standard interior padding for metric and result surfaces. */
    val card = 20.dp
    /** Tighter padding for list rows and secondary surfaces. */
    val cardCompact = 14.dp
    val section = 24.dp
}

/**
 * Elevation is intentionally sparse. A higher value means stronger hierarchy,
 * not another decorative shadow on every rounded rectangle.
 */
object HealthElevation {
    val hero = 5.dp
    val metric = 1.dp
    val row = 0.dp
    val floating = 4.dp
}
