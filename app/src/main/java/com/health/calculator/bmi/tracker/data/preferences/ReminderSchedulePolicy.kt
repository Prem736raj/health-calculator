package com.health.calculator.bmi.tracker.data.preferences

import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import java.util.Calendar

/**
 * Pure calendar rules shared by reminder schedulers and their receivers.
 * Keeping the window math here prevents one-shot alarms and "behind schedule"
 * nudges from disagreeing, especially for windows that cross midnight.
 */
object ReminderSchedulePolicy {
    const val MINUTES_PER_DAY = 24 * 60

    fun windowDurationMinutes(startMinutes: Int, endMinutes: Int): Int = when {
        endMinutes > startMinutes -> endMinutes - startMinutes
        endMinutes < startMinutes -> MINUTES_PER_DAY - startMinutes + endMinutes
        else -> 0
    }

    /** Minutes elapsed from the window start, or null while outside the window. */
    fun elapsedInWindow(currentMinutes: Int, startMinutes: Int, endMinutes: Int): Int? {
        val duration = windowDurationMinutes(startMinutes, endMinutes)
        if (duration <= 0) return null
        val elapsed = (currentMinutes - startMinutes + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return elapsed.takeIf { it < duration }
    }

    /** Return the next local-time water reminder strictly after [now]. */
    fun nextWaterReminder(now: Calendar, settings: WaterReminderSettings): Calendar {
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = settings.startHour * 60 + settings.startMinute
        val endMinutes = settings.endHour * 60 + settings.endMinute
        val duration = windowDurationMinutes(
            startMinutes,
            endMinutes
        )
        val frequency = settings.frequencyMinutes.coerceAtLeast(1)
        val next = (now.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val elapsed = elapsedInWindow(currentMinutes, startMinutes, endMinutes)
        if (elapsed == null) {
            // Outside a window, the next occurrence is today's start when
            // the clock is before it, otherwise tomorrow's start. This also
            // handles the gap before a cross-midnight window begins.
            if (currentMinutes >= startMinutes) next.add(Calendar.DAY_OF_YEAR, 1)
            next.set(Calendar.HOUR_OF_DAY, settings.startHour)
            next.set(Calendar.MINUTE, settings.startMinute)
        } else {
            // For a cross-midnight window, an after-midnight reading belongs
            // to yesterday's window start. Keeping that anchor date avoids
            // incorrectly moving a 04:00 reminder to the following day.
            val windowStart = (next.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (startMinutes > endMinutes && currentMinutes < endMinutes) {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
            }
            val nextElapsed = ((elapsed / frequency) + 1) * frequency
            if (nextElapsed >= duration) {
                windowStart.add(Calendar.DAY_OF_YEAR, 1)
                next.timeInMillis = windowStart.timeInMillis
                next.set(Calendar.HOUR_OF_DAY, settings.startHour)
                next.set(Calendar.MINUTE, settings.startMinute)
            } else {
                val absoluteMinutes = startMinutes + nextElapsed
                windowStart.add(Calendar.DAY_OF_YEAR, absoluteMinutes / MINUTES_PER_DAY)
                next.timeInMillis = windowStart.timeInMillis
                next.set(Calendar.HOUR_OF_DAY, (absoluteMinutes % MINUTES_PER_DAY) / 60)
                next.set(Calendar.MINUTE, absoluteMinutes % 60)
            }
        }

        if (next.timeInMillis <= now.timeInMillis) {
            next.add(Calendar.MINUTE, frequency)
        }
        return next
    }

    /** Return the next local-time weekly occurrence strictly after [now]. */
    fun nextWeeklyOccurrence(
        now: Calendar,
        dayOfWeek: Int,
        hour: Int,
        minute: Int
    ): Calendar {
        val targetDay = dayOfWeek.coerceIn(Calendar.SUNDAY, Calendar.SATURDAY)
        val daysUntilTarget = (targetDay - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, daysUntilTarget)
        }
        if (next.timeInMillis <= now.timeInMillis) {
            next.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return next
    }
}
