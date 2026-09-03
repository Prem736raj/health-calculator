package com.health.calculator.bmi.tracker.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.health.calculator.bmi.tracker.presentation.components.MedicalDisclaimerShort
import com.health.calculator.bmi.tracker.ui.components.WellnessEmptyState
import com.health.calculator.bmi.tracker.ui.components.WellnessIconBadge
import com.health.calculator.bmi.tracker.ui.components.WellnessInsightCallout
import com.health.calculator.bmi.tracker.ui.components.WellnessMetricTile
import com.health.calculator.bmi.tracker.ui.components.WellnessSectionLabel
import com.health.calculator.bmi.tracker.ui.components.home.HealthScoreBreakdownSheet
import com.health.calculator.bmi.tracker.ui.components.home.HomeSearchBar
import com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot
import com.health.calculator.bmi.tracker.util.HealthScoreCategory
import com.health.calculator.bmi.tracker.util.HealthScoreResult
import com.health.calculator.bmi.tracker.util.SmartRecommendation
import com.health.calculator.bmi.tracker.util.RecommendationType
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.ActionRowShape
import com.health.calculator.bmi.tracker.ui.theme.HeroCardShape
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricFontFamily
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle
import com.health.calculator.bmi.tracker.ui.theme.MetricTileShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.health.calculator.bmi.tracker.domain.insights.WellnessInsight
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

/**
 * A compact daily dashboard. Calculators remain discoverable, but the first
 * viewport prioritizes today's logging and the user's most recent metrics.
 */
