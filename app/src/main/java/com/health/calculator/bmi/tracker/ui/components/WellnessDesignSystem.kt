package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.theme.ActionRowShape
import com.health.calculator.bmi.tracker.ui.theme.InsightCalloutShape
import com.health.calculator.bmi.tracker.ui.theme.MetricTileShape
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle

/** A stylish vector badge with energetic background and rounded squircle shape. */
@Composable
fun WellnessIconBadge(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = container,
        contentColor = tint,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.22f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * Vibrant gradient metric tile — each feature gets its own rich colour personality.
 * Gradient highlights, frosted translucent icon container, and crisp typography
 * give the dashboard an energetic, consumer-first feel.
 */
@Composable
fun WellnessMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    supportingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    gradientStart: Color = accent,
    gradientEnd: Color = accent.copy(alpha = 0.78f)
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 136.dp)
            .shadow(elevation = 6.dp, shape = MetricTileShape, spotColor = gradientStart.copy(alpha = 0.35f))
            .semantics { contentDescription = "$label. $value. $supportingText" },
        shape = MetricTileShape,
        color = Color.Transparent,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    ),
                    shape = MetricTileShape
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // White frosted badge on gradient
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.22f),
                    contentColor = Color.White,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = WellnessMetricTextStyle,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                progress?.let { fraction ->
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.28f)
                    )
                }
            }
        }
    }
}

/** Modern action row with colored icon accent and clean tactile feedback. */
@Composable
fun WellnessActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = ActionRowShape, spotColor = accent.copy(alpha = 0.12f))
            .semantics { contentDescription = "$title. $description" },
        shape = ActionRowShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WellnessIconBadge(
                icon = icon,
                tint = accent,
                container = accent.copy(alpha = 0.14f)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Open $title",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Section heading with energetic vertical color bar for immediate visual rhythm. */
@Composable
fun WellnessSectionLabel(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** An inviting, warm insight card with subtle gradient border and tint. */
@Composable
fun WellnessInsightCallout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = InsightCalloutShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        shape = InsightCalloutShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                    )
                )
            )
        ) {
            content()
        }
    }
}

/** Friendly, inviting empty state with clear call to action. */
@Composable
fun WellnessEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = ActionRowShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        shape = ActionRowShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
