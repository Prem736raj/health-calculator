package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.*

class CsvExportHelper(private val context: Context) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportAll(
        entries: List<HistoryDisplayEntry>,
        onProgress: (Float) -> Unit
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "HealthData_All_$timestamp.csv")

        onProgress(0.1f)

        BufferedWriter(FileWriter(file)).use { writer ->
            // Universal header
            writer.write("Calculator Type,Date,Time,Primary Value,Unit,Category,")
            writer.write("Detail 1 Key,Detail 1 Value,Detail 2 Key,Detail 2 Value,")
            writer.write("Detail 3 Key,Detail 3 Value,Note")
            writer.newLine()

            val total = entries.size
            entries.forEachIndexed { index, entry ->
                writer.write(buildCsvRow(entry))
                writer.newLine()
                onProgress(0.1f + 0.85f * (index + 1) / total)
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
            // Type-specific headers
            val headers = getHeadersForType(calculatorType)
            writer.write(headers)
            writer.newLine()

            val total = filtered.size
            filtered.forEachIndexed { index, entry ->
                writer.write(buildTypedCsvRow(entry, calculatorType))
                writer.newLine()
                onProgress(0.1f + 0.85f * (index + 1) / total)
            }
        }

        onProgress(1f)
        return file
    }

    private fun buildCsvRow(entry: HistoryDisplayEntry): String {
        val details = entry.details.entries.toList()
        val sb = StringBuilder()

        sb.append(escapeCsv(entry.calculatorType.displayName)).append(",")
        sb.append(escapeCsv(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(entry.timestamp)))).append(",")
        sb.append(escapeCsv(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp)))).append(",")
        sb.append(escapeCsv(entry.primaryValue)).append(",")
        sb.append(escapeCsv(entry.primaryLabel)).append(",")
        sb.append(escapeCsv(entry.category ?: "")).append(",")

        // Up to 3 detail pairs
        for (i in 0..2) {
            if (i < details.size) {
                sb.append(escapeCsv(details[i].key)).append(",")
                sb.append(escapeCsv(details[i].value)).append(",")
            } else {
                sb.append(",,")
            }
        }

        sb.append(escapeCsv(entry.note ?: ""))

        return sb.toString()
    }

    private fun buildTypedCsvRow(entry: HistoryDisplayEntry, type: CalculatorType): String {
        val sb = StringBuilder()
        val dateStr = dateFormatter.format(Date(entry.timestamp))

        sb.append(escapeCsv(dateStr)).append(",")
        sb.append(escapeCsv(entry.primaryValue)).append(",")
        sb.append(escapeCsv(entry.category ?: "")).append(",")

        entry.details.values.forEach { value ->
            sb.append(escapeCsv(value)).append(",")
        }

        sb.append(escapeCsv(entry.note ?: ""))

        return sb.toString()
    }

    private fun getHeadersForType(type: CalculatorType): String {
        val baseHeaders = "Date & Time,Value,Category"
        val typeHeaders = when (type) {
            CalculatorType.BMI -> ",Weight,Height,Age,Gender"
            CalculatorType.BMR -> ",Formula,TDEE,Activity Level,Weight"
            CalculatorType.BLOOD_PRESSURE -> ",Systolic,Diastolic,Pulse,Position,Arm"
            CalculatorType.WHR -> ",Waist,Hip,WHtR,Body Shape"
            CalculatorType.WATER_INTAKE -> ",Goal,Consumed,Percentage"
            CalculatorType.METABOLIC_SYNDROME -> ",Criteria Met,Waist,BP,Glucose,Triglycerides,HDL"
            CalculatorType.BSA -> ",Formula,Weight,Height"
            CalculatorType.IBW -> ",Formula,Current Weight,Difference"
            CalculatorType.CALORIE -> ",TDEE,Goal Calories,Activity Level"
            CalculatorType.HEART_RATE -> ",Max HR,Resting HR,Formula,VO2 Max"
        }
        return "$baseHeaders$typeHeaders,Note"
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }
}
