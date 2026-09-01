// app/src/main/java/com/health/calculator/bmi/tracker/presentation/components/BottomNavigationBar.kt

package com.health.calculator.bmi.tracker.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.core.navigation.BottomNavItem
import com.health.calculator.bmi.tracker.ui.theme.BottomNavShape
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = BottomNavShape,
                clip = false
            )
            .clip(BottomNavShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route

            // Subtle scale animation for selected item
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "navItemScale_${item.name}"
            )

            NavigationBarItem(
                modifier = Modifier.scale(scale),
                selected = isSelected,
                onClick = { onItemClick(item) },
                icon = {
                    BadgedBox(
                        badge = {
                            item.badgeCount?.let { count ->
                                if (count > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = if (count > 99) "99+" else count.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.contentDescription
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                )
            )
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    HealthCalculatorTheme {
        BottomNavigationBar(
            currentRoute = BottomNavItem.HOME.route,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarDarkPreview() {
    HealthCalculatorTheme(themeMode = com.health.calculator.bmi.tracker.data.model.ThemeMode.DARK) {
        BottomNavigationBar(
            currentRoute = BottomNavItem.HISTORY.route,
            onItemClick = {}
        )
    }
}
