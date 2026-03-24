// app/src/main/java/com/health/calculator/bmi/tracker/ui/screens/welcomeback/WelcomeBackScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.welcomeback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeBackScreen(
    viewModel: WelcomeBackViewModel,
    onNavigateToCalculator: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.isVisible || uiState.data == null) return

    val data = uiState.data!!

    val waveAnim = rememberInfiniteTransition(label = "wave")
    val waveScale by waveAnim.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "wave_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {
                        viewModel.dismiss()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Animated wave emoji
            Text(
                text = "👋",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.scale(waveScale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (data.userName != null) "Welcome back, ${data.userName}!"
                else "Welcome back!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = getDaysAwayMessage(data.daysAway),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Streak Status
            if (data.streakStatus.wasWaterStreakBroken || data.streakStatus.wasTrackingStreakBroken) {
                StreakStatusCard(
                    streakStatus = data.streakStatus,
                    showFreezeOption = uiState.showStreakFreezeOption,
                    freezeCount = uiState.streakFreezeCount,
                    freezeApplied = uiState.freezeApplied,
                    onUseFreeze = { viewModel.useStreakFreeze() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Plant status
            if (data.plantStatus.needsAttention) {
                PlantStatusCard(plantStatus = data.plantStatus)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Last Known Metrics
            if (data.lastHealthMetrics.isNotEmpty()) {
                Text(
                    "Here's where you left off:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                data.lastHealthMetrics.forEach { metric ->
                    LastMetricCard(
                        metric = metric,
                        onClick = {
                            viewModel.dismiss()
                            onNavigateToCalculator(metric.route)
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Action Buttons
            Text(
                "Let's pick up where you left off!",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Most used calculators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data.mostUsedCalculators.take(3).forEach { calc ->
                    QuickActionButton(
                        calculator = calc,
                        onClick = {
                            viewModel.dismiss()
                            onNavigateToCalculator(calc.route)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary CTA
            Button(
                onClick = {
                    viewModel.dismiss()
                    onNavigateToCalculator("calculator/water_intake")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💧 Start with a Glass of Water")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    viewModel.dismiss()
                    onDismiss()
                }
            ) {
                Text("Go to Dashboard")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StreakStatusCard(
    streakStatus: StreakStatus,
    showFreezeOption: Boolean,
    freezeCount: Int,
    freezeApplied: Boolean,
    onUseFreeze: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (freezeApplied)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (freezeApplied) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Streak Protected!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text("Your streak freeze kept your streak alive!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                if (streakStatus.wasWaterStreakBroken) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Water streak ended at ${streakStatus.waterStreakBeforeBreak} days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Don't worry — start a new one today!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (streakStatus.wasTrackingStreakBroken) {
                    if (streakStatus.wasWaterStreakBroken) Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Tracking streak ended at ${streakStatus.trackingStreakBeforeBreak} days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Every day is a fresh start!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showFreezeOption) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onUseFreeze,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🛡️ Use Streak Freeze ($freezeCount available)")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantStatusCard(plantStatus: PlantWelcomeStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌱", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Your plant missed you!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "It needs some water to perk back up. Log a glass to help it grow!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LastMetricCard(
    metric: LastKnownMetric,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(metric.icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(metric.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (metric.category.isNotEmpty()) {
                    Text(metric.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                }
                Text(
                    if (metric.daysAgo == 0) "Today" else "${metric.daysAgo}d ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    calculator: FrequentCalculator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(calculator.icon, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                calculator.name.split(" ").first(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun getDaysAwayMessage(days: Int): String {
    return when {
        days < 3 -> "It's been a couple of days. Great to see you!"
        days < 7 -> "It's been $days days. Your health data is right where you left it."
        days < 14 -> "It's been about ${days / 7} week${if (days >= 14) "s" else ""}. We kept everything safe for you!"
        days < 30 -> "It's been a while — ${days} days to be exact. No judgment, just glad you're here!"
        else -> "It's been ${days} days. Every moment is the perfect time to restart. 💙"
    }
}
