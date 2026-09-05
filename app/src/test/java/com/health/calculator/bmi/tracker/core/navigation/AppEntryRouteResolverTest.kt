package com.health.calculator.bmi.tracker.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppEntryRouteResolverTest {

    @Test
    fun `widget aliases resolve to allow-listed app routes`() {
        assertEquals(Screen.WaterTracker.route, AppEntryRouteResolver.resolveTarget("water_tracker"))
        assertEquals(Screen.BmiCalculator.route, AppEntryRouteResolver.resolveTarget("bmi_calculator"))
        assertEquals(
            Screen.BloodPressureCalculator.route,
            AppEntryRouteResolver.resolveTarget("blood_pressure_checker")
        )
    }

    @Test
    fun `notification URI resolves to route`() {
        assertEquals(
            Screen.Reminders.route,
            AppEntryRouteResolver.resolve(dataUri = "healthapp://navigate/reminders")
        )
    }

    @Test
    fun `welcome back flag takes priority`() {
        assertEquals(
            "welcome_back",
            AppEntryRouteResolver.resolve(
                navigateTo = "water_tracker",
                showWelcomeBack = true
            )
        )
    }

    @Test
    fun `unknown external route is ignored`() {
        assertNull(AppEntryRouteResolver.resolveTarget("../../not-a-route"))
        assertNull(AppEntryRouteResolver.resolve(dataUri = "https://example.com/water_tracker"))
    }
}

