// ui/screens/reports/WeeklyReportScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.reports

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.*
import com.health.calculator.bmi.tracker.domain.engagement.WellnessEngagementPolicy
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(
    viewModel: WeeklyReportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.txt_weekly_report), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showScheduleSettings() }) {
                        Icon(Icons.Outlined.Schedule, "Schedule")
                    }
                    IconButton(onClick = { viewModel.showShareDialog() }) {
                        Icon(Icons.Outlined.Share, "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.txt_generating_your_report), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            val summary = uiState.currentReport

            LazyColumn(
                modifier = modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (summary != null) {
                    // Non-judgmental logging rhythm summary. The legacy grade
                    // remains in storage for schema compatibility only.
                    item(key = "grade") {
                        WeeklyRhythmCard(
                            grade = summary.report.overallGrade,
                            message = summary.report.overallMessage,
                            weekStart = summary.report.weekStartDate,
                            weekEnd = summary.report.weekEndDate
                        )
                    }

                    // Wellness Score change
                    if (summary.report.healthScoreEnd >= 0) {
                        item(key = "score") {
                            HealthScoreChangeCard(
                                scoreStart = summary.report.healthScoreStart,
                                scoreEnd = summary.report.healthScoreEnd,
                                change = summary.report.healthScoreChange
                            )
                        }
                    }

                    // Highlights
                    if (summary.highlights.isNotEmpty()) {
                        item(key = "highlights_header") {
                            Text(stringResource(R.string.txt_highlights), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(summary.highlights, key = { it.title }) { highlight ->
                            HighlightCard(highlight = highlight)
                        }
                    }

                    // Metric Summaries
                    if (summary.metricSummaries.isNotEmpty()) {
                        item(key = "metrics_header") {
                            Text(stringResource(R.string.txt_this_week_s_metrics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(summary.metricSummaries, key = { it.metricName }) { metric ->
                            MetricSummaryCard(metric = metric)
                        }
                    }

                    // No data state
                    if (summary.metricSummaries.isEmpty()) {
                        item(key = "no_data") {
                            NoDataCard()
                        }
                    }

                    // Next Week Goals
                    if (summary.nextWeekGoals.isNotEmpty()) {
                        item(key = "goals_header") {
                            Text(stringResource(R.string.txt_next_week_focus), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(summary.nextWeekGoals, key = { it.suggestion }) { goal ->
                            NextWeekGoalCard(goal = goal)
                        }
                    }

                    // Previous reports
                    if (uiState.previousReports.size > 1) {
                        item(key = "prev_header") {
                            Text(stringResource(R.string.txt_previous_reports), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(
                            uiState.previousReports.drop(1).take(8),
                            key = { it.id }
                        ) { report ->
                            PreviousReportCard(
                                report = report,
                                onClick = { viewModel.loadReport(report.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                } else {
                    item { NoDataCard() }
                }
            }
        }
    }

    // Schedule Settings Dialog
    if (uiState.showScheduleSettings) {
        ReportScheduleDialog(
            enabled = uiState.reportScheduleEnabled,
            day = uiState.scheduledDay,
            hour = uiState.scheduledHour,
            minute = uiState.scheduledMinute,
            onToggle = viewModel::toggleSchedule,
            onDayChange = viewModel::setScheduleDay,
            onTimeChange = viewModel::setScheduleTime,
            onDismiss = viewModel::dismissScheduleSettings
        )
    }

    // Share Dialog
    if (uiState.showShareDialog) {
        ReportShareDialog(
            includeWeight = uiState.shareIncludeWeight,
            includeBmi = uiState.shareIncludeBmi,
            includeBp = uiState.shareIncludeBp,
            includeWater = uiState.shareIncludeWater,
            includeCalories = uiState.shareIncludeCalories,
            includeExercise = uiState.shareIncludeExercise,
            includeScore = uiState.shareIncludeScore,
            canShare = uiState.shareIncludeScore ||
                uiState.shareIncludeWeight ||
                uiState.shareIncludeBmi ||
                uiState.shareIncludeBp ||
                uiState.shareIncludeWater ||
                uiState.shareIncludeCalories ||
                uiState.shareIncludeExercise,
            onToggleSection = viewModel::toggleShareSection,
            onShare = { viewModel.shareReport(context) },
            onDismiss = viewModel::dismissShareDialog
        )
    }
}

@Composable
private fun WeeklyRhythmCard(grade: String, message: String, weekStart: Long, weekEnd: Long) {
    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
    val dateRange = "${dateFmt.format(Date(weekStart))} – ${dateFmt.format(Date(weekEnd))}"

    val accentColor = MaterialTheme.colorScheme.primary
    val rhythmLabel = WellnessEngagementPolicy.weeklyRhythmLabel(grade)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dateRange, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // A neutral rhythm icon avoids turning wellness logging into
                // an academic grade or a judgment of health.
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↻",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = rhythmLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HealthScoreChangeCard(scoreStart: Int, scoreEnd: Int, change: Int) {
    val changeColor = when {
        change > 0 -> Color(0xFF4CAF50)
        change < 0 -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("\uD83C\uDFC6 Wellness Score", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$scoreEnd",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(R.string.txt_100_3), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (change != 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (change > 0) "↑" else "↓",
                        style = MaterialTheme.typography.titleLarge,
                        color = changeColor
                    )
                    Text(
                        text = "${if (change > 0) "+" else ""}$change",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Text(
                        text = stringResource(R.string.txt_vs_last_week),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(
            text = "Informational logging metric · not clinically validated or diagnostic",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun MetricSummaryCard(metric: MetricWeeklySummary) {
    val trendColor = when (metric.trend) {
        MetricTrend.IMPROVING -> MaterialTheme.colorScheme.primary
        MetricTrend.DECLINING -> MaterialTheme.colorScheme.tertiary
        MetricTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        MetricTrend.NEW -> MaterialTheme.colorScheme.primary
        MetricTrend.NO_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = metric.icon, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(metric.metricName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = metric.currentValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(metric.detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(metric.trend.arrow, style = MaterialTheme.typography.titleMedium)
                Text(
                    metric.trend.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = trendColor,
                    fontWeight = FontWeight.Medium
                )
                metric.previousValue?.let {
                    Text("Was: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(highlight: WeeklyHighlight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(highlight.icon, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(highlight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(highlight.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NextWeekGoalCard(goal: NextWeekGoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(goal.icon, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(goal.suggestion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(goal.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PreviousReportCard(report: WeeklyReport, onClick: () -> Unit) {
    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
    val dateRange = "${dateFmt.format(Date(report.weekStartDate))} – ${dateFmt.format(Date(report.weekEndDate))}"
    val accentColor = MaterialTheme.colorScheme.primary
    val rhythmLabel = WellnessEngagementPolicy.weeklyRhythmLabel(report.overallGrade)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("↻", fontWeight = FontWeight.Bold, color = accentColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(dateRange, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(rhythmLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!report.isRead) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}

@Composable
private fun NoDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("\uD83D\uDCCA", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.txt_not_enough_data_yet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.txt_start_tracking_your_health_met),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
        }
    }
}
