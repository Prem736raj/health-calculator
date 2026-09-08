package com.health.calculator.bmi.tracker.data.export

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvExportHelper(@ApplicationContext private val context: Context) {

    fun exportAll(
        entries: List<HistoryDisplayEntry>,
        onProgress: (Float) -> Unit
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "HealthData_All_$timestamp.csv")

        onProgress(0.1f)

        BufferedWriter(FileWriter(file)).use { writer ->
            writeMetadata(writer)
            writer.write(CsvExportSchema.UNIVERSAL_HEADER)
            writer.newLine()

            val total = entries.size
            entries.forEachIndexed { index, entry ->
                writer.write(buildCsvRow(entry))
                writer.newLine()
                if (total > 0) {
                    onProgress(0.1f + 0.85f * (index + 1) / total)
                }
            }
        }

        onProgress(1f)
        return file
    }

    fun exportByCalculator(
        entries: List<HistoryDisplayEntry>,
        calculatorType: CalculatorType,
        onProgress: (Float) -> Unit
    ): File {
        val filtered = entries.filter { it.calculatorType == calculatorType }
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "${calculatorType.shortName}_Data_$timestamp.csv")

        onProgress(0.1f)

        BufferedWriter(FileWriter(file)).use { writer ->
            writeMetadata(writer)
            writer.write(CsvExportSchema.UNIVERSAL_HEADER)
            writer.newLine()

            val total = filtered.size
            filtered.forEachIndexed { index, entry ->
                writer.write(CsvExportSchema.buildRow(entry))
                writer.newLine()
                if (total > 0) {
                    onProgress(0.1f + 0.85f * (index + 1) / total)
                }
            }
        }

        onProgress(1f)
        return file
    }

    private fun buildCsvRow(entry: HistoryDisplayEntry): String {
        return CsvExportSchema.buildRow(entry)
    }

    private fun writeMetadata(writer: BufferedWriter) {
        ExportDisclosurePolicy.csvMetadataRows().forEach { row ->
            writer.write(row)
            writer.newLine()
        }
    }
}

/** Stable schema shared by all CSV exports, regardless of calculator type. */
internal object CsvExportSchema {
    const val UNIVERSAL_HEADER =
        "Calculator Type,Date,Time,Primary Value,Unit,Category," +
            "Detail 1 Key,Detail 1 Value,Detail 2 Key,Detail 2 Value," +
            "Detail 3 Key,Detail 3 Value,Note"

    fun buildRow(entry: HistoryDisplayEntry): String {
        val details = entry.details.entries.take(3).toList()
        val values = buildList {
            add(entry.calculatorType.displayName)
            add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(entry.timestamp)))
            add(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp)))
            add(entry.primaryValue)
            add(entry.primaryLabel)
            add(entry.category.orEmpty())
            repeat(3) { index ->
                add(details.getOrNull(index)?.key.orEmpty())
                add(details.getOrNull(index)?.value.orEmpty())
            }
            add(entry.note.orEmpty())
        }
        return values.joinToString(",") { escapeCsv(it) }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}
