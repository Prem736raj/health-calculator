package com.health.calculator.bmi.tracker.analytics

import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductAnalyticsPolicyTest {

    @Test
    fun eventNamesAreUniqueAndSafeForAnalytics() {
        val events = ProductAnalyticsEvent.entries

        assertEquals(events.size, events.map { it.eventName }.toSet().size)
        assertTrue(events.all { ProductAnalyticsPolicy.isSafeEventName(it.eventName) })
        assertTrue(events.all { it.eventName.matches(Regex("[a-z][a-z0-9_]{2,39}")) })
    }

    @Test
    fun onlyAllowlistedValuesSurviveSanitization() {
        val safe = ProductAnalyticsPolicy.sanitize(
            event = ProductAnalyticsEvent.CALCULATOR_COMPLETED,
            parameters = mapOf(
                "calculator_id" to "BMI",
                "entry_point" to "calculators",
                "result" to "22.4",
                "free_text" to "user note"
            )
        )

        assertEquals(
            mapOf("calculator_id" to "bmi", "entry_point" to "calculators"),
            safe
        )
    }

    @Test
    fun malformedOrSensitiveValuesAreDroppedWithoutBreakingEvent() {
        val safe = ProductAnalyticsPolicy.sanitize(
            event = ProductAnalyticsEvent.WATER_LOGGED,
            parameters = mapOf(
                "water_ml" to "500",
                "source_override" to "https://example.test/prompt",
                "source" to "500 ml"
            )
        )

        assertTrue(safe.isEmpty())
        assertTrue(ProductAnalyticsPolicy.forbiddenParameterNames.contains("weight"))
        assertTrue(ProductAnalyticsPolicy.forbiddenParameterNames.contains("prompt"))
        assertFalse(ProductAnalyticsPolicy.isSafeEventName("health_value"))
    }

    @Test
    fun requiredGrowthEventsRemainInTheContract() {
        val names = ProductAnalyticsEvent.entries.map { it.eventName }.toSet()

        assertTrue(names.containsAll(setOf(
            "app_opened",
            "onboarding_completed",
            "calculator_completed",
            "tracker_opened",
            "water_logged",
            "weight_logged",
            "health_connect_connected",
            "insight_opened",
            "ai_assistant_opened",
            "weekly_report_opened",
            "reminder_enabled"
        )))
    }

    @Test
    fun onboardingActionUsesOnlySmallNonSensitiveVocabulary() {
        assertEquals(
            mapOf("action" to "water"),
            ProductAnalyticsPolicy.sanitize(
                ProductAnalyticsEvent.ONBOARDING_ACTION_SELECTED,
                mapOf("action" to "WATER", "weight" to "72")
            )
        )
    }
}
