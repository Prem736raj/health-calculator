package com.health.calculator.bmi.tracker.data.model

import java.text.SimpleDateFormat
import java.util.*

enum class CalculatorType(
    val displayName: String,
    val shortName: String,
    val emoji: String,
    val key: String
) {
    BMI("BMI Calculator", "BMI", "⚖️", "bmi"),
    BMR("BMR Calculator", "BMR", "🔥", "bmr"),
    BLOOD_PRESSURE("Blood Pressure", "BP", "❤️", "bp"),
    WHR("Waist-to-Hip Ratio", "WHR", "📏", "whr"),
    WATER_INTAKE("Water Intake", "Water", "💧", "water"),
    METABOLIC_SYNDROME("Metabolic Syndrome", "MetS", "🏥", "metabolic"),
    BSA("Body Surface Area", "BSA", "👤", "bsa"),
    IBW("Ideal Body Weight", "IBW", "🏋️", "ibw"),
    CALORIE("Daily Calories", "Cal", "🍽️", "calorie"),
    HEART_RATE("Heart Rate Zones", "HR", "💓", "hr");

    companion object {
        fun fromKey(key: String): CalculatorType? =
            entries.find { it.key == key }
    }
}

enum class DateGroup(val label: String, val order: Int) {
    TODAY("Today", 0),
    YESTERDAY("Yesterday", 1),
    THIS_WEEK("This Week", 2),
    THIS_MONTH("This Month", 3),
    LAST_MONTH("Last Month", 4),
    OLDER("Older", 5);

    companion object {
        fun fromTimestamp(timestamp: Long): DateGroup {
            val now = Calendar.getInstance()
            val entry = Calendar.getInstance().apply { timeInMillis = timestamp }

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val yesterdayStart = (todayStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val lastMonthStart = (monthStart.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
            }

            return when {
                entry.timeInMillis >= todayStart.timeInMillis -> TODAY
                entry.timeInMillis >= yesterdayStart.timeInMillis -> YESTERDAY
                entry.timeInMillis >= weekStart.timeInMillis -> THIS_WEEK
                entry.timeInMillis >= monthStart.timeInMillis -> THIS_MONTH
                entry.timeInMillis >= lastMonthStart.timeInMillis -> LAST_MONTH
                else -> OLDER
            }
        }
    }
}

data class HistoryDisplayEntry(
    val id: Long,
    val calculatorType: CalculatorType,
    val primaryValue: String,
    val primaryLabel: String,
    val category: String?,
    val categoryColor: CategoryColor,
    val timestamp: Long,
    val details: Map<String, String>,
    val note: String? = null,
    val isSelected: Boolean = false
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

enum class CategoryColor {
    GREEN, YELLOW, ORANGE, RED, BLUE, GRAY;
}

data class GroupedHistoryEntries(
    val dateGroup: DateGroup,
    val entries: List<HistoryDisplayEntry>,
    val isCollapsed: Boolean = false
)

enum class HistorySortOption(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    VALUE_HIGHEST("Highest Value"),
    VALUE_LOWEST("Lowest Value"),
    TYPE_ALPHABETICAL("By Calculator Type")
}

data class HistoryFilter(
    val selectedTypes: Set<CalculatorType> = emptySet(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val selectedCategories: Set<String> = emptySet()
) {
    val isActive: Boolean
        get() = selectedTypes.isNotEmpty() || startDate != null ||
                endDate != null || selectedCategories.isNotEmpty()

    val activeFilterCount: Int
        get() {
            var count = 0
            if (selectedTypes.isNotEmpty()) count++
            if (startDate != null || endDate != null) count++
            if (selectedCategories.isNotEmpty()) count++
            return count
        }
}
