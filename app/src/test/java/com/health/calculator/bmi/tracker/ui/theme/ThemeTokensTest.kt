package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTokensTest {

    @Test
    fun `brand primary pairs meet body text contrast target`() {
        assertTrue("light primary", contrastRatio(PrimaryLight, OnPrimaryLight) >= 4.5)
        assertTrue("light primary container", contrastRatio(PrimaryContainerLight, OnPrimaryContainerLight) >= 4.5)
        assertTrue("dark primary", contrastRatio(PrimaryDark, OnPrimaryDark) >= 4.5)
        assertTrue("dark primary container", contrastRatio(PrimaryContainerDark, OnPrimaryContainerDark) >= 4.5)
        assertTrue("hero start", contrastRatio(WellnessPalette.HeroStart, WellnessPalette.OnHero) >= 4.5)
        assertTrue("hero end", contrastRatio(WellnessPalette.HeroEnd, WellnessPalette.OnHero) >= 4.5)
    }

    @Test
    fun `wellness spacing tokens stay positive and ordered`() {
        assertTrue(HealthSpacing.xSmall.value > 0f)
        assertTrue(HealthSpacing.small < HealthSpacing.medium)
        assertTrue(HealthSpacing.medium < HealthSpacing.large)
        assertTrue(HealthSpacing.large < HealthSpacing.xLarge)
        assertEquals(16f, HealthSpacing.screenHorizontal.value, 0.001f)
    }

    @Test
    fun `legacy accent aliases point at semantic palette`() {
        assertEquals(HealthColors.Healthy, HealthGreen)
        assertEquals(HealthColors.Warning, HealthYellow)
        assertEquals(HealthColors.Danger, HealthRed)
        assertEquals(HealthColors.Info, HealthBlue)
    }

    @Test
    fun `visual roles are intentionally distinct`() {
        assertNotEquals(WellnessPalette.ActionLight, WellnessPalette.WarmAccentLight)
        assertNotEquals(WellnessPalette.ActionDark, WellnessPalette.WarmAccentDark)
        assertNotEquals(WellnessPalette.OnTrackLight, WellnessPalette.ActionLight)
        assertNotEquals(WellnessPalette.OnTrackDark, WellnessPalette.ActionDark)
    }

    @Test
    fun `measured values use the dedicated numeric family`() {
        assertEquals(WellnessMetricFontFamily, WellnessMetricTextStyle.fontFamily)
        assertNotEquals(WellnessDisplayFontFamily, InterFontFamily)
    }

    @Test
    fun `component tiers keep hierarchy and touch targets explicit`() {
        assertTrue(HealthSpacing.touchTarget.value >= 48f)
        assertTrue(HealthElevation.hero > HealthElevation.metric)
        assertEquals(HealthElevation.row.value, 0f, 0.001f)
        assertNotEquals(WellnessPalette.HeroStart, WellnessPalette.MetricSurfaceLight)
        assertNotEquals(WellnessPalette.HeroEnd, WellnessPalette.QuietSurfaceDark)
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = luminance(first)
        val secondLuminance = luminance(second)
        val lighter = maxOf(firstLuminance, secondLuminance)
        val darker = minOf(firstLuminance, secondLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val value = channel.toDouble()
            return if (value <= 0.03928) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }

        return (0.2126 * linear(color.red)) +
            (0.7152 * linear(color.green)) +
            (0.0722 * linear(color.blue))
    }
}
