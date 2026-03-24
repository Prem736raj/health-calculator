package com.health.calculator.bmi.tracker.data.model

import com.health.calculator.bmi.tracker.data.model.*

/**
 * Extension functions for HistoryEntry data conversion.
 */

fun HistoryEntry.toDisplayEntry(): HistoryDisplayEntry {
    return HistoryDisplayEntry(
        id = this.id,
        calculatorType = CalculatorType.fromKey(this.calculatorKey) ?: CalculatorType.BMI,
        primaryValue = this.resultValue,
        primaryLabel = this.resultLabel ?: "",
        category = this.category,
        categoryColor = mapCategoryColor(this.category, this.calculatorKey),
        timestamp = this.timestamp,
        details = parseDetails(this.detailsJson),
        note = this.note
    )
}

fun HistoryEntry.toParsedEntry(): ParsedHistoryEntry {
    val details = parseDetails(this.detailsJson)
    
    return ParsedHistoryEntry(
        id = this.id,
        calculatorKey = this.calculatorKey,
        primaryValue = this.resultValue.toDoubleOrNull() ?: 0.0,
        primaryLabel = this.resultLabel,
        secondaryValue = details["secondary_value"]?.toDoubleOrNull(),
        secondaryLabel = details["secondary_label"],
        category = this.category,
        timestamp = this.timestamp,
        details = details,
        note = this.note
    )
}

private fun mapCategoryColor(category: String?, calculatorKey: String): CategoryColor {
    if (category == null) return CategoryColor.GRAY

    val lowerCategory = category.lowercase()
    return when {
        lowerCategory.contains("normal") || lowerCategory.contains("optimal") ||
        lowerCategory.contains("low risk") || lowerCategory.contains("healthy") ->
            CategoryColor.GREEN

        lowerCategory.contains("overweight") || lowerCategory.contains("high normal") ||
        lowerCategory.contains("moderate") || lowerCategory.contains("mild") ||
        lowerCategory.contains("pre-hypertension") ->
            CategoryColor.YELLOW

        lowerCategory.contains("obese class i") || lowerCategory.contains("grade 1") ||
        lowerCategory.contains("caution") || lowerCategory.contains("stage 1") ->
            CategoryColor.ORANGE

        lowerCategory.contains("obese") || lowerCategory.contains("hypertension") ||
        lowerCategory.contains("high risk") || lowerCategory.contains("underweight") ||
        lowerCategory.contains("thinness") || lowerCategory.contains("emergency") ||
        lowerCategory.contains("stage 2") || lowerCategory.contains("crisis") ->
            CategoryColor.RED

        lowerCategory.contains("not present") || lowerCategory.contains("active") ->
            CategoryColor.BLUE

        else -> CategoryColor.GRAY
    }
}

private fun parseDetails(json: String?): Map<String, String> {
    if (json.isNullOrBlank()) return emptyMap()
    return try {
        if (json.contains("|")) {
            json.split("|").associate { pair ->
                val parts = pair.split(":", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim()
                else pair to ""
            }
        } else if (json.startsWith("{") && json.endsWith("}")) {
            json.substring(1, json.length - 1)
                .split(",")
                .associate { pair ->
                    val parts = pair.split(":", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim().removeSurrounding("\"")
                        val value = parts[1].trim().removeSurrounding("\"")
                        key to value
                    } else pair to ""
                }
        } else {
            emptyMap()
        }
    } catch (e: Exception) {
        emptyMap()
    }
}
