// app/src/main/java/com/health/calculator/bmi/tracker/core/navigation/BottomNavItem.kt

package com.health.calculator.bmi.tracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents each item in the bottom navigation bar.
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
    val badgeCount: Int? = null
) {
    HOME(
        route = Screen.Home.route,
        title = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Home Dashboard"
    ),
    HISTORY(
        route = Screen.History.route,
        title = "History",
        selectedIcon = Icons.Rounded.History,
        unselectedIcon = Icons.Outlined.History,
        contentDescription = "Calculation History"
    ),
    PROFILE(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "User Profile"
    ),
    SETTINGS(
        route = Screen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        contentDescription = "App Settings"
    );

    companion object {
        /**
         * Find BottomNavItem by route string
         */
        fun fromRoute(route: String?): BottomNavItem? {
            return entries.find { it.route == route }
        }
    }
}
