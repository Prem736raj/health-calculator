package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryFilter
import com.health.calculator.bmi.tracker.data.model.HistorySortOption
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryFilterSheet(
    currentFilter: HistoryFilter,
    currentSort: HistorySortOption,
    availableCategories: List<String>,
    onApplyFilter: (HistoryFilter) -> Unit,
    onApplySort: (HistorySortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var localFilter by remember { mutableStateOf(currentFilter) }
    var localSort by remember { mutableStateOf(currentSort) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Date pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localFilter.startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    localFilter = localFilter.copy(startDate = datePickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localFilter.endDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    localFilter = localFilter.copy(endDate = datePickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    localFilter = HistoryFilter()
                    localSort = HistorySortOption.NEWEST_FIRST
                }) {
                    Text("Reset All", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === SORT SECTION ===
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistorySortOption.entries.forEach { option ->
                    FilterChip(
                        selected = localSort == option,
                        onClick = { localSort = option },
                        label = { Text(option.label, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (localSort == option) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === CALCULATOR TYPE FILTER ===
            Text(
                text = "Calculator Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorType.entries.forEach { type ->
                    val isSelected = type in localFilter.selectedTypes
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newTypes = if (isSelected)
                                localFilter.selectedTypes - type
                            else
                                localFilter.selectedTypes + type
                            localFilter = localFilter.copy(selectedTypes = newTypes)
                        },
                        label = {
                            Text(
                                "${type.emoji} ${type.shortName}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === DATE RANGE FILTER ===
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start date
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localFilter.startDate?.let { dateFormatter.format(Date(it)) }
                                    ?: "Any",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // End date
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localFilter.endDate?.let { dateFormatter.format(Date(it)) }
                                    ?: "Any",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Clear date range
            if (localFilter.startDate != null || localFilter.endDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        localFilter = localFilter.copy(startDate = null, endDate = null)
                    }
                ) {
                    Icon(Icons.Default.Clear, null, Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear dates", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === CATEGORY FILTER ===
            if (availableCategories.isNotEmpty()) {
                Text(
                    text = "Result Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = category in localFilter.selectedCategories
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newCategories = if (isSelected)
                                    localFilter.selectedCategories - category
                                else
                                    localFilter.selectedCategories + category
                                localFilter = localFilter.copy(selectedCategories = newCategories)
                            },
                            label = {
                                Text(category, style = MaterialTheme.typography.labelMedium)
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // === APPLY BUTTON ===
            Button(
                onClick = {
                    onApplyFilter(localFilter)
                    onApplySort(localSort)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.FilterList, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveFilterChips(
    filter: HistoryFilter,
    sortOption: HistorySortOption,
    onRemoveTypeFilter: (CalculatorType) -> Unit,
    onRemoveDateFilter: () -> Unit,
    onRemoveCategoryFilter: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    if (!filter.isActive && sortOption == HistorySortOption.NEWEST_FIRST) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Filters",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            TextButton(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    "Clear all",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort chip
            if (sortOption != HistorySortOption.NEWEST_FIRST) {
                item {
                    AssistChip(
                        onClick = {},
                        label = { Text("Sort: ${sortOption.label}", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.Sort, null, Modifier.size(14.dp))
                        }
                    )
                }
            }

            // Type filter chips
            items(filter.selectedTypes.toList()) { type ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveTypeFilter(type) },
                    label = { Text("${type.emoji} ${type.shortName}", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, "Remove", Modifier.size(14.dp))
                    }
                )
            }

            // Date range chip
            if (filter.startDate != null || filter.endDate != null) {
                item {
                    val dateText = buildString {
                        filter.startDate?.let { append(dateFormatter.format(Date(it))) }
                        append(" - ")
                        filter.endDate?.let { append(dateFormatter.format(Date(it))) }
                            ?: append("Now")
                    }
                    InputChip(
                        selected = true,
                        onClick = { onRemoveDateFilter() },
                        label = { Text(dateText, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, null, Modifier.size(14.dp))
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Close, "Remove", Modifier.size(14.dp))
                        }
                    )
                }
            }

            // Category chips
            items(filter.selectedCategories.toList()) { category ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveCategoryFilter(category) },
                    label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, "Remove", Modifier.size(14.dp))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
