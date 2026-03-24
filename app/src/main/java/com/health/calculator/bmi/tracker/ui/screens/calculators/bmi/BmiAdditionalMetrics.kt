package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// ─── Colors ───────────────────────────────────────────────────────────────────

private val MetricsAccent = Color(0xFF5C6BC0)

/**
 * Expandable section that displays additional BMI-related metrics:
 * 1. BMI Prime
 * 2. Ponderal Index
 * 3. Asian BMI Classification
 *
 * These are displayed below the main BMI result as supplementary information
 * that users can explore if interested, without cluttering the primary result.
 */
@Composable
fun BmiAdditionalMetricsSection(
    metrics: AdditionalBmiMetrics,
    bmiValue: Double,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val expandRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "expand_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(400, easing = EaseOutCubic)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Header (always visible, acts as toggle) ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MetricsAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = null,
                        tint = MetricsAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Additional Metrics",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "BMI Prime • Ponderal Index • Asian Categories",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MetricsAccent,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(expandRotation)
                )
            }

            // ── Expandable Content ────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + expandVertically(
                    animationSpec = tween(400, easing = EaseOutCubic)
                ),
                exit = fadeOut(tween(200)) + shrinkVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // 1. BMI Prime
                    BmiPrimeCard(bmiPrime = metrics.bmiPrime)

                    // 2. Ponderal Index
                    PonderalIndexCard(ponderalIndex = metrics.ponderalIndex)

                    // 3. Asian BMI Categories
                    AsianBmiCard(
                        asianCategory = metrics.asianCategory,
                        bmiValue = bmiValue,
                        standardCategory = BmiCategory.fromBmi(bmiValue)
                    )

                    // Info note
                    InfoNote()
                }
            }
        }
    }
}

// ─── BMI Prime Card ───────────────────────────────────────────────────────────

