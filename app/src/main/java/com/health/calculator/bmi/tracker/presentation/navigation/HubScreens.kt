package com.health.calculator.bmi.tracker.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.health.calculator.bmi.tracker.domain.insights.WellnessInsight
import com.health.calculator.bmi.tracker.ui.components.WellnessActionRow
import com.health.calculator.bmi.tracker.ui.components.WellnessIconBadge
import com.health.calculator.bmi.tracker.ui.components.WellnessInsightCallout
import com.health.calculator.bmi.tracker.ui.components.WellnessSectionLabel
import com.health.calculator.bmi.tracker.ui.theme.CalculatorColors
import com.health.calculator.bmi.tracker.ui.theme.FeatureColors
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.HealthSpacing

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
            contentPadding = PaddingValues(horizontal = HealthSpacing.screenHorizontal, vertical = HealthSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WellnessSectionLabel(
                    title = "Daily check-ins",
                    subtitle = "Log what matters to you. Nothing here is required, and missing a day does not erase your progress."
                )
            }
            item { HubActionCard("Weight", "Add a weigh-in and see your trend", Icons.Outlined.MonitorWeight, onOpenWeight, CalculatorColors.IdealWeight) }
            item { HubActionCard("Water", "Record glasses or millilitres", Icons.Outlined.WaterDrop, onOpenWater, CalculatorColors.WaterIntake) }
            item { HubActionCard("Blood pressure", "Keep a careful home reading log", Icons.Outlined.MonitorHeart, onOpenBloodPressure, CalculatorColors.BloodPressure) }
            item { HubActionCard("Food and calories", "Optional meal and calorie notes", Icons.Outlined.LocalDining, onOpenFood, CalculatorColors.DailyCalorie) }
            item { HubActionCard("Steps and connected data", "Choose whether to connect Health Connect", Icons.Outlined.DirectionsWalk, onOpenHealthConnections, FeatureColors.StepsDeep) }
            item { HubActionCard("History", "Review, edit or remove previous entries", Icons.Outlined.History, onOpenHistory, HealthColors.Info) }
            item { HubActionCard("Reminders", "Choose helpful prompts only when you want them", Icons.Outlined.Notifications, onOpenReminders, FeatureColors.BpDeep) }
        }
    }
}

