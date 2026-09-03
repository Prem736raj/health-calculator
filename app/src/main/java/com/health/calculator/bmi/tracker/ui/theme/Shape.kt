package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// SHAPE SYSTEM - Shape signals hierarchy, not just decoration.
// ============================================================================

val HealthShapes = Shapes(
    // Small components: Chips, small buttons, toggles
    extraSmall = RoundedCornerShape(6.dp),

    // Small components: Text fields and compact rows
    small = RoundedCornerShape(10.dp),

    // Medium components: metric tiles and calculator input cards
    medium = RoundedCornerShape(16.dp),

    // Large components: result panels and bottom sheets
    large = RoundedCornerShape(22.dp),

    // Extra large: the single hero surface on a dashboard
    extraLarge = RoundedCornerShape(28.dp),
)

// ============================================================================
// CUSTOM SHAPE TOKENS - For specific health app UI elements
// ============================================================================

// Calculator result card - more rounded for a "badge" feel
val ResultCardShape = RoundedCornerShape(24.dp)

/** The asymmetric shape reserves the strongest silhouette for the hero. */
val HeroCardShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 12.dp,
    bottomEnd = 28.dp,
    bottomStart = 12.dp
)

/** Compact, equal-weight tiles for daily metrics. */
val MetricTileShape = RoundedCornerShape(18.dp)

/** Low-ornament rows used for navigation and settings actions. */
val ActionRowShape = RoundedCornerShape(12.dp)

/** A callout shape that reads as editorial content rather than a data card. */
val InsightCalloutShape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 22.dp,
    bottomEnd = 22.dp,
    bottomStart = 8.dp
)

// Gauge/meter background shapes
val GaugeShape = RoundedCornerShape(50)            // Fully rounded (pill)

// Bottom navigation bar
val BottomNavShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

// Top app bar with subtle rounding at bottom
val TopBarShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 26.dp,
    bottomEnd = 26.dp
)

// Input field shape
val InputFieldShape = RoundedCornerShape(16.dp)

// Button shapes
val ButtonShape = RoundedCornerShape(16.dp)
val SmallButtonShape = RoundedCornerShape(12.dp)
val PillButtonShape = RoundedCornerShape(50)

// Health category indicator (small rounded badge)
val CategoryBadgeShape = RoundedCornerShape(8.dp)

// Card with only top corners rounded (for stacked lists)
val TopRoundedCardShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)
