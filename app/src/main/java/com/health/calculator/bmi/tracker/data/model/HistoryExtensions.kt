package com.health.calculator.bmi.tracker.data.model

import com.health.calculator.bmi.tracker.data.model.*
import org.json.JSONObject

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
    val parsedJson = try {
        val objectValue = JSONObject(json)
        objectValue.keys().asSequence().associateWith { key ->
            objectValue.opt(key)?.toString().orEmpty()
        }
    } catch (_: Exception) {
        // Local JVM tests do not provide Android's full org.json runtime. The
        // conservative fallback keeps valid string-valued detail payloads
        // readable there while Android still uses JSONObject as the primary
        // parser.
        parseJsonObjectFallback(json)
    }
    if (parsedJson.isNotEmpty() || json.trim() == "{}") return parsedJson

    return try {
        // Older releases stored a pipe-delimited payload. Keep this fallback
        // so existing history remains readable after the JSON parser upgrade.
        if (!json.contains("|")) return emptyMap()
        json.split("|").mapNotNull { pair ->
            val parts = pair.split(":", limit = 2)
            parts.takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() }
        }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }
}

private fun parseJsonObjectFallback(json: String): Map<String, String> {
    val source = json.trim()
    if (source.length < 2 || source.first() != '{' || source.last() != '}') return emptyMap()

    var index = 1
    val result = linkedMapOf<String, String>()

    fun skipWhitespace() {
        while (index < source.length && source[index].isWhitespace()) index++
    }

    fun readString(): String? {
        if (index >= source.length || source[index] != '\"') return null
        index++
        val builder = StringBuilder()
        while (index < source.length) {
            when (val character = source[index++]) {
                '\"' -> return builder.toString()
                '\\' -> {
                    if (index >= source.length) return null
                    when (val escaped = source[index++]) {
                        '\"', '\\', '/' -> builder.append(escaped)
                        'b' -> builder.append('\b')
                        'f' -> builder.append('\u000C')
                        'n' -> builder.append('\n')
                        'r' -> builder.append('\r')
                        't' -> builder.append('\t')
                        'u' -> {
                            if (index + 4 > source.length) return null
                            val code = source.substring(index, index + 4).toIntOrNull(16) ?: return null
                            builder.append(code.toChar())
                            index += 4
                        }
                        else -> return null
                    }
                }
                else -> builder.append(character)
            }
        }
        return null
    }

    while (true) {
        skipWhitespace()
        if (index >= source.length) return emptyMap()
        if (source[index] == '}') return result

        val key = readString() ?: return emptyMap()
        skipWhitespace()
        if (index >= source.length || source[index++] != ':') return emptyMap()
        skipWhitespace()

        val value = if (index < source.length && source[index] == '\"') {
            readString() ?: return emptyMap()
        } else {
            val start = index
            while (index < source.length && source[index] != ',' && source[index] != '}') index++
            source.substring(start, index).trim()
        }
        result[key] = value
        skipWhitespace()
        when {
            index < source.length && source[index] == ',' -> index++
            index < source.length && source[index] == '}' -> return result
            else -> return emptyMap()
        }
    }
}
