package com.health.calculator.bmi.tracker.navigation

import com.health.calculator.bmi.tracker.core.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStructureTest {
    @Test
    fun primaryNavigationUsesFiveFocusedAreas() {
        assertEquals(
            listOf(
                Screen.Home.route,
                Screen.Track.route,
                Screen.Calculators.route,
                Screen.Insights.route,
                Screen.Profile.route
            ),
            Screen.bottomNavRoutes
        )
        assertTrue(Screen.isBottomNavRoute(Screen.Track.route))
        assertTrue(!Screen.isBottomNavRoute(Screen.History.route))
        assertTrue(!Screen.isBottomNavRoute(Screen.Settings.route))
    }
}
