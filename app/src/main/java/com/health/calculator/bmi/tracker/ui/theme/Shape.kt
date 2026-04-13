package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// SHAPE SYSTEM - Consistent rounded corners throughout the app
// ============================================================================

val HealthShapes = Shapes(
    // Small components: Chips, small buttons, toggles
    extraSmall = RoundedCornerShape(8.dp),

    // Small components: Text fields, small cards
    small = RoundedCornerShape(14.dp),

    // Medium components: Cards, dialogs, calculator input cards
    medium = RoundedCornerShape(20.dp),

    // Large components: Bottom sheets, large cards, result panels
    large = RoundedCornerShape(28.dp),

    // Extra large: Full-screen dialogs, modal bottoms, feature cards
    extraLarge = RoundedCornerShape(36.dp),
)

// ============================================================================
// CUSTOM SHAPE TOKENS - For specific health app UI elements
// ============================================================================

// Calculator result card - more rounded for a "badge" feel
val ResultCardShape = RoundedCornerShape(28.dp)

// Gauge/meter background shapes
val GaugeShape = RoundedCornerShape(50)            // Fully rounded (pill)

// Bottom navigation bar
val BottomNavShape = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 28.dp,
    bottomStart = 18.dp,
    bottomEnd = 18.dp
)

// Top app bar with subtle rounding at bottom
val TopBarShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 26.dp,
    bottomEnd = 26.dp
)

// Input field shape
val InputFieldShape = RoundedCornerShape(20.dp)

// Button shapes
val ButtonShape = RoundedCornerShape(18.dp)
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
