// app/src/main/java/com/health/calculator/bmi/tracker/core/navigation/BottomNavItem.kt

package com.health.calculator.bmi.tracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
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
    TRACK(
        route = Screen.Track.route,
        title = "Track",
        selectedIcon = Icons.Rounded.DirectionsWalk,
        unselectedIcon = Icons.Outlined.DirectionsWalk,
        contentDescription = "Daily tracking"
    ),
    CALCULATORS(
        route = Screen.Calculators.route,
        title = "Calculate",
        selectedIcon = Icons.Rounded.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        contentDescription = "Health calculators"
    ),
    INSIGHTS(
        route = Screen.Insights.route,
        title = "Insights",
        selectedIcon = Icons.Rounded.Insights,
        unselectedIcon = Icons.Outlined.Insights,
        contentDescription = "Wellness insights"
    ),
    PROFILE(
        route = Screen.Profile.route,
        title = "Profile",
        selectedIcon = Icons.Rounded.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "User Profile"
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
