package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class JsonExportHelper(private val context: Context) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun exportAll(
        entries: List<HistoryDisplayEntry>,
        profileData: Map<String, String>? = null,
        onProgress: (Float) -> Unit
    ): File {
        onProgress(0.1f)

        val rootObject = JSONObject().apply {
            put("export_version", 1)
            put("app_name", "Health Calculator: BMI Tracker")
            put("app_package", "com.health.calculator.bmi.tracker")
            put("exported_at", dateFormatter.format(Date()))
            put("total_entries", entries.size)

            // Profile data
            profileData?.let { profile ->
                put("profile", JSONObject().apply {
                    profile.forEach { (key, value) -> put(key, value) }
                })
            }

            // Entries grouped by type
            val grouped = entries.groupBy { it.calculatorType }
            val calculatorsArray = JSONArray()

            var processedCount = 0
            val totalCount = entries.size

            grouped.forEach { (type, typeEntries) ->
                val calcObject = JSONObject().apply {
                    put("calculator_type", type.key)
                    put("calculator_name", type.displayName)
                    put("entry_count", typeEntries.size)

                    val entriesArray = JSONArray()
                    typeEntries.forEach { entry ->
                        entriesArray.put(entryToJson(entry))
                        processedCount++
                        onProgress(0.1f + 0.8f * processedCount / totalCount)
                    }
                    put("entries", entriesArray)

                    // Summary stats
                    val values = typeEntries.mapNotNull { it.primaryValue.toDoubleOrNull() }
                    if (values.isNotEmpty()) {
                        put("statistics", JSONObject().apply {
                            put("count", values.size)
                            put("average", "%.2f".format(values.average()))
                            put("min", "%.2f".format(values.min()))
                            put("max", "%.2f".format(values.max()))
                            put("latest", typeEntries.first().primaryValue)
                        })
                    }
                }
                calculatorsArray.put(calcObject)
            }

            put("calculators", calculatorsArray)
        }

        onProgress(0.9f)

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "HealthData_Backup_$timestamp.json")

        FileWriter(file).use { writer ->
            writer.write(rootObject.toString(2))
        }

        onProgress(1f)
        return file
    }

    fun exportFiltered(
        entries: List<HistoryDisplayEntry>,
        filterDescription: String,
        onProgress: (Float) -> Unit
    ): File {
        onProgress(0.1f)

        val rootObject = JSONObject().apply {
            put("export_version", 1)
            put("app_name", "Health Calculator: BMI Tracker")
            put("exported_at", dateFormatter.format(Date()))
            put("filter_applied", filterDescription)
            put("total_entries", entries.size)

            val entriesArray = JSONArray()
            entries.forEachIndexed { index, entry ->
                entriesArray.put(entryToJson(entry))
                onProgress(0.1f + 0.8f * (index + 1) / entries.size)
            }
            put("entries", entriesArray)
        }

        onProgress(0.9f)

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "HealthData_Filtered_$timestamp.json")

        FileWriter(file).use { writer ->
            writer.write(rootObject.toString(2))
        }

        onProgress(1f)
        return file
    }

    private fun entryToJson(entry: HistoryDisplayEntry): JSONObject {
        return JSONObject().apply {
            put("id", entry.id)
            put("calculator_type", entry.calculatorType.key)
            put("value", entry.primaryValue)
            put("label", entry.primaryLabel)
            put("category", entry.category ?: JSONObject.NULL)
            put("timestamp", entry.timestamp)
            put("date_formatted", entry.formattedDateTime)
            put("note", entry.note ?: JSONObject.NULL)

            val detailsObj = JSONObject()
            entry.details.forEach { (key, value) ->
                detailsObj.put(key, value)
            }
            put("details", detailsObj)
        }
    }
}
