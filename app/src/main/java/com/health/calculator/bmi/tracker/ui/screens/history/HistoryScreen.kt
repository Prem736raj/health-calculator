package com.health.calculator.bmi.tracker.ui.screens.history

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.ui.components.history.*
import com.health.calculator.bmi.tracker.data.model.HistoryFilter
import com.health.calculator.bmi.tracker.data.model.HistorySortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: HistoryViewModel = viewModel(),
    statsViewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Handle export intent
    LaunchedEffect(uiState.exportIntent) {
        uiState.exportIntent?.let {
            context.startActivity(Intent.createChooser(it, "Share History"))
            viewModel.clearExportIntent()
        }
    }

    // Handle back button when in selection mode
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.deselectAll()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle export progress/completion snackbars
    LaunchedEffect(uiState.exportProgress) {
        val progress = uiState.exportProgress
        when {
            progress.isComplete -> {
                val result = snackbarHostState.showSnackbar(
                    message = "Export completed successfully",
                    actionLabel = "Share",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.shareExportedFile()
                }
                viewModel.resetExportProgress()
            }
            progress.error != null -> {
                snackbarHostState.showSnackbar(
                    message = progress.error,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetExportProgress()
            }
        }
    }

    // Handle undo snackbar
    LaunchedEffect(uiState.showUndoSnackbar) {
        if (uiState.showUndoSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "Entry deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearUndoSnackbar()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isSelectionMode) "Select Items" else if (selectedTab == 0) "History" else "Statistics",
                            fontWeight = FontWeight.Bold
                        )
                        if (!uiState.isSelectionMode && selectedTab == 0 && uiState.groupedEntries.isNotEmpty()) {
                            Text(
                                text = "${uiState.groupedEntries.sumOf { it.entries.size }} records total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = if (uiState.isSelectionMode) viewModel::deselectAll else onNavigateBack) {
                        Icon(
                            imageVector = if (uiState.isSelectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.showExportSheet(true) }) {
                            Icon(Icons.Default.IosShare, "Export")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (uiState.filter.isActive) {
                                        Badge { Text(uiState.filter.activeFilterCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.FilterList, "Filter")
                            }
                        }
                    } else {
                        IconButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.SelectAll, "Select All")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tabs
                if (!uiState.isSelectionMode) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Log") },
                            icon = { Icon(Icons.Default.History, null) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Statistics") },
                            icon = { Icon(Icons.Default.BarChart, null) }
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Search Bar
                    if (!uiState.isSelectionMode) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search results, notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    // Active Filters Chips
                    ActiveFilterChips(
                        filter = uiState.filter,
                        sortOption = uiState.sortOption,
                        onRemoveTypeFilter = { type ->
                            viewModel.setFilter(uiState.filter.copy(selectedTypes = uiState.filter.selectedTypes - type))
                        },
                        onRemoveDateFilter = {
                            viewModel.setFilter(uiState.filter.copy(startDate = null, endDate = null))
                        },
                        onRemoveCategoryFilter = { cat ->
                            viewModel.setFilter(uiState.filter.copy(selectedCategories = uiState.filter.selectedCategories - cat))
                        },
                        onClearAll = {
                            viewModel.setFilter(HistoryFilter())
                            viewModel.setSortOption(HistorySortOption.NEWEST_FIRST)
                        }
                    )
                }

                // Batch Operations Bar (Overlay logic handled by separate component, but we place it here)
                BatchOperationsBar(
                    selectedCount = uiState.selectedIds.size,
                    totalCount = uiState.groupedEntries.sumOf { it.entries.size },
                    isVisible = uiState.isSelectionMode,
                    onSelectAll = viewModel::selectAll,
                    onDeselectAll = viewModel::deselectAll,
                    onDeleteSelected = viewModel::requestDeleteSelected,
                    onExportSelected = { viewModel.showExportSheet(true) }
                )

                // History List
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.groupedEntries.isEmpty()) {
                    HistoryEmptyState(
                        isFiltered = uiState.filter.isActive || searchQuery.isNotEmpty(),
                        onClearFilters = {
                            viewModel.setFilter(HistoryFilter())
                            viewModel.onSearchQueryChange("")
                        },
                        onNavigateHome = onNavigateHome
                    )
                } else {
                    HistoryList(
                        groupedEntries = uiState.groupedEntries,
                        isSelectionMode = uiState.isSelectionMode,
                        selectedIds = uiState.selectedIds,
                        onEntryClick = viewModel::onEntryClick,
                        onEntryLongClick = viewModel::onEntryLongClick,
                        onNoteClick = viewModel::onNoteClick,
                        onDeleteClick = viewModel::deleteEntry,
                        onExportClick = viewModel::exportSelected
                    )
                }
            } else {
                StatisticsScreen(statsViewModel)
            }
        }
    }

        // Dialogs
        when (val dialog = uiState.dialogState) {
            is HistoryDialogState.EditNote -> {
                NoteEditDialog(
                    currentNote = dialog.currentNote,
                    onSave = { viewModel.saveNote(dialog.id, it) },
                    onDismiss = viewModel::dismissDialog
                )
            }
            is HistoryDialogState.ConfirmDelete -> {
                DeleteConfirmDialog(
                    count = dialog.ids.size,
                    onConfirm = viewModel::confirmDelete,
                    onDismiss = viewModel::dismissDialog
                )
            }
            else -> {}
        }

        // Filter Sheet
        if (showFilterSheet) {
            HistoryFilterSheet(
                currentFilter = uiState.filter,
                currentSort = uiState.sortOption,
                availableCategories = uiState.availableCategories,
                onApplyFilter = viewModel::setFilter,
                onApplySort = viewModel::setSortOption,
                onDismiss = { showFilterSheet = false }
            )
        }

        // Export Sheet
        if (uiState.showExportSheet) {
            ExportBottomSheet(
                onDismiss = { viewModel.showExportSheet(false) },
                onExport = viewModel::startExport,
                onScheduleSettings = { 
                    viewModel.showExportSheet(false)
                    viewModel.showScheduleDialog(true)
                },
                isFiltered = uiState.filter.isActive || searchQuery.isNotEmpty(),
                currentCalculator = if (uiState.filter.selectedTypes.size == 1) uiState.filter.selectedTypes.first() else null
            )
        }

        // Schedule Dialog
        if (uiState.showScheduleDialog) {
            ExportScheduleDialog(
                currentSchedule = uiState.currentSchedule,
                onDismiss = { viewModel.showScheduleDialog(false) },
                onSave = viewModel::updateSchedule
            )
        }

        // Progress Indicator Overlay
        if (uiState.exportProgress.isExporting) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Exporting Data") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { uiState.exportProgress.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(uiState.exportProgress.statusMessage)
                    }
                }
            )
        }
    }
}
