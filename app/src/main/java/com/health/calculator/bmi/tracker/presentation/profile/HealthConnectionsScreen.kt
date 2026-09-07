package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.PermissionController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.HealthConnection
import com.health.calculator.bmi.tracker.data.model.HealthConnectionMap
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectFeature
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectPermissionPolicy
import com.health.calculator.bmi.tracker.presentation.settings.SettingsUiState
import com.health.calculator.bmi.tracker.presentation.settings.SettingsViewModel
import com.health.calculator.bmi.tracker.data.model.StepHistoryEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectionsScreen(
    state: MultiProfileUiState,
    onBackClick: () -> Unit,
    onNavigateToCalculator: (String) -> Unit
) {
    val healthConnectViewModel: SettingsViewModel = hiltViewModel()
    val healthConnectState by healthConnectViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        healthConnectViewModel.checkHealthConnectStatus()
        healthConnectViewModel.syncHealthConnectData()
    }

    LaunchedEffect(healthConnectState.healthConnectSyncStatus) {
        healthConnectState.healthConnectSyncStatus?.let { message ->
            snackbarHostState.showSnackbar(message)
            healthConnectViewModel.dismissHealthConnectSyncStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.txt_health_connections), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ConnectionHeader(state.healthConnectionMap)

            HealthConnectAccessCard(
                state = healthConnectState,
                onConnect = { feature ->
                    permissionLauncher.launch(HealthConnectPermissionPolicy.permissionsFor(feature))
                },
                onSync = healthConnectViewModel::syncHealthConnectData
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (healthConnectState.healthConnectStepHistory.isNotEmpty()) {
                    item {
                        StepHistorySummary(
                            entries = healthConnectState.healthConnectStepHistory
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.txt_your_health_data_network),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        stringResource(R.string.txt_see_how_your_profile_data_flow),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                state.healthConnectionMap?.connections?.let { connections ->
                    items(connections) { connection ->
                        ConnectionCard(
                            connection = connection,
                            onNavigate = { onNavigateToCalculator(connection.calculatorRoute) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepHistorySummary(
    entries: List<StepHistoryEntry>,
    modifier: Modifier = Modifier
) {
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("EEE") }
    val days = entries
        .sortedBy { it.dayStartMillis }
        .takeLast(7)
        .map { entry ->
            Instant.ofEpochMilli(entry.dayStartMillis).atZone(zone).toLocalDate() to entry.steps
        }
    if (days.isEmpty()) return

    val maxSteps = days.maxOf { it.second }.coerceAtLeast(1L)
    val average = days.map { it.second }.average()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Recent steps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Read-only daily snapshots · average ${"%,.0f".format(average)} steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Outlined.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(104.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { (date, steps) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            if (steps > 0) "${(steps / 1_000.0).let { "%.1f".format(it) }}k" else "—",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height((56f * (steps.toFloat() / maxSteps)).coerceAtLeast(if (steps > 0) 6f else 2f).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.78f))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            date.format(formatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                "Trends describe recorded activity only; they do not predict health outcomes.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthConnectAccessCard(
    state: SettingsUiState,
    onConnect: (HealthConnectFeature) -> Unit,
    onSync: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Health Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (state.isHealthConnectSupported) "Optional read-only data for your daily cards" else "Health Connect is not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.isHealthConnectSupported) {
                    TextButton(onClick = onSync) { Text("Refresh") }
                }
            }

            if (state.isHealthConnectSupported) {
                HealthConnectFeatureRow(
                    icon = Icons.Outlined.DirectionsWalk,
                    title = "Steps",
                    value = state.healthConnectSteps?.let { "$it today" },
                    connected = state.isHealthConnectConnected,
                    onClick = { if (state.isHealthConnectConnected) onSync() else onConnect(HealthConnectFeature.STEPS) }
                )
                HealthConnectFeatureRow(
                    icon = Icons.Outlined.MonitorWeight,
                    title = "Weight",
                    value = state.healthConnectWeightKg?.let { "%.1f kg".format(it) },
                    connected = state.isHealthConnectWeightConnected,
                    onClick = { if (state.isHealthConnectWeightConnected) onSync() else onConnect(HealthConnectFeature.WEIGHT) }
                )
                Text(
                    "Allow either feature, change access, or revoke it in Health Connect settings. Health Metrics Tracker does not write data back.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HealthConnectFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String?,
    connected: Boolean,
    onClick: () -> Unit
) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        connected && value != null -> value
                        connected -> "Connected · tap to refresh"
                        else -> "Not connected · optional"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onClick) { Text(if (connected) "Refresh" else "Connect") }
        }
    }
}

@Composable
fun ConnectionHeader(map: HealthConnectionMap?) {
    if (map == null) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Data Points", value = "${map.profileFieldUsageCount}")
            StatItem(label = "Connections", value = "${map.totalInterconnections}")
            StatItem(
                label = "Alerts", 
                value = "${map.calculatorsNeedingRecalculation.size}",
                valueColor = if (map.calculatorsNeedingRecalculation.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun ConnectionCard(
    connection: HealthConnection,
    onNavigate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(connection.icon, fontSize = 20.sp)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = connection.calculatorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (connection.needsRecalculation) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.txt_profile_data_changed), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                IconButton(onClick = onNavigate) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recalculate", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Inputs
            if (connection.inputsFromProfile.isNotEmpty()) {
                InputSection("From Profile", connection.inputsFromProfile, MaterialTheme.colorScheme.primary)
            }
            
            connection.inputsFromOtherCalculators.forEach { link ->
                InputSection("From ${link.sourceCalculator}", listOf(link.dataField), MaterialTheme.colorScheme.secondary)
            }

            // Outputs
            if (connection.outputsUsedBy.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InputSection("Results Feed Into", connection.outputsUsedBy, MaterialTheme.colorScheme.tertiary, isOutput = true)
            }
        }
    }
}

@Composable
fun InputSection(title: String, fields: List<String>, color: Color, isOutput: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 4.dp
        ) {
            fields.forEach { field ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(field, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = color.copy(alpha = 0.1f),
                        labelColor = color
                    )
                )
            }
        }
    }
}

// FlowRow copy for Compose 1.3/Material3 (simple implementation)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content, modifier = modifier) { measurables, constraints ->
        val chipConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(chipConstraints) }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            var y = 0
            var x = 0
            var maxYInRow = 0
            
            placeables.forEach { placeable ->
                if (x + placeable.width > constraints.maxWidth) {
                    y += maxYInRow + crossAxisSpacing.roundToPx()
                    x = 0
                    maxYInRow = 0
                }
                
                placeable.placeRelative(x, y)
                x += placeable.width + mainAxisSpacing.roundToPx()
                maxYInRow = maxOf(maxYInRow, placeable.height)
            }
        }
    }
}
