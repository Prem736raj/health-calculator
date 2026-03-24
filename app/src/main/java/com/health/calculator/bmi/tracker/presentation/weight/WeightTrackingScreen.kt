package com.health.calculator.bmi.tracker.presentation.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Tracking", fontWeight = FontWeight.Bold) },
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
        }
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
                            text = "History & Trends",
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
                    text = "Recent Logs",
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
                            text = "No history available",
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
            onDismiss = { viewModel.onDismissLogDialog() }
        )
    }

    uiState.snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            // In a real app, you'd show a Snackbar via SnackbarHost
            // For now we'll just acknowledge it
            viewModel.onSnackbarDismissed()
        }
    }
}

@Composable
private fun HistoryItem(
    entry: com.health.calculator.bmi.tracker.data.model.WeightEntry,
    useMetric: Boolean,
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
