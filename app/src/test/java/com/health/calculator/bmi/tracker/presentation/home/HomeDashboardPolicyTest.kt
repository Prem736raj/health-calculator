package com.health.calculator.bmi.tracker.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDashboardPolicyTest {

    @Test
    fun `home shows no more than two insight previews`() {
        val insights = listOf("first", "second", "third")

        assertEquals(listOf("first", "second"), HomeDashboardPolicy.insightPreview(insights))
    }

    @Test
    fun `home preserves a short insight list`() {
        val insights = listOf("only")

        assertEquals(insights, HomeDashboardPolicy.insightPreview(insights))
    }

    @Test
    fun `daily dashboard keeps the four retention metrics in order`() {
        assertEquals(
            listOf("steps", "water", "weight", "calories"),
            HomeDashboardPolicy.dailyMetricIds
        )
    }
}
