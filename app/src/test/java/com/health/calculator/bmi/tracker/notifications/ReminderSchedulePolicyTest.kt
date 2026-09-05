package com.health.calculator.bmi.tracker.notifications

import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.ReminderSchedulePolicy
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePolicyTest {
    private val zone = TimeZone.getTimeZone("UTC")

    private fun at(day: Int, hour: Int, minute: Int): Calendar = Calendar.getInstance(zone).apply {
        set(2026, Calendar.JUNE, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }

    @Test
    fun waterWindowSchedulesNextIntervalWithinSameDay() {
        val now = at(10, 10, 15)
        val settings = WaterReminderSettings(
            isEnabled = true,
            startHour = 9,
            startMinute = 0,
            endHour = 18,
            endMinute = 0,
            frequencyMinutes = 60
        )

        val next = ReminderSchedulePolicy.nextWaterReminder(now, settings)

        assertEquals(11, next.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, next.get(Calendar.MINUTE))
        assertTrue(next.timeInMillis > now.timeInMillis)
    }

    @Test
    fun crossMidnightWindowContinuesAfterMidnight() {
        val now = at(10, 23, 10)
        val settings = WaterReminderSettings(
            isEnabled = true,
            startHour = 22,
            startMinute = 0,
            endHour = 6,
            endMinute = 0,
            frequencyMinutes = 120
        )

        val next = ReminderSchedulePolicy.nextWaterReminder(now, settings)

        assertEquals(0, next.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, next.get(Calendar.MINUTE))
        assertEquals(11, next.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun crossMidnightWindowKeepsAfterMidnightIntervalOnCurrentDay() {
        val now = at(11, 2, 10)
        val settings = WaterReminderSettings(
            isEnabled = true,
            startHour = 22,
            startMinute = 0,
            endHour = 6,
            endMinute = 0,
            frequencyMinutes = 120
        )

        val next = ReminderSchedulePolicy.nextWaterReminder(now, settings)

        assertEquals(4, next.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, next.get(Calendar.MINUTE))
        assertEquals(11, next.get(Calendar.DAY_OF_MONTH))
        assertTrue(next.timeInMillis > now.timeInMillis)
    }

    @Test
    fun outsideWindowStartsAtNextWindow() {
        val now = at(10, 20, 0)
        val settings = WaterReminderSettings(
            isEnabled = true,
            startHour = 9,
            startMinute = 0,
            endHour = 18,
            endMinute = 0,
            frequencyMinutes = 60
        )

        val next = ReminderSchedulePolicy.nextWaterReminder(now, settings)

        assertEquals(11, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, next.get(Calendar.MINUTE))
    }

    @Test
    fun weeklyOccurrenceMovesToFollowingWeekAtExactTime() {
        val now = at(8, 9, 0)
        val next = ReminderSchedulePolicy.nextWeeklyOccurrence(
            now = now,
            dayOfWeek = Calendar.MONDAY,
            hour = 9,
            minute = 0
        )

        assertEquals(Calendar.MONDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(15, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
        assertTrue(next.timeInMillis > now.timeInMillis)
    }
}
