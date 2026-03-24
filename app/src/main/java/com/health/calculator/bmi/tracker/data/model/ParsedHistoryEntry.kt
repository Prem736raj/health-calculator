package com.health.calculator.bmi.tracker.data.model

/**
 * Represents a parsed history entry with strongly typed numeric values,
 * suitable for calculations and reporting.
 */
data class ParsedHistoryEntry(
    val id: Long = 0,
    val calculatorKey: String,
    val primaryValue: Double,
    val primaryLabel: String,
    val secondaryValue: Double? = null,
    val secondaryLabel: String? = null,
    val category: String?,
    val timestamp: Long,
    val note: String?,
    val details: Map<String, String>
)
