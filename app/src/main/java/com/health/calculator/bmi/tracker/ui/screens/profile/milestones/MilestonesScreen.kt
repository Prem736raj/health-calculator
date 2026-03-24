package com.health.calculator.bmi.tracker.ui.screens.profile.milestones

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.MilestoneType
import com.health.calculator.bmi.tracker.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonesScreen(
    viewModel: MilestonesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milestones & Records", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Journey Summary
                HealthJourneySummaryCard(summary = uiState.journeySummary)

                // Personal Records
                PersonalRecordsCard(records = uiState.personalRecords)

                // Milestones Timeline
                MilestonesSection(
                    earnedMilestones = viewModel.getFilteredEarnedMilestones(),
                    unearnedMilestoneTypes = viewModel.getFilteredUnearnedMilestones(),
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // New Record Celebration
    if (uiState.showNewRecordCelebration && uiState.newRecordType != null) {
        NewRecordCelebrationDialog(
            recordType = uiState.newRecordType!!,
            newValue = uiState.newRecordValue,
            previousValue = uiState.previousRecordValue,
            onDismiss = viewModel::dismissRecordCelebration
        )
    }

    // New Milestone Celebration
    if (uiState.showNewMilestoneCelebration && uiState.newMilestones.isNotEmpty()) {
        val currentIdx = uiState.currentMilestoneCelebrationIndex
        if (currentIdx < uiState.newMilestones.size) {
            NewMilestoneCelebrationDialog(
                milestoneType = uiState.newMilestones[currentIdx],
                remainingCount = uiState.newMilestones.size - currentIdx,
                onNext = viewModel::dismissMilestoneCelebration,
                onDismissAll = viewModel::dismissAllCelebrations
            )
        }
    }
}
