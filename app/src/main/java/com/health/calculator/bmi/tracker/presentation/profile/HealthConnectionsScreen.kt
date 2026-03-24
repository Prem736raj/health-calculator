package com.health.calculator.bmi.tracker.presentation.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.HealthConnection
import com.health.calculator.bmi.tracker.data.model.HealthConnectionMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectionsScreen(
    state: MultiProfileUiState,
    onBackClick: () -> Unit,
    onNavigateToCalculator: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Connections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Your Health Data Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "See how your profile data flows into different calculators and how they interconnect.",
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
                            Text("Profile data changed", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
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