enum class CalculatorDestination {
    BMI, BMR, BLOOD_PRESSURE, WATER, CALORIES, WAIST_HIP, HEART_RATE, IDEAL_WEIGHT, BSA, METABOLIC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsHubScreen(onOpen: (CalculatorDestination) -> Unit) {
    val calculators = remember {
        CalculatorQualityCatalog.all.map { info ->
            CalculatorEntry(
                destination = info.id,
                title = info.title,
                description = info.description,
                icon = calculatorIcon(info.id),
                qualityInfo = info
            )
        }
    }
    var selectedInfo by remember { mutableStateOf<CalculatorQualityInfo?>(null) }
    HubScaffold(
        title = "Calculators",
        subtitle = "Estimates with context, limits and sources",
        icon = Icons.Outlined.Calculate
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = HealthSpacing.screenHorizontal, vertical = HealthSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WellnessSectionLabel(
                    title = "Pick a starting point",
                    subtitle = "These tools are informational estimates. Open a result to see the method, reference range and limitations."
                )
            }
            items(calculators) { entry ->
                CalculatorHubCard(
                    entry = entry,
                    onOpen = { onOpen(entry.destination) },
                    onShowQuality = { selectedInfo = entry.qualityInfo }
                )
            }
        }
    }

    selectedInfo?.let { info ->
        CalculatorQualityDialog(info = info, onDismiss = { selectedInfo = null })
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
    onOpenHistory: () -> Unit,
    insights: List<WellnessInsight> = emptyList(),
    onOpenInsight: (String) -> Unit = { onOpenHistory() }
) {
    HubScaffold(
        title = "Insights",
        subtitle = "Patterns from the information you choose to record",
        icon = Icons.Outlined.AutoAwesome
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = HealthSpacing.screenHorizontal, vertical = HealthSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WellnessSectionLabel(
                    title = "Your next useful view",
                    subtitle = "Insights describe logged patterns. They do not explain causes or diagnose conditions."
                )
            }
            if (insights.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WellnessSectionLabel("From your recent logs")
                        insights.take(5).forEach { insight ->
                            InsightSummaryCard(insight = insight, onOpen = { onOpenInsight(insight.actionRoute) })
                        }
                    }
                }
            }
            item { HubActionCard("Weekly wellness summary", "Compare your check-ins with the previous week", Icons.Outlined.Assessment, onOpenWeeklyReport, MaterialTheme.colorScheme.primary) }
            item { HubActionCard("Trends", "See weight, hydration and blood-pressure history", Icons.Outlined.Timeline, onOpenTrends, HealthColors.Good) }
            item { HubActionCard("AI Wellness Assistant", "Ask general wellness questions with optional context", Icons.Outlined.AutoAwesome, onOpenAssistant, MaterialTheme.colorScheme.tertiary) }
            item { HubActionCard("Milestones", "Celebrate consistent, non-competitive progress", Icons.Outlined.Flag, onOpenAchievements, HealthColors.Healthy) }
            item { HubActionCard("Learn", "Read practical, evidence-informed explainers", Icons.Outlined.ShowChart, onOpenArticles, HealthColors.Info) }
            item { HubActionCard("All history", "Open the detailed history and export tools", Icons.Outlined.History, onOpenHistory, MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun InsightSummaryCard(insight: WellnessInsight, onOpen: () -> Unit) {
    WellnessInsightCallout(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WellnessIconBadge(
                    icon = Icons.Outlined.ShowChart,
                    tint = MaterialTheme.colorScheme.tertiary,
                    container = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(insight.message, style = MaterialTheme.typography.bodyMedium)
            Text(insight.evidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onOpen, contentPadding = PaddingValues(0.dp)) { Text("View detail") }
        }
    }
}

private data class CalculatorEntry(
    val destination: CalculatorDestination,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val qualityInfo: CalculatorQualityInfo
)

private fun calculatorIcon(destination: CalculatorDestination): ImageVector = when (destination) {
    CalculatorDestination.BMI -> Icons.Outlined.Calculate
    CalculatorDestination.BMR -> Icons.Outlined.ShowChart
    CalculatorDestination.BLOOD_PRESSURE -> Icons.Outlined.MonitorHeart
    CalculatorDestination.WATER -> Icons.Outlined.WaterDrop
    CalculatorDestination.CALORIES -> Icons.Outlined.LocalDining
    CalculatorDestination.WAIST_HIP -> Icons.Outlined.Assessment
    CalculatorDestination.HEART_RATE -> Icons.Outlined.FavoriteBorder
    CalculatorDestination.IDEAL_WEIGHT -> Icons.Outlined.MonitorWeight
    CalculatorDestination.BSA -> Icons.Outlined.Timeline
    CalculatorDestination.METABOLIC -> Icons.Outlined.Flag
}

private fun calculatorAccent(destination: CalculatorDestination): Color = when (destination) {
    CalculatorDestination.BMI -> CalculatorColors.BMI
    CalculatorDestination.BMR -> CalculatorColors.BMR
    CalculatorDestination.BLOOD_PRESSURE -> CalculatorColors.BloodPressure
    CalculatorDestination.WATER -> CalculatorColors.WaterIntake
    CalculatorDestination.CALORIES -> CalculatorColors.DailyCalorie
    CalculatorDestination.WAIST_HIP -> CalculatorColors.WaistToHip
    CalculatorDestination.HEART_RATE -> CalculatorColors.HeartRateZone
    CalculatorDestination.IDEAL_WEIGHT -> CalculatorColors.IdealWeight
    CalculatorDestination.BSA -> CalculatorColors.BSA
    CalculatorDestination.METABOLIC -> CalculatorColors.MetabolicSyndrome
}

@Composable
private fun CalculatorHubCard(
    entry: CalculatorEntry,
    onOpen: () -> Unit,
    onShowQuality: () -> Unit
) {
    val accent = calculatorAccent(entry.destination)
    WellnessActionRow(
        icon = entry.icon,
        title = entry.title,
        description = entry.description,
        onClick = onOpen,
        accent = accent,
        trailingContent = {
            IconButton(onClick = onShowQuality) {
                Icon(Icons.Outlined.Info, contentDescription = "Method and limits for ${entry.title}", tint = accent)
            }
        }
    )
}

@Composable
private fun CalculatorQualityDialog(
    info: CalculatorQualityInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${info.title}: method and limits") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QualityDialogSection("What it does", info.description)
                QualityDialogSection("Inputs", info.inputs)
                QualityDialogSection("Method", info.method)
                QualityDialogSection("How to read it", info.interpretation)
                QualityDialogSection("Limitations", info.limitations)
                Text("Sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                info.sources.forEach { source ->
                    Text("• $source", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (info.related.isNotEmpty()) {
                    Text("Related tools", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        info.related.map { CalculatorQualityCatalog.get(it).title }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Informational wellness estimate only—not a diagnosis or treatment plan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun QualityDialogSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

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
                navigationIcon = {
                    WellnessIconBadge(
                        icon = icon,
                        tint = MaterialTheme.colorScheme.primary,
                        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
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
    onClick: () -> Unit,
    accent: androidx.compose.ui.graphics.Color
) {
    WellnessActionRow(
        icon = icon,
        title = title,
        description = description,
        onClick = onClick,
        accent = accent
    )
}
