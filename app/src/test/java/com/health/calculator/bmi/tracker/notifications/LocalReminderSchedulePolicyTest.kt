package com.health.calculator.bmi.tracker.notification

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReminderSchedulePolicyTest {
    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun schedulesLaterTodayWhenTheTimeHasNotPassed() {
        val now = Instant.parse("2026-09-08T03:00:00Z") // 08:30 local
        val scheduled = LocalReminderSchedulePolicy.nextOccurrenceMillis(10, 15, now, zone)
        assertEquals(Instant.parse("2026-09-08T04:45:00Z").toEpochMilli(), scheduled)
    }

    @Test
    fun rollsToTomorrowAfterTheLocalTimeHasPassed() {
        val now = Instant.parse("2026-09-08T06:00:00Z") // 11:30 local
        val scheduled = LocalReminderSchedulePolicy.nextOccurrenceMillis(10, 15, now, zone)
        assertEquals(Instant.parse("2026-09-09T04:45:00Z").toEpochMilli(), scheduled)
    }

    @Test
    fun evaluatesLocalTimeAcrossDstTransitions() {
        val dstZone = ZoneId.of("America/New_York")
        val now = Instant.parse("2026-03-08T06:59:00Z") // 01:59 before spring-forward
        val scheduled = LocalReminderSchedulePolicy.nextOccurrenceMillis(2, 30, now, dstZone)
        val scheduledLocal = Instant.ofEpochMilli(scheduled).atZone(dstZone)
        assertTrue(scheduledLocal.toLocalDate().toString() == "2026-03-08")
        // 02:30 does not exist on the spring-forward day; java.time resolves
        // it to the next valid wall-clock instant (03:30) instead of drifting
        // by a fixed 24-hour interval.
        assertEquals(3, scheduledLocal.hour)
        assertEquals(30, scheduledLocal.minute)
    }
}
