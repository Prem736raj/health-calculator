package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.export.ExportConfig
import com.health.calculator.bmi.tracker.data.export.ExportFormat
import com.health.calculator.bmi.tracker.data.export.ExportScope
import com.health.calculator.bmi.tracker.data.model.CalculatorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    onDismiss: () -> Unit,
    onExport: (ExportConfig) -> Unit,
    onScheduleSettings: () -> Unit,
    isFiltered: Boolean = false,
    currentCalculator: CalculatorType? = null
) {
    var config by remember { 
        mutableStateOf(ExportConfig(
            scope = if (currentCalculator != null) ExportScope.CALCULATOR else if (isFiltered) ExportScope.FILTERED else ExportScope.ALL,
            calculatorType = currentCalculator
        ))
    }

    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Export Health Data",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Format Selection
            SectionTitle("Export Format")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExportFormat.values().forEach { format ->
                    FormatCard(
                        format = format,
                        isSelected = config.format == format,
                        onClick = { config = config.copy(format = format) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Scope Selection
            SectionTitle("Data Scope")
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(bottom = 24.dp)
            ) {
                ExportScopeItem(
                    scope = ExportScope.ALL,
                    selected = config.scope == ExportScope.ALL,
                    onClick = { config = config.copy(scope = ExportScope.ALL) },
                    icon = Icons.Default.AllInbox
                )
                
                if (isFiltered) {
                    ExportScopeItem(
                        scope = ExportScope.FILTERED,
                        selected = config.scope == ExportScope.FILTERED,
                        onClick = { config = config.copy(scope = ExportScope.FILTERED) },
                        icon = Icons.Default.FilterList
                    )
                }

                if (currentCalculator != null) {
                    ExportScopeItem(
                        scope = ExportScope.CALCULATOR,
                        selected = config.scope == ExportScope.CALCULATOR,
                        onClick = { config = config.copy(scope = ExportScope.CALCULATOR) },
                        icon = Icons.Default.Calculate,
                        labelSuffix = " (${currentCalculator.displayName})"
                    )
                }
            }

            // Options
            SectionTitle("Options")
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                OptionSwitch(
                    label = "Include Profile Data",
                    checked = config.includeProfile,
                    onCheckedChange = { config = config.copy(includeProfile = it) }
                )
                OptionSwitch(
                    label = "Include Summary Stats",
                    checked = checked@ config.includeSummaryStats,
                    onCheckedChange = { config = config.copy(includeSummaryStats = it) }
                )
            }

            // Actions
            Button(
                onClick = { onExport(config) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate ${config.format.name}")
            }

            TextButton(
                onClick = onScheduleSettings,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Schedule Automatic Exports")
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun FormatCard(
    format: ExportFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                when(format) {
                    ExportFormat.PDF -> Icons.Default.PictureAsPdf
                    ExportFormat.CSV -> Icons.Default.TableChart
                    ExportFormat.JSON -> Icons.Default.Code
                },
                contentDescription = null
            )
            Spacer(Modifier.height(4.dp))
            Text(format.name, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ExportScopeItem(
    scope: ExportScope,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    labelSuffix: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text = scope.label + labelSuffix,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun OptionSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
