package com.health.calculator.bmi.tracker.engagement

import com.health.calculator.bmi.tracker.domain.engagement.WellnessEngagementPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WellnessEngagementPolicyTest {
    @Test
    fun reengagementFeaturesAreOptInByDefault() {
        assertFalse(WellnessEngagementPolicy.DEFAULT_INACTIVITY_NOTIFICATIONS_ENABLED)
        assertFalse(WellnessEngagementPolicy.DEFAULT_STREAK_PROTECTION_ENABLED)
    }

    @Test
    fun weeklySnapshotUsesRhythmLanguageInsteadOfGrades() {
        assertEquals("Strong logging rhythm", WellnessEngagementPolicy.weeklyRhythmLabel("A"))
        assertEquals("A lighter logging week", WellnessEngagementPolicy.weeklyRhythmLabel("F"))
        assertTrue(
            WellnessEngagementPolicy.weeklyRhythmMessage("F")
                .contains("does not erase your progress")
        )
        assertFalse(WellnessEngagementPolicy.weeklyRhythmMessage("A").contains("health goals"))
    }

    @Test
    fun streakCopyIsOptionalAndNonPunitive() {
        assertTrue(WellnessEngagementPolicy.streakReminderTitle(7).contains("gentle"))
        val message = WellnessEngagementPolicy.streakReminderMessage("water")
        assertTrue(message.contains("If it fits your day"))
        assertTrue(message.contains("Skipping today is okay"))
        assertFalse(message.contains("at risk"))
    }
}
