// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRBodyFatInput.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedInputField
import com.health.calculator.bmi.tracker.ui.utils.ShakeController

@Composable
fun BMRBodyFatInput(
    visible: Boolean,
    bodyFatText: String,
    onBodyFatChange: (String) -> Unit,
    errorMessage: String?,
    shakeController: ShakeController,
    modifier: Modifier = Modifier
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(tween(200)) + shrinkVertically(
            animationSpec = tween(200),
            shrinkTowards = Alignment.Top
        ),
        modifier = modifier
    ) {
        Column {
            AnimatedInputField(
                value = bodyFatText,
                onValueChange = onBodyFatChange,
                label = "Body Fat Percentage",
                icon = Icons.Outlined.Percent,
                errorMessage = errorMessage,
                shakeController = shakeController,
                suffix = "%"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // "Don't know?" help link
            TextButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier.align(Alignment.Start),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Don't know your body fat %?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showHelpDialog) {
        BodyFatHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
private fun BodyFatHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Estimating Body Fat %",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "There are several ways to estimate your body fat percentage:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                BodyFatMethod(
                    emoji = "📏",
                    title = "Tape Measure Method",
                    description = "Use body measurements (waist, neck, hip) with the U.S. Navy formula. Available online."
                )

                Spacer(modifier = Modifier.height(8.dp))

                BodyFatMethod(
                    emoji = "⚖️",
                    title = "Smart Scale",
                    description = "Many body composition scales use bioelectrical impedance to estimate body fat."
                )

                Spacer(modifier = Modifier.height(8.dp))

                BodyFatMethod(
                    emoji = "📐",
                    title = "Skinfold Calipers",
                    description = "Pinch test at multiple body sites. Best done by a trained professional."
                )

                Spacer(modifier = Modifier.height(8.dp))

                BodyFatMethod(
                    emoji = "🏥",
                    title = "DEXA Scan",
                    description = "Most accurate method. Available at medical facilities and some gyms."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // General ranges
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "General Body Fat Ranges:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Men:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BodyFatRange("Essential Fat", "2-5%")
                        BodyFatRange("Athletes", "6-13%")
                        BodyFatRange("Fitness", "14-17%")
                        BodyFatRange("Average", "18-24%")
                        BodyFatRange("Above Average", "25%+")

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Women:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BodyFatRange("Essential Fat", "10-13%")
                        BodyFatRange("Athletes", "14-20%")
                        BodyFatRange("Fitness", "21-24%")
                        BodyFatRange("Average", "25-31%")
                        BodyFatRange("Above Average", "32%+")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💡 Tip: If you don't know your body fat %, use the Mifflin-St Jeor formula instead — it doesn't require it and is highly accurate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun BodyFatMethod(
    emoji: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BodyFatRange(label: String, range: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
    }
}