@Composable
private fun BmiPrimeCard(bmiPrime: BmiPrimeResult) {
    val statusColor = Color(bmiPrime.status.colorHex)

    MetricCard(
        icon = Icons.Filled.Speed,
        iconColor = statusColor,
        title = "BMI Prime",
        formulaText = "BMI ÷ 25.0"
    ) {
        // Value display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = String.format("%.2f", bmiPrime.value),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = bmiPrime.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Deviation percentage
            Column(horizontalAlignment = Alignment.End) {
                val deviationText = when {
                    bmiPrime.deviationPercent > 0 -> "+${String.format("%.1f", bmiPrime.deviationPercent)}%"
                    bmiPrime.deviationPercent < 0 -> "${String.format("%.1f", bmiPrime.deviationPercent)}%"
                    else -> "0%"
                }
                Text(
                    text = deviationText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = statusColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "from optimal (1.0)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Explanation
        Text(
            text = bmiPrime.status.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Scale visualization
        BmiPrimeScale(value = bmiPrime.value, statusColor = statusColor)

        Spacer(modifier = Modifier.height(8.dp))

        // Interpretation note
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Text(
                text = "💡 A BMI Prime of 1.0 corresponds to a BMI of 25 (upper normal limit). Values below 0.74 indicate underweight, while values above 1.0 suggest overweight or obesity.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun BmiPrimeScale(value: Double, statusColor: Color) {
    val position = ((value / 1.5).coerceIn(0.0, 1.0)).toFloat()

    Column {
        // Scale bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF43A047).copy(alpha = 0.3f),
                            Color(0xFF43A047).copy(alpha = 0.6f),
                            Color(0xFFFFC107).copy(alpha = 0.6f),
                            Color(0xFFE53935).copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            // Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth(position)
                    .height(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, statusColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Labels
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "0",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "0.74",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "1.0",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFFFFC107).copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "1.5",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// ─── Ponderal Index Card ──────────────────────────────────────────────────────

@Composable
private fun PonderalIndexCard(ponderalIndex: PonderalIndexResult) {
    val statusColor = Color(ponderalIndex.status.colorHex)

    MetricCard(
        icon = Icons.Outlined.Analytics,
        iconColor = Color(0xFF00897B),
        title = "Ponderal Index",
        formulaText = "Weight(kg) ÷ Height(m)³"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.1f", ponderalIndex.value),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "kg/m³",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = ponderalIndex.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Normal range",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "11–15 kg/m³",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF43A047)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = ponderalIndex.status.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // PI scale bar
        PonderalIndexScale(
            value = ponderalIndex.value,
            statusColor = statusColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Text(
                text = "💡 Unlike BMI, the Ponderal Index accounts for the cubic relationship between mass and height, making it less biased toward taller or shorter individuals. It's commonly used in neonatal medicine.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun PonderalIndexScale(value: Double, statusColor: Color) {
    // Scale from 8 to 20
    val position = ((value - 8.0) / 12.0).coerceIn(0.0, 1.0).toFloat()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF9800).copy(alpha = 0.4f),
                            Color(0xFF43A047).copy(alpha = 0.5f),
                            Color(0xFF43A047).copy(alpha = 0.5f),
                            Color(0xFFE53935).copy(alpha = 0.4f)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(position)
                    .height(8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, statusColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("8", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.weight(0.25f))
            Text("11", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.weight(0.33f))
            Text("15", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.weight(0.42f))
            Text("20", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

// ─── Asian BMI Category Card ──────────────────────────────────────────────────

@Composable
private fun AsianBmiCard(
    asianCategory: AsianBmiCategory,
    bmiValue: Double,
    standardCategory: BmiCategory
) {
    val asianColor = Color(asianCategory.colorHex)
    val standardColor = Color(standardCategory.colorHex)

    // Check if the Asian classification differs from standard
    val isDifferent = asianCategory.label != standardCategory.label

    MetricCard(
        icon = Icons.Outlined.Groups,
        iconColor = Color(0xFFE65100),
        title = "Asian BMI Classification",
        formulaText = "WHO Adjusted Cutoffs for Asian Populations"
    ) {
        // Show the classification
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = asianColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, asianColor.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = asianCategory.label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = asianColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Risk Level: ${asianCategory.riskLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = asianColor.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "BMI ${String.format("%.1f", bmiValue)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = asianColor
                )
            }
        }

        // Show difference alert if Asian category differs from standard
        if (isDifferent) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFF9800).copy(alpha = 0.06f),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, Color(0xFFFF9800).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "⚠️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Different from standard classification",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Standard WHO: ${standardCategory.label} • Asian WHO: ${asianCategory.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Asian BMI classification table
        AsianBmiTable(currentCategory = asianCategory, bmiValue = bmiValue)

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Text(
                text = "💡 Research shows Asian populations tend to develop health risks like diabetes and cardiovascular disease at lower BMI values. The WHO recommends these adjusted cutoffs for Asian, South Asian, and Southeast Asian populations.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun AsianBmiTable(
    currentCategory: AsianBmiCategory,
    bmiValue: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "BMI Range",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )

        // Category rows
        AsianBmiCategory.entries.forEach { category ->
            val isCurrentCategory = category == currentCategory
            val catColor = Color(category.colorHex)

            val rangeText = when (category) {
                AsianBmiCategory.UNDERWEIGHT -> "< 18.5"
                AsianBmiCategory.NORMAL -> "18.5 – 22.9"
                AsianBmiCategory.OVERWEIGHT -> "23.0 – 24.9"
                AsianBmiCategory.OBESE_CLASS_I -> "25.0 – 29.9"
                AsianBmiCategory.OBESE_CLASS_II -> "≥ 30.0"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCurrentCategory)
                            catColor.copy(alpha = 0.08f)
                        else
                            Color.Transparent
                    )
                    .border(
                        width = if (isCurrentCategory) 1.dp else 0.dp,
                        color = if (isCurrentCategory) catColor.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(catColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isCurrentCategory) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isCurrentCategory)
                        catColor
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isCurrentCategory) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isCurrentCategory)
                        catColor
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.End
                )

                // Current indicator
                if (isCurrentCategory) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "◀",
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ─── Metric Card Wrapper ──────────────────────────────────────────────────────

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    formulaText: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formulaText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

// ─── Info Note ────────────────────────────────────────────────────────────────

@Composable
private fun InfoNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MetricsAccent.copy(alpha = 0.6f),
            modifier = Modifier
                .size(14.dp)
                .offset { IntOffset(0, 4) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "These additional metrics provide supplementary perspectives on your body composition. They should be considered alongside your primary BMI result, not as standalone diagnoses. Always consult a healthcare professional for personalized health advice.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            lineHeight = 16.sp
        )
    }
}
