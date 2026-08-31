package com.health.calculator.bmi.tracker.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The six highest-value logging destinations are deliberately kept together. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackHubScreen(
    onOpenWeight: () -> Unit,
    onOpenWater: () -> Unit,
    onOpenBloodPressure: () -> Unit,
    onOpenFood: () -> Unit,
    onOpenHealthConnections: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenReminders: () -> Unit
) {
    HubScaffold(
        title = "Track",
        subtitle = "Small check-ins that make trends useful",
        icon = Icons.Outlined.DirectionsWalk
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Daily check-ins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Log what matters to you. Nothing here is required, and missing a day does not erase your progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { HubActionCard("Weight", "Add a weigh-in and see your trend", Icons.Outlined.MonitorWeight, onOpenWeight) }
            item { HubActionCard("Water", "Record glasses or millilitres", Icons.Outlined.WaterDrop, onOpenWater) }
            item { HubActionCard("Blood pressure", "Keep a careful home reading log", Icons.Outlined.MonitorHeart, onOpenBloodPressure) }
            item { HubActionCard("Food and calories", "Optional meal and calorie notes", Icons.Outlined.LocalDining, onOpenFood) }
            item { HubActionCard("Steps and connected data", "Choose whether to connect Health Connect", Icons.Outlined.DirectionsWalk, onOpenHealthConnections) }
            item { HubActionCard("History", "Review, edit or remove previous entries", Icons.Outlined.History, onOpenHistory) }
            item { HubActionCard("Reminders", "Choose helpful prompts only when you want them", Icons.Outlined.Notifications, onOpenReminders) }
        }
    }
}

enum class CalculatorDestination {
    BMI, BMR, BLOOD_PRESSURE, WATER, CALORIES, WAIST_HIP, HEART_RATE, IDEAL_WEIGHT, BSA, METABOLIC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsHubScreen(onOpen: (CalculatorDestination) -> Unit) {
    val calculators = listOf(
        CalculatorEntry(CalculatorDestination.BMI, "BMI", "A height-and-weight reference calculation", Icons.Outlined.Calculate),
        CalculatorEntry(CalculatorDestination.CALORIES, "Daily calories", "Estimate energy needs for planning", Icons.Outlined.LocalDining),
        CalculatorEntry(CalculatorDestination.WATER, "Water starting point", "A transparent adult beverage estimate", Icons.Outlined.WaterDrop),
        CalculatorEntry(CalculatorDestination.BLOOD_PRESSURE, "Blood pressure", "Record and understand a single reading", Icons.Outlined.MonitorHeart),
        CalculatorEntry(CalculatorDestination.WAIST_HIP, "Waist and hip", "Body-proportion reference measures", Icons.Outlined.Assessment),
        CalculatorEntry(CalculatorDestination.HEART_RATE, "Heart-rate zones", "Training ranges with a talk-test reminder", Icons.Outlined.FavoriteBorder),
        CalculatorEntry(CalculatorDestination.BMR, "Resting energy", "Compare adult BMR equations", Icons.Outlined.ShowChart),
        CalculatorEntry(CalculatorDestination.IDEAL_WEIGHT, "Height-based weight range", "An adult BMI reference range, not a target", Icons.Outlined.MonitorWeight),
        CalculatorEntry(CalculatorDestination.BSA, "Body surface area", "Compare historical BSA equations", Icons.Outlined.Timeline),
        CalculatorEntry(CalculatorDestination.METABOLIC, "Metabolic markers", "Count selected screening markers", Icons.Outlined.Flag)
    )
    HubScaffold(
        title = "Calculators",
        subtitle = "Estimates with context, limits and sources",
        icon = Icons.Outlined.Calculate
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Pick a starting point",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "These tools are informational estimates. Open a result to see the method, reference range and limitations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(calculators) { entry ->
                HubActionCard(entry.title, entry.description, entry.icon) { onOpen(entry.destination) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsHubScreen(
    onOpenWeeklyReport: () -> Unit,
    onOpenTrends: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenArticles: () -> Unit,
    onOpenHistory: () -> Unit
) {
    HubScaffold(
        title = "Insights",
        subtitle = "Patterns from the information you choose to record",
        icon = Icons.Outlined.AutoAwesome
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Your next useful view",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Insights describe logged patterns. They do not explain causes or diagnose conditions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { HubActionCard("Weekly wellness summary", "Compare your check-ins with the previous week", Icons.Outlined.Assessment, onOpenWeeklyReport) }
            item { HubActionCard("Trends", "See weight, hydration and blood-pressure history", Icons.Outlined.Timeline, onOpenTrends) }
            item { HubActionCard("AI Wellness Assistant", "Ask general wellness questions with optional context", Icons.Outlined.AutoAwesome, onOpenAssistant) }
            item { HubActionCard("Milestones", "Celebrate consistent, non-competitive progress", Icons.Outlined.Flag, onOpenAchievements) }
            item { HubActionCard("Learn", "Read practical, evidence-informed explainers", Icons.Outlined.ShowChart, onOpenArticles) }
            item { HubActionCard("All history", "Open the detailed history and export tools", Icons.Outlined.History, onOpenHistory) }
        }
    }
}

private data class CalculatorEntry(
    val destination: CalculatorDestination,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubScaffold(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}

@Composable
private fun HubActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$title. $description" },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.Edit, contentDescription = "Open $title", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
