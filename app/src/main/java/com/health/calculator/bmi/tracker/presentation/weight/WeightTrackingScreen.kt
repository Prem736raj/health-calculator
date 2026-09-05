package com.health.calculator.bmi.tracker.presentation.weight

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.repository.WeightTimeFilter
import com.health.calculator.bmi.tracker.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackingScreen(
    viewModel: WeightTrackingViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarDismissed()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.txt_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.txt_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.txt_weight_tracking), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onLogWeightClick() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Weight")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                WeightStatisticsCard(
                    statistics = uiState.statistics,
                    useMetric = uiState.useMetric
                )
            }

            item {
                WeightComparisonCard(
                    weekly = uiState.weeklyComparison,
                    monthly = uiState.monthlyComparison,
                    useMetric = uiState.useMetric
                )
            }

            // Goal Progress (if set)
            uiState.goalProgress?.let { progress ->
                item {
                    WeightGoalProgressCard(
                        progress = progress,
                        useMetric = uiState.useMetric
                    )
                }
            }

            // Trend Graph
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.txt_history_trends),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Time Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            WeightTimeFilter.values().take(3).forEach { filter ->
                                FilterChip(
                                    selected = uiState.timeFilter == filter,
                                    onClick = { viewModel.onTimeFilterChange(filter) },
                                    label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    WeightTrendGraph(
                        entries = uiState.weights,
                        goalWeightKg = uiState.goalProgress?.goalWeight,
                        useMetric = uiState.useMetric
                    )
                }
            }

            // Recent Entries Header
            item {
                Text(
                    text = stringResource(R.string.txt_recent_logs),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // History List
            if (uiState.weights.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.txt_no_history_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.weights, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        useMetric = uiState.useMetric,
                        onEdit = { viewModel.onEditEntry(entry) },
                        onDelete = { viewModel.onDeleteEntry(entry) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (uiState.isLogDialogOpen) {
        LogWeightDialog(
            weightInput = uiState.weightInput,
            noteInput = uiState.noteInput,
            dateMillis = uiState.dateMillis,
            useMetric = uiState.useMetric,
            isSaving = uiState.isSaving,
            onWeightChange = { viewModel.onWeightInputChange(it) },
            onNoteChange = { viewModel.onNoteInputChange(it) },
            onDateClick = { showDatePicker = true },
            onSave = { viewModel.onSaveWeight() },
            onDismiss = { viewModel.onDismissLogDialog() },
            isEditing = uiState.editingEntry != null
        )
    }

}

@Composable
private fun WeightComparisonCard(
    weekly: com.health.calculator.bmi.tracker.domain.tracking.TrackingComparison?,
    monthly: com.health.calculator.bmi.tracker.domain.tracking.TrackingComparison?,
    useMetric: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Period comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "Averages compare logged entries only; they do not predict health outcomes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ComparisonRow("7 days", weekly, useMetric)
            ComparisonRow("30 days", monthly, useMetric)
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    comparison: com.health.calculator.bmi.tracker.domain.tracking.TrackingComparison?,
    useMetric: Boolean
) {
    val unit = if (useMetric) "kg" else "lb"
    val formatAverage: (Double) -> String = { value ->
        val display = if (useMetric) value else value * 2.20462
        String.format(Locale.getDefault(), "%.1f %s", display, unit)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$label vs previous", style = MaterialTheme.typography.bodyMedium)
        if (comparison == null) {
            Text("Need more logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val sign = if (comparison.percentChange > 0) "+" else ""
            Text(
                "$sign${String.format(Locale.getDefault(), "%.1f", comparison.percentChange)}% · ${formatAverage(comparison.currentAverage)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HistoryItem(
    entry: com.health.calculator.bmi.tracker.data.model.WeightEntry,
    useMetric: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFmt = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.formattedWeight(useMetric),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFmt.format(Date(entry.dateMillis)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
