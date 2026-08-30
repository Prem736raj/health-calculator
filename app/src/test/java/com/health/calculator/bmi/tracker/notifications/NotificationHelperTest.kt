package com.health.calculator.bmi.tracker.notifications

import com.health.calculator.bmi.tracker.data.models.QuietHours
import com.health.calculator.bmi.tracker.data.models.Reminder
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import org.junit.Assert.*
import org.junit.Test

class NotificationHelperTest {

    @Test
    fun testQuietHoursCalculation() {
        val quietHours = QuietHours(
            isEnabled = true,
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0
        )

        // 23:30 is during quiet hours (overnight)
        assertTrue(quietHours.isInQuietPeriod(23, 30))
        // 03:00 is during quiet hours
        assertTrue(quietHours.isInQuietPeriod(3, 0))
        // 12:00 is outside quiet hours
        assertFalse(quietHours.isInQuietPeriod(12, 0))
    }

    @Test
    fun testReminderTimesAndDaysParsing() {
        val reminder = Reminder(
            id = "test-1",
            category = ReminderCategory.WATER_INTAKE.name,
            title = "Hydration Reminder",
            message = "Drink water",
            times = "08:00,12:00,18:00",
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true
        )

        val times = reminder.getTimesList()
        assertEquals(3, times.size)
        assertEquals("08:00", times[0])
        assertEquals("12:00", times[1])
        assertEquals("18:00", times[2])

        val days = reminder.getDaysList()
        assertEquals(5, days.size)
        assertTrue(days.contains(1)) // Monday
        assertTrue(days.contains(5)) // Friday
        assertFalse(days.contains(6)) // Saturday
    }

    @Test
    fun testReminderDisabledState() {
        val reminder = Reminder(
            id = "test-2",
            category = ReminderCategory.CUSTOM.name,
            title = "Disabled",
            message = "Should not ring",
            times = "09:00",
            daysOfWeek = "1",
            isEnabled = false
        )

        assertFalse(reminder.isEnabled)
    }
}
