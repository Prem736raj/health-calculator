package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExportHelper(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595  // A4 in points
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
    }

    // Colors
    private val primaryColor = Color.rgb(0, 121, 145)   // Teal
    private val darkText = Color.rgb(33, 33, 33)
    private val mediumText = Color.rgb(100, 100, 100)
    private val lightBg = Color.rgb(245, 248, 250)
    private val greenColor = Color.rgb(76, 175, 80)
    private val yellowColor = Color.rgb(255, 193, 7)
    private val orangeColor = Color.rgb(255, 152, 0)
    private val redColor = Color.rgb(244, 67, 54)
    private val borderColor = Color.rgb(224, 224, 224)

    // Paints
    private val titlePaint = TextPaint().apply {
        color = primaryColor
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val subtitlePaint = TextPaint().apply {
        color = darkText
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val bodyPaint = TextPaint().apply {
        color = darkText
        textSize = 11f
        isAntiAlias = true
    }

    private val smallPaint = TextPaint().apply {
        color = mediumText
        textSize = 9f
        isAntiAlias = true
    }

    private val headerPaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val cellPaint = TextPaint().apply {
        color = darkText
        textSize = 9.5f
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = borderColor
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun generateReport(
        entries: List<HistoryDisplayEntry>,
        config: ExportConfig,
        profileName: String?,
        onProgress: (Float) -> Unit
    ): File {
        val document = PdfDocument()
        val pages = mutableListOf<PdfDocument.Page>()
        var currentPageNum = 1
        var yPosition = MARGIN

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNum).create()
            val page = document.startPage(pageInfo)
            currentPageNum++
            yPosition = MARGIN
            return page
        }

        fun finishPage(page: PdfDocument.Page) {
            // Footer
            val footerPaint = TextPaint(smallPaint).apply { textAlign = Paint.Align.CENTER }
            page.canvas.drawText(
                "Health Calculator: BMI Tracker — Page ${currentPageNum - 1}",
                PAGE_WIDTH / 2f,
                PAGE_HEIGHT - 20f,
                footerPaint
            )
            document.finishPage(page)
        }

        fun ensureSpace(page: PdfDocument.Page, needed: Float): PdfDocument.Page {
            return if (yPosition + needed > PAGE_HEIGHT - 60) {
                finishPage(page)
                newPage()
            } else page
        }

        onProgress(0.1f)

        // === COVER PAGE ===
        var page = newPage()
        var canvas = page.canvas

        // App logo area
        val logoBgPaint = Paint().apply { color = primaryColor }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 200f, logoBgPaint)

        val logoText = TextPaint().apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Health Calculator", PAGE_WIDTH / 2f, 100f, logoText)

        val tagLine = TextPaint().apply {
            color = Color.rgb(200, 235, 240)
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("BMI Tracker — Health Report", PAGE_WIDTH / 2f, 130f, tagLine)

        yPosition = 240f

        // Report info
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        val now = dateFormatter.format(Date())

        if (config.includeProfile && !profileName.isNullOrBlank()) {
            canvas.drawText("Prepared for: $profileName", MARGIN, yPosition, subtitlePaint)
            yPosition += 30f
        }

        canvas.drawText("Generated: $now", MARGIN, yPosition, bodyPaint)
        yPosition += 20f

        config.dateRangeLabel?.let {
            canvas.drawText("Date Range: $it", MARGIN, yPosition, bodyPaint)
            yPosition += 20f
        }

        val totalEntries = entries.size
        val calcTypes = entries.map { it.calculatorType }.distinct().size
        canvas.drawText("Total Entries: $totalEntries across $calcTypes calculator types", MARGIN, yPosition, bodyPaint)
        yPosition += 50f

        // Summary statistics
        if (config.includeSummaryStats) {
            canvas.drawText("Summary Overview", MARGIN, yPosition, subtitlePaint)
            yPosition += 25f

            val grouped = entries.groupBy { it.calculatorType }
            grouped.forEach { (type, typeEntries) ->
                val bgPaint = Paint().apply { color = lightBg }
                canvas.drawRoundRect(
                    MARGIN, yPosition - 12f,
                    MARGIN + CONTENT_WIDTH, yPosition + 18f,
                    4f, 4f, bgPaint
                )
                canvas.drawText(
                    "${type.emoji} ${type.displayName}: ${typeEntries.size} readings",
                    MARGIN + 10f, yPosition + 4f, bodyPaint
                )
                yPosition += 28f

                if (yPosition > PAGE_HEIGHT - 100) {
                    finishPage(page)
                    page = newPage()
                    canvas = page.canvas
                }
            }
        }

        finishPage(page)
        onProgress(0.3f)

        // === DATA PAGES - One section per calculator ===
        val groupedEntries = entries.groupBy { it.calculatorType }
        val totalGroups = groupedEntries.size
        var processedGroups = 0

        groupedEntries.forEach { (type, typeEntries) ->
            page = newPage()
            canvas = page.canvas

            // Section header
            val sectionBgPaint = Paint().apply { color = primaryColor }
            canvas.drawRect(MARGIN, yPosition - 5f, MARGIN + CONTENT_WIDTH, yPosition + 30f, sectionBgPaint)

            val sectionTitle = TextPaint().apply {
                color = Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(
                "${type.emoji} ${type.displayName} (${typeEntries.size} entries)",
                MARGIN + 10f, yPosition + 20f, sectionTitle
            )
            yPosition += 50f

            // Statistics for this type
            val values = typeEntries.mapNotNull { it.primaryValue.toDoubleOrNull() }
            if (values.isNotEmpty()) {
                canvas.drawText("Statistics:", MARGIN, yPosition, subtitlePaint.apply { textSize = 13f })
                yPosition += 20f

                val stats = listOf(
                    "Latest: ${typeEntries.first().primaryValue} ${typeEntries.first().primaryLabel}",
                    "Average: ${"%.1f".format(values.average())}",
                    "Highest: ${"%.1f".format(values.max())}",
                    "Lowest: ${"%.1f".format(values.min())}",
                    "Readings: ${values.size}"
                )
                stats.forEach { stat ->
                    canvas.drawText("  • $stat", MARGIN + 10f, yPosition, bodyPaint)
                    yPosition += 16f
                }
                yPosition += 10f
            }

            // Table of entries
            canvas.drawText("All Readings:", MARGIN, yPosition, subtitlePaint.apply { textSize = 13f })
            yPosition += 20f

            // Table header
            val colWidths = floatArrayOf(120f, 80f, 120f, CONTENT_WIDTH - 320f)
            val headers = listOf("Date & Time", "Value", "Category", "Details")

            val headerBg = Paint().apply { color = primaryColor }
            canvas.drawRect(MARGIN, yPosition - 12f, MARGIN + CONTENT_WIDTH, yPosition + 8f, headerBg)

            var xPos = MARGIN + 5f
            headers.forEachIndexed { i, header ->
                canvas.drawText(header, xPos, yPosition + 2f, headerPaint)
                xPos += colWidths[i]
            }
            yPosition += 18f

            // Table rows
            typeEntries.forEachIndexed { index, entry ->
                page = ensureSpace(page, 22f)
                canvas = page.canvas

                // Alternating row background
                if (index % 2 == 0) {
                    val rowBg = Paint().apply { color = lightBg }
                    canvas.drawRect(MARGIN, yPosition - 10f, MARGIN + CONTENT_WIDTH, yPosition + 10f, rowBg)
                }

                xPos = MARGIN + 5f
                val dateFormat = SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())

                // Date
                canvas.drawText(dateFormat.format(Date(entry.timestamp)), xPos, yPosition + 2f, cellPaint)
                xPos += colWidths[0]

                // Value
                canvas.drawText("${entry.primaryValue} ${entry.primaryLabel}", xPos, yPosition + 2f, cellPaint)
                xPos += colWidths[1]

                // Category
                entry.category?.let {
                    canvas.drawText(it, xPos, yPosition + 2f, cellPaint)
                } ?: canvas.drawText("-", xPos, yPosition + 2f, cellPaint)
                xPos += colWidths[2]

                // Details (first detail only for space)
                val firstDetail = entry.details.entries.firstOrNull()
                firstDetail?.let {
                    val detailText = "${it.key}: ${it.value}"
                    val truncated = if (detailText.length > 35) detailText.take(32) + "..." else detailText
                    canvas.drawText(truncated, xPos, yPosition + 2f, cellPaint)
                }

                // Row border
                canvas.drawLine(MARGIN, yPosition + 10f, MARGIN + CONTENT_WIDTH, yPosition + 10f, linePaint)
                yPosition += 20f
            }

            finishPage(page)
            processedGroups++
            onProgress(0.3f + 0.6f * processedGroups / totalGroups)
        }

        // === DISCLAIMER PAGE ===
        page = newPage()
        canvas = page.canvas

        canvas.drawText("Disclaimer", MARGIN, yPosition, subtitlePaint)
        yPosition += 25f

        val disclaimerText = "This report is generated by Health Calculator: BMI Tracker for " +
                "informational and educational purposes only. It is NOT a substitute for professional " +
                "medical advice, diagnosis, or treatment. Always seek the advice of your physician " +
                "or other qualified health provider with any questions you may have regarding a " +
                "medical condition. Never disregard professional medical advice or delay in seeking " +
                "it because of information provided by this application."

        val disclaimerLayout = StaticLayout.Builder
            .obtain(disclaimerText, 0, disclaimerText.length, bodyPaint, CONTENT_WIDTH.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .build()

        canvas.save()
        canvas.translate(MARGIN, yPosition)
        disclaimerLayout.draw(canvas)
        canvas.restore()

        finishPage(page)
        onProgress(0.95f)

        // Save to file
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HealthReport_$timestamp.pdf"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        onProgress(1f)
        return file
    }
}
