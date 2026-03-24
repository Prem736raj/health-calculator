// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRFormulaSelector.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BMRFormula

@Composable
fun BMRFormulaSelector(
    selectedFormula: BMRFormula,
    onFormulaSelected: (BMRFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInfoDialog by remember { mutableStateOf<BMRFormula?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calculation Formula",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable formula chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(BMRFormula.entries.toList()) { formula ->
                FormulaChip(
                    formula = formula,
                    isSelected = formula == selectedFormula,
                    onClick = { onFormulaSelected(formula) },
                    onInfoClick = { showInfoDialog = formula }
                )
            }
        }

        // Selected formula description
        Spacer(modifier = Modifier.height(10.dp))

        AnimatedContent(
            targetState = selectedFormula,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { -it / 4 })
            },
            label = "formulaDescription"
        ) { formula ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formula.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Info dialog
    showInfoDialog?.let { formula ->
        FormulaInfoDialog(
            formula = formula,
            onDismiss = { showInfoDialog = null }
        )
    }
}

@Composable
private fun FormulaChip(
    formula: BMRFormula,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "chipContent"
    )
    val borderColor = if (isSelected) Color.Transparent
    else MaterialTheme.colorScheme.outlineVariant

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chipScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formula.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (formula.requiresBodyFat) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.Percent,
                        contentDescription = "Requires body fat %",
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tag: year or "Recommended"
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (formula.isRecommended) {
                    if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    else Color(0xFF4CAF50).copy(alpha = 0.12f)
                } else {
                    contentColor.copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = formula.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (formula.isRecommended) FontWeight.Bold else FontWeight.Normal,
                    color = if (formula.isRecommended) {
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else Color(0xFF4CAF50)
                    } else contentColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun FormulaInfoDialog(
    formula: BMRFormula,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formula.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (formula.isRecommended) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⭐ Recommended",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Year: ${formula.year}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formula.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                if (formula.requiresBodyFat) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "This formula requires your body fat percentage for accurate results.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
