package com.health.calculator.bmi.tracker.tracking

import com.health.calculator.bmi.tracker.data.repository.FoodLogDayPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodLogDayPolicyTest {
    @Test
    fun onlyAChangedCalendarDateResetsTheDailyLog() {
        assertFalse(FoodLogDayPolicy.needsReset("2026-09-08", "2026-09-08"))
        assertTrue(FoodLogDayPolicy.needsReset("2026-09-07", "2026-09-08"))
        assertTrue(FoodLogDayPolicy.needsReset("", "2026-09-08"))
    }
}
