package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.HealthOverview
import com.health.calculator.bmi.tracker.data.model.WeightStatistics
import com.health.calculator.bmi.tracker.ui.components.HealthOverviewCard
import com.health.calculator.bmi.tracker.ui.components.ProfileWeightSection

@Composable
fun HealthOverviewSection(
    overview: HealthOverview,
    weightStatistics: WeightStatistics?,
    latestWeight: Double,
    useMetric: Boolean,
    onLogWeight: () -> Unit,
    onViewTrends: () -> Unit,
    onMetricClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Your Health Status",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Latest results from your calculations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileWeightSection(
            latestWeight = latestWeight,
            statistics = weightStatistics,
            useMetric = useMetric,
            onLogWeight = onLogWeight,
            onViewTrends = onViewTrends
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Key Metrics Grid/List
        HealthOverviewItem(overview.latestBmi, onMetricClick)
        HealthOverviewItem(overview.latestBp, onMetricClick)
        HealthOverviewItem(overview.latestWhr, onMetricClick)
        HealthOverviewItem(overview.latestBmr, onMetricClick)
        HealthOverviewItem(overview.metabolicSyndromeStatus, onMetricClick)

        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Metrics
        Text(
            text = "Activity & Nutrition",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummarySmallCard(
                title = "Hydration",
                value = "${overview.waterStreak} Day Streak",
                modifier = Modifier.weight(1f)
            )
            SummarySmallCard(
                title = "Calorie Adherence",
                value = "${(overview.calorieAdherence * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (overview.healthScore >= 0) {
            HealthScoreDonut(score = overview.healthScore)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun HealthOverviewItem(
    metric: com.health.calculator.bmi.tracker.data.model.HealthMetricSummary?,
    onMetricClick: (String, String) -> Unit
) {
    if (metric != null) {
        HealthOverviewCard(
            metric = metric,
            onClick = { onMetricClick(metric.navigateRoute, metric.label) },
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
fun SummarySmallCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HealthScoreDonut(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overall Health Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Based on your aggregated health data",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "$score",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
