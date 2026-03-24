package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Room entity representing a single calculation history record.
 */
@Entity(tableName = "history_entries") // Renamed to match the mapper's expectation if necessary, or keep as history_entries
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "calculator_key")
    val calculatorKey: String,

    @ColumnInfo(name = "result_value")
    val resultValue: String,

    @ColumnInfo(name = "result_label")
    val resultLabel: String,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "details_json")
    val detailsJson: String? = null,

    @ColumnInfo(name = "note")
    val note: String? = null
)

/**
 * Partial history entry for mapping calculator keys to their latest calculation timestamps.
 */
data class CalculatorLastCalc(
    @ColumnInfo(name = "calculator_key")
    val calculatorKey: String,
    @ColumnInfo(name = "last_calc")
    val lastCalcTime: Long
)
