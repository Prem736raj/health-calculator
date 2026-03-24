package com.health.calculator.bmi.tracker.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.health.calculator.bmi.tracker.data.model.AssessmentComparison
import com.health.calculator.bmi.tracker.data.model.CriterionTrend
import com.health.calculator.bmi.tracker.data.model.MetabolicSyndromeRecord
import com.health.calculator.bmi.tracker.data.model.MetabolicTrendDirection

class MetabolicSyndromeTrackingRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "metabolic_syndrome_tracking", Context.MODE_PRIVATE
    )
    private val reminderPrefs: SharedPreferences = context.getSharedPreferences(
        "metabolic_syndrome_reminders", Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_RECORDS = "ms_records"
        private const val KEY_LAB_REMINDER_TIME = "lab_reminder_time"
        private const val KEY_LAB_REMINDER_ENABLED = "lab_reminder_enabled"
        private const val KEY_REMINDER_MONTHS = "lab_reminder_months"
    }

    fun saveRecord(record: MetabolicSyndromeRecord) {
        val records = getAllRecords().toMutableList()
        records.add(record)
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    fun getAllRecords(): List<MetabolicSyndromeRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<MetabolicSyndromeRecord>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRecordsSorted(): List<MetabolicSyndromeRecord> {
        return getAllRecords().sortedByDescending { it.timestamp }
    }

    fun getLatestRecord(): MetabolicSyndromeRecord? {
        return getRecordsSorted().firstOrNull()
    }

    fun getPreviousRecord(): MetabolicSyndromeRecord? {
        val sorted = getRecordsSorted()
        return if (sorted.size >= 2) sorted[1] else null
    }

    fun getRecordCount(): Int = getAllRecords().size

    fun deleteRecord(id: Long) {
        val records = getAllRecords().toMutableList()
        records.removeAll { it.id == id }
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    fun clearAllRecords() {
        prefs.edit().remove(KEY_RECORDS).apply()
    }

    fun getComparison(): AssessmentComparison? {
        val current = getLatestRecord() ?: return null
        val previous = getPreviousRecord()

        val trends = buildCriterionTrends(current, previous)

        val improvedCount = trends.count { it.trend == MetabolicTrendDirection.IMPROVED }
        val worsenedCount = trends.count { it.trend == MetabolicTrendDirection.WORSENED }
        val unchangedCount = trends.count { it.trend == MetabolicTrendDirection.UNCHANGED }

        val newlyNormal = trends.filter {
            it.previouslyMet == true && !it.currentlyMet
        }.map { it.name }

        val newlyAbnormal = trends.filter {
            it.previouslyMet == false && it.currentlyMet
        }.map { it.name }

        val overallTrend = when {
            previous == null -> MetabolicTrendDirection.NEW
            current.criteriaMet < previous.criteriaMet -> MetabolicTrendDirection.IMPROVED
            current.criteriaMet > previous.criteriaMet -> MetabolicTrendDirection.WORSENED
            else -> MetabolicTrendDirection.UNCHANGED
        }

        return AssessmentComparison(
            currentDate = current.dateTime,
            previousDate = previous?.dateTime,
            currentCriteriaMet = current.criteriaMet,
            previousCriteriaMet = previous?.criteriaMet,
            criterionTrends = trends,
            improvedCount = improvedCount,
            worsenedCount = worsenedCount,
            unchangedCount = unchangedCount,
            overallTrend = overallTrend,
            newlyNormalCriteria = newlyNormal,
            newlyAbnormalCriteria = newlyAbnormal
        )
    }

    private fun buildCriterionTrends(
        current: MetabolicSyndromeRecord,
        previous: MetabolicSyndromeRecord?
    ): List<CriterionTrend> {
        return listOf(
            buildSingleTrend(
                name = "Central Obesity",
                icon = "📏",
                currentValue = "%.1f cm".format(current.waistCm),
                previousValue = previous?.let { "%.1f cm".format(it.waistCm) },
                currentMet = current.waistMet,
                previousMet = previous?.waistMet,
                currentNumeric = current.waistCm,
                previousNumeric = previous?.waistCm,
                lowerIsBetter = true
            ),
            buildSingleTrend(
                name = "Blood Pressure",
                icon = "❤️",
                currentValue = "%.0f/%.0f".format(current.systolic, current.diastolic),
                previousValue = previous?.let { "%.0f/%.0f".format(it.systolic, it.diastolic) },
                currentMet = current.bpMet,
                previousMet = previous?.bpMet,
                currentNumeric = current.systolic,
                previousNumeric = previous?.systolic,
                lowerIsBetter = true
            ),
            buildSingleTrend(
                name = "Fasting Glucose",
                icon = "🍯",
                currentValue = "%.0f mg/dL".format(current.glucoseMgDl),
                previousValue = previous?.let { "%.0f mg/dL".format(it.glucoseMgDl) },
                currentMet = current.glucoseMet,
                previousMet = previous?.glucoseMet,
                currentNumeric = current.glucoseMgDl,
                previousNumeric = previous?.glucoseMgDl,
                lowerIsBetter = true
            ),
            buildSingleTrend(
                name = "Triglycerides",
                icon = "🩸",
                currentValue = "%.0f mg/dL".format(current.triglyceridesMgDl),
                previousValue = previous?.let { "%.0f mg/dL".format(it.triglyceridesMgDl) },
                currentMet = current.triglyceridesMet,
                previousMet = previous?.triglyceridesMet,
                currentNumeric = current.triglyceridesMgDl,
                previousNumeric = previous?.triglyceridesMgDl,
                lowerIsBetter = true
            ),
            buildSingleTrend(
                name = "HDL Cholesterol",
                icon = "💛",
                currentValue = "%.0f mg/dL".format(current.hdlMgDl),
                previousValue = previous?.let { "%.0f mg/dL".format(it.hdlMgDl) },
                currentMet = current.hdlMet,
                previousMet = previous?.hdlMet,
                currentNumeric = current.hdlMgDl,
                previousNumeric = previous?.hdlMgDl,
                lowerIsBetter = false // Higher HDL is better
            )
        )
    }

    private fun buildSingleTrend(
        name: String,
        icon: String,
        currentValue: String,
        previousValue: String?,
        currentMet: Boolean,
        previousMet: Boolean?,
        currentNumeric: Float,
        previousNumeric: Float?,
        lowerIsBetter: Boolean
    ): CriterionTrend {
        val trend = when {
            previousMet == null -> MetabolicTrendDirection.NEW
            previousMet && !currentMet -> MetabolicTrendDirection.IMPROVED
            !previousMet && currentMet -> MetabolicTrendDirection.WORSENED
            previousMet == currentMet -> {
                if (previousNumeric == null) MetabolicTrendDirection.UNCHANGED
                else {
                    val diff = currentNumeric - previousNumeric
                    when {
                        kotlin.math.abs(diff) < 0.5f -> MetabolicTrendDirection.UNCHANGED
                        lowerIsBetter && diff < 0 -> MetabolicTrendDirection.IMPROVED
                        lowerIsBetter && diff > 0 -> MetabolicTrendDirection.WORSENED
                        !lowerIsBetter && diff > 0 -> MetabolicTrendDirection.IMPROVED
                        !lowerIsBetter && diff < 0 -> MetabolicTrendDirection.WORSENED
                        else -> MetabolicTrendDirection.UNCHANGED
                    }
                }
            }
            else -> MetabolicTrendDirection.UNCHANGED
        }

        val changeDesc = when {
            previousNumeric == null -> "First measurement"
            else -> {
                val diff = currentNumeric - previousNumeric
                val direction = if (diff > 0) "+" else ""
                "$direction%.1f from last".format(diff)
            }
        }

        return CriterionTrend(
            name = name,
            icon = icon,
            currentValue = currentValue,
            previousValue = previousValue,
            currentlyMet = currentMet,
            previouslyMet = previousMet,
            trend = trend,
            changeDescription = changeDesc
        )
    }

    // === Lab Reminder Methods ===
    fun setLabReminder(enabled: Boolean, months: Int = 3) {
        val reminderTime = if (enabled) {
            System.currentTimeMillis() + (months * 30L * 24 * 60 * 60 * 1000)
        } else 0L

        reminderPrefs.edit()
            .putBoolean(KEY_LAB_REMINDER_ENABLED, enabled)
            .putLong(KEY_LAB_REMINDER_TIME, reminderTime)
            .putInt(KEY_REMINDER_MONTHS, months)
            .apply()
    }

    fun isLabReminderEnabled(): Boolean {
        return reminderPrefs.getBoolean(KEY_LAB_REMINDER_ENABLED, false)
    }

    fun getLabReminderTime(): Long {
        return reminderPrefs.getLong(KEY_LAB_REMINDER_TIME, 0L)
    }

    fun getReminderMonths(): Int {
        return reminderPrefs.getInt(KEY_REMINDER_MONTHS, 3)
    }
}
