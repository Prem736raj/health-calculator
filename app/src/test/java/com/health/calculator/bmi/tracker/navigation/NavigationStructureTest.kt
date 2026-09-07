package com.health.calculator.bmi.tracker.navigation

import com.health.calculator.bmi.tracker.core.navigation.Screen
import com.health.calculator.bmi.tracker.core.navigation.onboardingDestination
import com.health.calculator.bmi.tracker.core.navigation.rootOwnsSystemBarInsets
import com.health.calculator.bmi.tracker.ui.screens.onboarding.OnboardingStartAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun `root only applies top inset to destinations without their own scaffold`() {
        assertTrue(rootOwnsSystemBarInsets(Screen.Home.route))
        assertTrue(rootOwnsSystemBarInsets(Screen.Splash.route))
        assertTrue(rootOwnsSystemBarInsets(null))
        assertFalse(rootOwnsSystemBarInsets(Screen.Track.route))
        assertFalse(rootOwnsSystemBarInsets(Screen.Profile.route))
        assertFalse(rootOwnsSystemBarInsets(Screen.BmiCalculator.route))
    }

    @Test
    fun onboardingChoicesOpenUsefulOptionalDestinations() {
        assertEquals(Screen.WaterTracker.route, onboardingDestination(OnboardingStartAction.WATER))
        assertEquals(Screen.WeightTracking.route, onboardingDestination(OnboardingStartAction.WEIGHT))
        assertEquals(Screen.Track.route, onboardingDestination(OnboardingStartAction.STEPS))
        assertEquals(Screen.BmiCalculator.route, onboardingDestination(OnboardingStartAction.CALCULATORS))
    }
}
