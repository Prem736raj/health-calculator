// app/src/main/java/com/health/calculator/bmi/tracker/presentation/home/components/CalculatorCard.kt

package com.health.calculator.bmi.tracker.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.theme.CalculatorColors
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class for calculator card information displayed on the Home Dashboard.
 */
data class CalculatorCardData(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

/**
 * Premium calculator card with press animation, subtle shadow, and decorative elements.
 */
@Composable
fun CalculatorCard(
    data: CalculatorCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0,
    contentOverlay: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    // ── Press interaction animation ──────────────────────────────────
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale_${data.id}"
    )

    val pressElevation by animateFloatAsState(
        targetValue = if (isPressed) 1f else 4f,
        animationSpec = tween(100),
        label = "pressElevation_${data.id}"
    )

    // ── Entrance animation ───────────────────────────────────────────
    val entranceScale = remember { Animatable(0.85f) }
    val entranceAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        scope.launch {
            entranceAlpha.animateTo(
                1f,
                tween(400, easing = FastOutSlowInEasing)
            )
        }
        entranceScale.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // ── Card colors ──────────────────────────────────────────────────
    val cardBackgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val iconBackgroundColor = data.color.copy(alpha = if (isDark) 0.2f else 0.1f)
    val decorativeCircleColor = data.color.copy(alpha = if (isDark) 0.06f else 0.04f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale * entranceScale.value)
            .alpha(entranceAlpha.value)
            .shadow(
                elevation = pressElevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = data.color.copy(alpha = 0.1f),
                spotColor = data.color.copy(alpha = 0.15f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── Decorative background circle ─────────────────────────
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 15.dp, y = (-15).dp)
                    .clip(CircleShape)
                    .background(decorativeCircleColor)
            )

            // ── Content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Icon with colored background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = data.color
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Description or Overlay
                if (contentOverlay != null) {
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        contentOverlay.invoke()
                    }
                } else {
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom action hint
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculate",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = data.color
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = data.color
                    )
                }
            }
        }
    }
}

/**
 * Wide/horizontal variant used for featured calculators at the top.
 */
@Composable
fun CalculatorCardWide(
    data: CalculatorCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    val isDark = isSystemInDarkTheme()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "widePressScale_${data.id}"
    )

    val entranceScale = remember { Animatable(0.9f) }
    val entranceAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        scope.launch {
            entranceAlpha.animateTo(
                1f,
                tween(400, easing = FastOutSlowInEasing)
            )
        }
        entranceScale.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val cardBackgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale * entranceScale.value)
            .alpha(entranceAlpha.value)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = data.color.copy(alpha = 0.08f),
                spotColor = data.color.copy(alpha = 0.12f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Decorative circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 25.dp)
                    .clip(CircleShape)
                    .background(data.color.copy(alpha = if (isDark) 0.06f else 0.04f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(data.color.copy(alpha = if (isDark) 0.2f else 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = data.color
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Arrow
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(data.color.copy(alpha = if (isDark) 0.15f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open ${data.title}",
                        modifier = Modifier.size(18.dp),
                        tint = data.color
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun CalculatorCardPreview() {
    HealthCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CalculatorCard(
                data = CalculatorCardData(
                    id = "bmi",
                    title = "BMI Calculator",
                    description = "Calculate your Body Mass Index using WHO standards",
                    icon = Icons.Rounded.MonitorWeight,
                    color = CalculatorColors.BMI,
                    route = "calculator/bmi"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculatorCardWidePreview() {
    HealthCalculatorTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CalculatorCardWide(
                data = CalculatorCardData(
                    id = "bmi",
                    title = "BMI Calculator",
                    description = "Calculate your Body Mass Index using WHO standards",
                    icon = Icons.Rounded.MonitorWeight,
                    color = CalculatorColors.BMI,
                    route = "calculator/bmi"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculatorCardDarkPreview() {
    HealthCalculatorTheme(themeMode = com.health.calculator.bmi.tracker.data.model.ThemeMode.DARK) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            CalculatorCard(
                data = CalculatorCardData(
                    id = "bp",
                    title = "Blood Pressure",
                    description = "Check your blood pressure category per WHO guidelines",
                    icon = Icons.Rounded.MonitorWeight,
                    color = CalculatorColors.BloodPressure,
                    route = "calculator/bp"
                ),
                onClick = {}
            )
        }
    }
}
