// app/src/main/java/com/health/calculator/bmi/tracker/presentation/home/components/DashboardHeader.kt

package com.health.calculator.bmi.tracker.presentation.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.core.utils.DateUtils
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardHeader(
    userName: String?,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val greeting = remember { DateUtils.getGreeting() }
    val displayName = if (userName.isNullOrBlank()) null else userName

    // ── Entrance animations ──────────────────────────────────────────
    val greetingAlpha = remember { Animatable(0f) }
    val greetingOffsetY = remember { Animatable(20f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(20f) }
    val cardScale = remember { Animatable(0.9f) }
    val cardAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered entrance animations
        launch {
            greetingAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            greetingOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(120)
        launch {
            titleAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            titleOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(200)
        launch {
            cardAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            cardScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // ── Top Row: Greeting + Notification ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset { IntOffset(0, greetingOffsetY.value.toInt()) }
                    .alpha(greetingAlpha.value)
            ) {
                // Greeting text
                Text(
                    text = "$greeting 👋",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title or user name
                Text(
                    text = if (displayName != null) displayName else "Your Health Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .offset { IntOffset(0, titleOffsetY.value.toInt()) }
                        .alpha(titleAlpha.value)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // History Button
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.alpha(greetingAlpha.value)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.alpha(greetingAlpha.value)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Profile Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.alpha(greetingAlpha.value)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                // Notification bell
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .alpha(greetingAlpha.value)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                        // Notification dot indicator (placeholder)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Health Tip / Motivational Card ───────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .scale(cardScale.value)
                .alpha(cardAlpha.value),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                // Decorative background circles
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-10).dp, y = 15.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Track Your Health",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Use WHO-standard calculators to monitor your well-being and stay on top of your health goals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun DashboardHeaderPreview() {
    HealthCalculatorTheme {
        DashboardHeader(userName = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardHeaderWithNamePreview() {
    HealthCalculatorTheme {
        DashboardHeader(userName = "Alex")
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardHeaderDarkPreview() {
    HealthCalculatorTheme(themeMode = ThemeMode.DARK) {
        DashboardHeader(userName = null)
    }
}