@Composable
fun HomeScreen(
    onNavigateToBmi: () -> Unit,
    onNavigateToBmr: () -> Unit,
    onNavigateToBp: () -> Unit,
    onNavigateToWhr: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToMetabolic: () -> Unit,
    onNavigateToBsa: () -> Unit,
    onNavigateToIbw: () -> Unit,
    onNavigateToCalorie: () -> Unit,
    onNavigateToHeartRate: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiCoach: () -> Unit,
    onNavigateToWeight: () -> Unit,
    onNavigateToHealthConnections: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToTrack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: HomeSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by searchViewModel.recentSearches.collectAsStateWithLifecycle()
    var showScoreBreakdown by remember { mutableStateOf(false) }

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchViewModel.updateSearchQuery("")
    }

    val navigateRoute: (String) -> Unit = { route ->
        when (route) {
            "bmi_calculator" -> onNavigateToBmi()
            "bmr_calculator" -> onNavigateToBmr()
            "blood_pressure_calculator", "blood_pressure_checker", "bp_calculator" -> onNavigateToBp()
            "whr_calculator" -> onNavigateToWhr()
            "water_calculator", "water_intake_calculator" -> onNavigateToWater()
            "calorie_calculator" -> onNavigateToCalorie()
            "heart_rate_calculator", "heart_rate_zone_calculator" -> onNavigateToHeartRate()
            "metabolic_syndrome", "metabolic_syndrome_checker" -> onNavigateToMetabolic()
            "bsa_calculator" -> onNavigateToBsa()
            "ibw_calculator" -> onNavigateToIbw()
            "history" -> onNavigateToHistory()
            "profile" -> onNavigateToProfile()
            "weight_tracking" -> onNavigateToWeight()
            "health_connections" -> onNavigateToHealthConnections()
            "track" -> onNavigateToTrack()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                onOpenHistory = onNavigateToHistory,
                onOpenProfile = onNavigateToProfile,
                onOpenSettings = onNavigateToSettings
            )
        }

        item {
            HomeSearchBar(
                query = searchQuery,
                onQueryChange = searchViewModel::updateSearchQuery,
                results = searchResults,
                recentSearches = recentSearches,
                onClearRecent = searchViewModel::clearRecentSearches,
                onRemoveRecent = searchViewModel::removeRecentSearch,
                onResultClick = { result ->
                    searchViewModel.onSearchResultClick(result)
                    navigateRoute(result.navigationRoute)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            WellnessScoreCard(
                result = uiState.healthScore,
                onOpenDetails = { showScoreBreakdown = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            DailyMetricsSection(
                state = uiState,
                onOpenSteps = onNavigateToHealthConnections,
                onOpenWater = onNavigateToWater,
                onOpenWeight = onNavigateToWeight,
                onOpenCalories = onNavigateToCalorie,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            LatestMetricsSection(
                metrics = uiState.healthMetrics,
                onOpenBmi = onNavigateToBmi,
                onOpenBloodPressure = onNavigateToBp,
                onOpenHeartRate = onNavigateToHeartRate,
                onStartCheckIn = onNavigateToCalculators,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            InsightPreviewSection(
                insights = HomeDashboardPolicy.insightPreview(uiState.deterministicInsights),
                recommendations = HomeDashboardPolicy.insightPreview(uiState.recommendations),
                onOpenRecommendation = navigateRoute,
                onDismissRecommendation = viewModel::dismissRecommendation,
                onOpenAiAssistant = onNavigateToAiCoach,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            QuickActionsSection(
                onOpenWater = onNavigateToWater,
                onOpenWeight = onNavigateToWeight,
                onOpenBloodPressure = onNavigateToBp,
                onOpenCalculators = onNavigateToCalculators,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            RelevantCalculatorsCard(
                onOpenBmi = onNavigateToBmi,
                onOpenCalories = onNavigateToCalorie,
                onOpenAll = onNavigateToCalculators,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { MedicalDisclaimerShort() }
    }

    if (showScoreBreakdown) {
        HealthScoreBreakdownSheet(
            healthScore = uiState.healthScore,
            onDismiss = { showScoreBreakdown = false }
        )
    }
}

@Composable
private fun HomeHeader(
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your daily wellness snapshot",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenHistory) {
            Icon(Icons.Default.History, contentDescription = "Open history")
        }
        IconButton(onClick = onOpenProfile) {
            Icon(Icons.Default.Person, contentDescription = "Open profile")
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Open settings")
        }
    }
}

@Composable
private fun WellnessScoreCard(
    result: HealthScoreResult,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasEnoughData = result.category != HealthScoreCategory.INSUFFICIENT_DATA
    val displayedProgress = if (hasEnoughData) result.totalScore / 100f else 0f
    // Signature motion: only the Wellness Score ring animates on entry.
    val animatedProgress by animateFloatAsState(
        targetValue = displayedProgress,
        animationSpec = tween(durationMillis = 900),
        label = "wellness_score_ring"
    )

    Card(
        onClick = onOpenDetails,
        modifier = modifier.fillMaxWidth(),
        shape = HeroCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(76.dp),
                    strokeWidth = 7.dp,
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hasEnoughData) result.totalScore.toString() else "—",
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = WellnessMetricFontFamily),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text("/100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.scoreName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.category.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (hasEnoughData) {
                        "A custom summary of recent check-ins and daily logging."
                    } else {
                        "Record at least two metrics to build a useful consistency summary."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Informational only · not clinically validated",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View Wellness Score details", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun DailyMetricsSection(
    state: HomeUiState,
    onOpenSteps: () -> Unit,
    onOpenWater: () -> Unit,
    onOpenWeight: () -> Unit,
    onOpenCalories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = state.healthMetrics
    val cards = state.calculatorCardsState
    val numberFormat = remember { NumberFormat.getIntegerInstance() }
    val weight = cards.currentWeight?.takeIf { it > 0f }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(title = "Today", subtitle = "Small check-ins build useful trends")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DailyMetricCard(
                icon = Icons.Outlined.DirectionsWalk,
                label = "Steps",
                value = metrics.stepsToday?.let(numberFormat::format) ?: "Connect",
                supportingText = if (metrics.stepsToday == null) "Health Connect" else "synced today",
                progress = null,
                onClick = onOpenSteps,
                accent = HealthColors.Info,
                modifier = Modifier.weight(1f)
            )
            DailyMetricCard(
                icon = Icons.Outlined.WaterDrop,
                label = "Water",
                value = "${numberFormat.format(metrics.waterIntakeToday)} ml",
                supportingText = if (metrics.waterGoalToday > 0) {
                    "of ${numberFormat.format(metrics.waterGoalToday)} ml"
                } else {
                    "set a personal goal"
                },
                progress = metrics.waterGoalToday.takeIf { it > 0 }?.let {
                    (metrics.waterIntakeToday.toFloat() / it).coerceIn(0f, 1f)
                },
                onClick = onOpenWater,
                accent = HealthColors.Info,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DailyMetricCard(
                icon = Icons.Outlined.MonitorWeight,
                label = "Weight",
                value = weight?.let { String.format(Locale.getDefault(), "%.1f kg", it) } ?: "Log weight",
                supportingText = if (weight == null) "start a trend" else "latest log",
                progress = null,
                onClick = onOpenWeight,
                accent = HealthColors.Healthy,
                modifier = Modifier.weight(1f)
            )
            DailyMetricCard(
                icon = Icons.Outlined.LocalDining,
                label = "Calories",
                value = numberFormat.format(metrics.caloriesConsumedToday),
                supportingText = if (metrics.calorieTargetToday > 0) {
                    "of ${numberFormat.format(metrics.calorieTargetToday)} kcal"
                } else {
                    "optional food log"
                },
                progress = metrics.calorieTargetToday.takeIf { it > 0 }?.let {
                    (metrics.caloriesConsumedToday.toFloat() / it).coerceIn(0f, 1f)
                },
                onClick = onOpenCalories,
                accent = HealthColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DailyMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    supportingText: String,
    progress: Float?,
    onClick: () -> Unit,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    WellnessMetricTile(
        icon = icon,
        label = label,
        value = value,
        supportingText = supportingText,
        progress = progress,
        onClick = onClick,
        accent = accent,
        modifier = modifier
    )
}

@Composable
private fun LatestMetricsSection(
    metrics: HealthMetricsSnapshot,
    onOpenBmi: () -> Unit,
    onOpenBloodPressure: () -> Unit,
    onOpenHeartRate: () -> Unit,
    onStartCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableRows = buildList {
        metrics.bmi?.let {
            add(LatestMetric(Icons.Outlined.Assessment, "BMI", String.format(Locale.getDefault(), "%.1f", it), metrics.bmiCategory, onOpenBmi))
        }
        if (metrics.systolicBP != null && metrics.diastolicBP != null) {
            add(LatestMetric(Icons.Outlined.MonitorHeart, "Blood pressure", "${metrics.systolicBP}/${metrics.diastolicBP}", metrics.bpCategory, onOpenBloodPressure))
        }
        metrics.restingHR?.let {
            add(LatestMetric(Icons.Outlined.FavoriteBorder, "Resting heart rate", "$it bpm", "Latest saved value", onOpenHeartRate))
        }
    }.take(HomeDashboardPolicy.MAX_LATEST_METRICS)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(title = "Latest metrics", subtitle = "Your most recent saved check-ins")
        if (availableRows.isEmpty()) {
            WellnessEmptyState(
                icon = Icons.Outlined.Assessment,
                title = "No health metrics saved yet",
                message = "Start with one useful check-in. Your dashboard will grow as you add data.",
                actionLabel = "Choose a calculator",
                onAction = onStartCheckIn
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ActionRowShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
            ) {
                availableRows.forEachIndexed { index, metric ->
                    LatestMetricRow(metric)
                    if (index < availableRows.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

private data class LatestMetric(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val interpretation: String?,
    val onClick: () -> Unit
)

@Composable
private fun LatestMetricRow(metric: LatestMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = metric.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WellnessIconBadge(
            icon = metric.icon,
            tint = MaterialTheme.colorScheme.primary,
            container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(metric.label, style = MaterialTheme.typography.labelLarge)
            metric.interpretation?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(metric.value, style = WellnessMetricTextStyle, fontWeight = FontWeight.SemiBold)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InsightPreviewSection(
    insights: List<WellnessInsight>,
    recommendations: List<SmartRecommendation>,
    onOpenRecommendation: (String) -> Unit,
    onDismissRecommendation: (String) -> Unit,
    onOpenAiAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(title = "For you", subtitle = "Explainable prompts from your recent activity")
        WellnessInsightCallout(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (insights.isNotEmpty()) {
                    insights.forEachIndexed { index, insight ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WellnessIconBadge(
                                    icon = Icons.Outlined.ShowChart,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    container = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    insight.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                insight.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                insight.evidence,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                            )
                            TextButton(
                                onClick = { onOpenRecommendation(insight.actionRoute) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(insight.actionLabel)
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                        if (index < insights.lastIndex) HorizontalDivider()
                    }
                } else if (recommendations.isEmpty()) {
                    Text("Patterns will appear here", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Keep logging the metrics that matter to you. Suggestions are informational and never diagnoses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    recommendations.forEachIndexed { index, recommendation ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WellnessIconBadge(
                                    icon = recommendationIcon(recommendation.type),
                                    tint = recommendation.color,
                                    container = recommendation.color.copy(alpha = 0.14f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    recommendation.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onDismissRecommendation(recommendation.id) }) {
                                    Text("Hide")
                                }
                            }
                            Text(
                                recommendation.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            TextButton(
                                onClick = { onOpenRecommendation(recommendation.actionRoute) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(recommendation.actionLabel)
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                        if (index < recommendations.lastIndex) HorizontalDivider()
                    }
                }
                Text(
                    "App suggestions use saved activity rules. AI responses are separate.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.72f)
                )
                OutlinedButton(onClick = onOpenAiAssistant) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open AI Wellness Assistant")
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onOpenWater: () -> Unit,
    onOpenWeight: () -> Unit,
    onOpenBloodPressure: () -> Unit,
    onOpenCalculators: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        HomeAction("Water", Icons.Default.WaterDrop, onOpenWater),
        HomeAction("Weight", Icons.Default.Scale, onOpenWeight),
        HomeAction("Blood pressure", Icons.Default.Favorite, onOpenBloodPressure),
        HomeAction("Calculators", Icons.Default.Calculate, onOpenCalculators)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(title = "Quick actions", subtitle = "Go straight to a common task")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            actions.forEach { action ->
                Surface(
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f),
                    shape = ActionRowShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WellnessIconBadge(
                            icon = action.icon,
                            tint = MaterialTheme.colorScheme.primary,
                            container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            action.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class HomeAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private fun recommendationIcon(type: RecommendationType): ImageVector = when (type) {
    RecommendationType.BMI_CHECK -> Icons.Outlined.Assessment
    RecommendationType.BP_CHECK -> Icons.Outlined.MonitorHeart
    RecommendationType.WATER_REMINDER -> Icons.Outlined.WaterDrop
    RecommendationType.CALORIE_REMINDER -> Icons.Outlined.LocalDining
    RecommendationType.WEIGHT_TREND -> Icons.Outlined.MonitorWeight
    RecommendationType.GOAL_PROGRESS -> Icons.Outlined.ShowChart
    RecommendationType.WHR_CHECK -> Icons.Outlined.ShowChart
    RecommendationType.HR_CHECK -> Icons.Outlined.FavoriteBorder
    RecommendationType.ALL_GOOD -> Icons.Outlined.FavoriteBorder
    RecommendationType.STREAK -> Icons.Outlined.ShowChart
    RecommendationType.NEW_CALCULATOR -> Icons.Default.Calculate
    RecommendationType.PROFILE_INCOMPLETE -> Icons.Default.Person
}

@Composable
private fun RelevantCalculatorsCard(
    onOpenBmi: () -> Unit,
    onOpenCalories: () -> Unit,
    onOpenAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MetricTileShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeading(title = "Useful calculators", subtitle = "Estimates with methods, limits, and sources")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenBmi, modifier = Modifier.weight(1f)) {
                    Text("BMI")
                }
                OutlinedButton(onClick = onOpenCalories, modifier = Modifier.weight(1f)) {
                    Text("Daily energy")
                }
            }
            Button(onClick = onOpenAll, modifier = Modifier.fillMaxWidth()) {
                Text("Browse all calculators")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    WellnessSectionLabel(title = title, subtitle = subtitle)
}
