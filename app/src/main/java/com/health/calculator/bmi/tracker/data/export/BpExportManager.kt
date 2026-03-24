// data/export/BpExportManager.kt
package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BpExportManager(private val context: Context) {

    companion object {
        private const val AUTHORITY = "com.health.calculator.bmi.tracker.fileprovider"
        private const val PDF_PAGE_WIDTH = 595 // A4
        private const val PDF_PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 16f
    }

    // ─── CSV Export ────────────────────────────────────────────────────────────

    fun exportToCsv(
        readings: List<BloodPressureEntity>,
        profileName: String
    ): Uri? {
        try {
            val fileName = "bp_readings_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                // Header
                writer.append("Date,Time,Systolic (mmHg),Diastolic (mmHg),Pulse (BPM),Category,Risk Level,Arm,Position,Time of Day,On Medication,Medication Name,Pulse Pressure,MAP,Note\n")

                readings.forEach { entity ->
                    val dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(entity.measurementTimestamp),
                        ZoneId.systemDefault()
                    )
                    val date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

                    val categoryDisplay = try {
                        BpCategory.valueOf(entity.category).displayName
                    } catch (e: Exception) { entity.category }

                    val riskDisplay = try {
                        BpRiskLevel.valueOf(entity.riskLevel).displayName
                    } catch (e: Exception) { entity.riskLevel }

                    val armDisplay = entity.arm?.let {
                        try { BpArm.valueOf(it).displayName } catch (e: Exception) { it }
                    } ?: ""

                    val posDisplay = entity.position?.let {
                        try { BpPosition.valueOf(it).displayName } catch (e: Exception) { it }
                    } ?: ""

                    val todDisplay = entity.timeOfDay?.let {
                        try { BpTimeOfDay.valueOf(it).displayName } catch (e: Exception) { it }
                    } ?: ""

                    val escapedNote = "\"${entity.note.replace("\"", "\"\"")}\""

                    writer.append("$date,$time,${entity.systolic},${entity.diastolic},${entity.pulse ?: ""},")
                    writer.append("$categoryDisplay,$riskDisplay,$armDisplay,$posDisplay,$todDisplay,")
                    writer.append("${entity.onMedication},\"${entity.medicationName}\",")
                    writer.append("${entity.pulsePressure},${String.format("%.1f", entity.meanArterialPressure)},")
                    writer.append("$escapedNote\n")
                }
            }

            return FileProvider.getUriForFile(context, AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // ─── Text Share ────────────────────────────────────────────────────────────

    fun formatReadingAsText(entity: BloodPressureEntity): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.measurementTimestamp),
            ZoneId.systemDefault()
        )

        val categoryDisplay = try {
            BpCategory.valueOf(entity.category).displayName
        } catch (e: Exception) { entity.category }

        val sb = StringBuilder()
        sb.appendLine("🩺 Blood Pressure Reading")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📊 ${entity.systolic}/${entity.diastolic} mmHg")
        sb.appendLine("📋 Category: $categoryDisplay (WHO)")

        entity.pulse?.let {
            sb.appendLine("❤️ Pulse: $it BPM")
        }

        sb.appendLine("📅 ${dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}")

        if (entity.onMedication) {
            val medName = if (entity.medicationName.isNotEmpty()) " (${entity.medicationName})" else ""
            sb.appendLine("💊 On BP Medication$medName")
        }

        if (entity.note.isNotEmpty()) {
            sb.appendLine("📝 Note: ${entity.note}")
        }

        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Tracked with Health Calculator: BMI Tracker")

        return sb.toString()
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share BP Reading").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ─── Image Share ───────────────────────────────────────────────────────────

    fun createReadingImage(entity: BloodPressureEntity): Uri? {
        try {
            val width = 600
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val category = try {
                BpCategory.valueOf(entity.category)
            } catch (e: Exception) { BpCategory.OPTIMAL }

            val categoryColor = getCategoryColorInt(category)
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.measurementTimestamp),
                ZoneId.systemDefault()
            )

            // Background
            val bgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 24f, 24f, bgPaint)

            // Top color bar
            val barPaint = Paint().apply {
                color = categoryColor
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), 80f), 24f, 24f, barPaint)
            canvas.drawRect(RectF(0f, 40f, width.toFloat(), 80f), barPaint)

            // Title text
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🩺 Blood Pressure Reading", width / 2f, 52f, titlePaint)

            // Reading value
            val readingPaint = Paint().apply {
                color = categoryColor
                textSize = 56f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("${entity.systolic}/${entity.diastolic}", width / 2f, 160f, readingPaint)

            // mmHg label
            val unitPaint = Paint().apply {
                color = Color.GRAY
                textSize = 18f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("mmHg", width / 2f, 185f, unitPaint)

            // Category badge
            val badgePaint = Paint().apply {
                color = categoryColor
                alpha = 40
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val categoryText = category.displayName
            val catTextPaint = Paint().apply {
                color = categoryColor
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val catWidth = catTextPaint.measureText(categoryText) + 40f
            canvas.drawRoundRect(
                RectF(width / 2f - catWidth / 2, 200f, width / 2f + catWidth / 2, 228f),
                14f, 14f, badgePaint
            )
            canvas.drawText(categoryText, width / 2f, 222f, catTextPaint)

            // Pulse
            var yOffset = 260f
            entity.pulse?.let {
                val pulsePaint = Paint().apply {
                    color = Color.DKGRAY
                    textSize = 16f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("❤️ Pulse: $it BPM", width / 2f, yOffset, pulsePaint)
                yOffset += 30f
            }

            // Date
            val datePaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")),
                width / 2f, yOffset, datePaint
            )
            yOffset += 25f

            // Medication
            if (entity.onMedication) {
                val medPaint = Paint().apply {
                    color = Color.rgb(30, 136, 229)
                    textSize = 13f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                val medText = if (entity.medicationName.isNotEmpty())
                    "💊 On Medication: ${entity.medicationName}"
                else "💊 On BP Medication"
                canvas.drawText(medText, width / 2f, yOffset, medPaint)
                yOffset += 25f
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.LTGRAY
                textSize = 11f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Health Calculator: BMI Tracker", width / 2f, height - 20f, footerPaint)

            // Border
            val borderPaint = Paint().apply {
                color = categoryColor
                alpha = 60
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(1.5f, 1.5f, width - 1.5f, height - 1.5f),
                24f, 24f, borderPaint
            )

            // Save
            val fileName = "bp_reading_${System.currentTimeMillis()}.png"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            return FileProvider.getUriForFile(context, AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share BP Reading").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // ─── PDF Report ────────────────────────────────────────────────────────────

    fun exportToPdf(
        readings: List<BloodPressureEntity>,
        profileName: String,
        isDoctorReport: Boolean = false
    ): Uri? {
        try {
            val document = PdfDocument()
            var pageNumber = 1
            var currentPage: PdfDocument.Page
            var canvas: Canvas
            var yPos: Float

            fun startNewPage(): Pair<PdfDocument.Page, Canvas> {
                val pageInfo = PdfDocument.PageInfo.Builder(
                    PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, pageNumber++
                ).create()
                val page = document.startPage(pageInfo)
                return page to page.canvas
            }

            fun checkPageBreak(currentY: Float, needed: Float, pg: PdfDocument.Page): Triple<Float, PdfDocument.Page, Canvas> {
                if (currentY + needed > PDF_PAGE_HEIGHT - MARGIN) {
                    document.finishPage(pg)
                    val (newPage, newCanvas) = startNewPage()
                    return Triple(MARGIN + 20f, newPage, newCanvas)
                }
                return Triple(currentY, pg, pg.canvas)
            }

            // Paints
            val titlePaint = Paint().apply {
                color = if (isDoctorReport) Color.rgb(30, 136, 229) else Color.rgb(0, 121, 107)
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.rgb(66, 66, 66)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(100, 100, 100)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(60, 60, 60)
                textSize = 10f
                isAntiAlias = true
            }
            val smallPaint = Paint().apply {
                color = Color.rgb(130, 130, 130)
                textSize = 9f
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 1f
            }

            val sortedReadings = readings.sortedByDescending { it.measurementTimestamp }
            val displayReadings = if (isDoctorReport) sortedReadings.take(30) else sortedReadings

            // Page 1
            var (page, cvs) = startNewPage()
            currentPage = page
            canvas = cvs
            yPos = MARGIN

            // ─── Header ─────
            if (isDoctorReport) {
                val headerBg = Paint().apply {
                    color = Color.rgb(227, 242, 253)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, PDF_PAGE_WIDTH.toFloat(), 80f, headerBg)

                titlePaint.textSize = 20f
                canvas.drawText("Blood Pressure Report", MARGIN, 35f, titlePaint)

                val medicalPaint = Paint().apply {
                    color = Color.rgb(30, 136, 229)
                    textSize = 11f
                    isAntiAlias = true
                }
                canvas.drawText("CONFIDENTIAL - For Medical Professional Review", MARGIN, 55f, medicalPaint)

                yPos = 100f
            } else {
                canvas.drawText("Blood Pressure Report", MARGIN, yPos + 22f, titlePaint)
                yPos += 40f
            }

            // Patient info
            if (profileName.isNotEmpty()) {
                canvas.drawText("Patient: $profileName", MARGIN, yPos, subtitlePaint)
                yPos += 20f
            }

            val dateRange = if (displayReadings.isNotEmpty()) {
                val oldest = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(displayReadings.last().measurementTimestamp),
                    ZoneId.systemDefault()
                ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                val newest = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(displayReadings.first().measurementTimestamp),
                    ZoneId.systemDefault()
                ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                "$oldest – $newest"
            } else "No readings"

            canvas.drawText("Period: $dateRange", MARGIN, yPos, bodyPaint)
            yPos += 15f
            canvas.drawText("Total Readings: ${displayReadings.size}", MARGIN, yPos, bodyPaint)
            yPos += 15f
            canvas.drawText(
                "Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}",
                MARGIN, yPos, smallPaint
            )
            yPos += 25f

            // Divider
            canvas.drawLine(MARGIN, yPos, PDF_PAGE_WIDTH - MARGIN, yPos, linePaint)
            yPos += 15f

            // ─── Summary Statistics ─────
            if (displayReadings.isNotEmpty()) {
                canvas.drawText("SUMMARY", MARGIN, yPos, subtitlePaint)
                yPos += 20f

                val avgSys = displayReadings.map { it.systolic }.average()
                val avgDia = displayReadings.map { it.diastolic }.average()
                val avgPulse = displayReadings.mapNotNull { it.pulse }.let {
                    if (it.isNotEmpty()) it.average() else null
                }

                canvas.drawText(
                    "Average: ${String.format("%.0f", avgSys)}/${String.format("%.0f", avgDia)} mmHg",
                    MARGIN, yPos, bodyPaint
                )
                yPos += 15f

                avgPulse?.let {
                    canvas.drawText("Average Pulse: ${String.format("%.0f", it)} BPM", MARGIN, yPos, bodyPaint)
                    yPos += 15f
                }

                canvas.drawText(
                    "Highest: ${displayReadings.maxOf { it.systolic }}/${displayReadings.maxOf { it.diastolic }} mmHg",
                    MARGIN, yPos, bodyPaint
                )
                yPos += 15f
                canvas.drawText(
                    "Lowest: ${displayReadings.minOf { it.systolic }}/${displayReadings.minOf { it.diastolic }} mmHg",
                    MARGIN, yPos, bodyPaint
                )
                yPos += 15f

                // Morning vs Evening (doctor report)
                if (isDoctorReport) {
                    val morningReadings = displayReadings.filter {
                        it.timeOfDay == BpTimeOfDay.MORNING.name
                    }
                    val eveningReadings = displayReadings.filter {
                        it.timeOfDay == BpTimeOfDay.EVENING.name || it.timeOfDay == BpTimeOfDay.NIGHT.name
                    }

                    if (morningReadings.isNotEmpty()) {
                        val mSys = morningReadings.map { it.systolic }.average()
                        val mDia = morningReadings.map { it.diastolic }.average()
                        canvas.drawText(
                            "Morning Avg: ${String.format("%.0f", mSys)}/${String.format("%.0f", mDia)} mmHg (${morningReadings.size} readings)",
                            MARGIN, yPos, bodyPaint
                        )
                        yPos += 15f
                    }
                    if (eveningReadings.isNotEmpty()) {
                        val eSys = eveningReadings.map { it.systolic }.average()
                        val eDia = eveningReadings.map { it.diastolic }.average()
                        canvas.drawText(
                            "Evening Avg: ${String.format("%.0f", eSys)}/${String.format("%.0f", eDia)} mmHg (${eveningReadings.size} readings)",
                            MARGIN, yPos, bodyPaint
                        )
                        yPos += 15f
                    }

                    // Medication status
                    val medicatedCount = displayReadings.count { it.onMedication }
                    if (medicatedCount > 0) {
                        canvas.drawText(
                            "Readings on medication: $medicatedCount of ${displayReadings.size}",
                            MARGIN, yPos, bodyPaint
                        )
                        yPos += 15f

                        val medNames = displayReadings
                            .filter { it.onMedication && it.medicationName.isNotEmpty() }
                            .map { it.medicationName }
                            .distinct()
                        if (medNames.isNotEmpty()) {
                            canvas.drawText("Medications: ${medNames.joinToString(", ")}", MARGIN, yPos, bodyPaint)
                            yPos += 15f
                        }
                    }
                }

                // Category distribution
                yPos += 5f
                canvas.drawText("CATEGORY DISTRIBUTION", MARGIN, yPos, subtitlePaint)
                yPos += 18f

                val grouped = displayReadings.groupBy { it.category }
                grouped.forEach { (catName, catReadings) ->
                    val catDisplay = try {
                        BpCategory.valueOf(catName).displayName
                    } catch (e: Exception) { catName }
                    val pct = (catReadings.size.toFloat() / displayReadings.size * 100).toInt()

                    canvas.drawText(
                        "• $catDisplay: ${catReadings.size} readings ($pct%)",
                        MARGIN + 10f, yPos, bodyPaint
                    )
                    yPos += 14f
                }
            }

            yPos += 15f
            canvas.drawLine(MARGIN, yPos, PDF_PAGE_WIDTH - MARGIN, yPos, linePaint)
            yPos += 15f

            // ─── Readings Table ─────
            canvas.drawText("ALL READINGS", MARGIN, yPos, subtitlePaint)
            yPos += 20f

            // Table headers
            val colX = floatArrayOf(MARGIN, 110f, 175f, 235f, 285f, 325f, 410f)
            val headers = arrayOf("Date/Time", "SYS", "DIA", "Pulse", "PP", "Category", "Note")

            val headerBg = Paint().apply {
                color = Color.rgb(240, 240, 240)
                style = Paint.Style.FILL
            }
            canvas.drawRect(MARGIN - 5, yPos - 12f, PDF_PAGE_WIDTH - MARGIN + 5, yPos + 4f, headerBg)

            headers.forEachIndexed { i, header ->
                canvas.drawText(header, colX[i], yPos, headerPaint)
            }
            yPos += 16f

            displayReadings.forEach { entity ->
                val result = checkPageBreak(yPos, 16f, currentPage)
                yPos = result.first
                currentPage = result.second
                canvas = result.third

                val dt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.measurementTimestamp),
                    ZoneId.systemDefault()
                )

                val catDisplay = try {
                    BpCategory.valueOf(entity.category).displayName
                } catch (e: Exception) { entity.category }

                val medIcon = if (entity.onMedication) " 💊" else ""

                canvas.drawText(dt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm")), colX[0], yPos, bodyPaint)
                canvas.drawText("${entity.systolic}", colX[1], yPos, bodyPaint)
                canvas.drawText("${entity.diastolic}", colX[2], yPos, bodyPaint)
                canvas.drawText("${entity.pulse ?: "-"}", colX[3], yPos, bodyPaint)
                canvas.drawText("${entity.pulsePressure}", colX[4], yPos, bodyPaint)

                val catPaint = Paint(bodyPaint).apply {
                    color = getCategoryColorInt(try { BpCategory.valueOf(entity.category) } catch (e: Exception) { BpCategory.OPTIMAL })
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(catDisplay.take(12) + medIcon, colX[5], yPos, catPaint)

                if (entity.note.isNotEmpty()) {
                    canvas.drawText(entity.note.take(18), colX[6], yPos, smallPaint)
                }

                yPos += 14f

                // Light row divider
                val rowLine = Paint().apply { color = Color.rgb(240, 240, 240); strokeWidth = 0.5f }
                canvas.drawLine(MARGIN, yPos - 3f, PDF_PAGE_WIDTH - MARGIN, yPos - 3f, rowLine)
            }

            // Footer
            yPos += 20f
            val footResult = checkPageBreak(yPos, 40f, currentPage)
            yPos = footResult.first
            currentPage = footResult.second
            canvas = footResult.third

            canvas.drawLine(MARGIN, yPos, PDF_PAGE_WIDTH - MARGIN, yPos, linePaint)
            yPos += 15f

            val disclaimerPaint = Paint().apply {
                color = Color.rgb(150, 150, 150)
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText(
                "This report is generated for informational purposes only and does not constitute medical advice.",
                MARGIN, yPos, disclaimerPaint
            )
            yPos += 12f
            canvas.drawText(
                "Always consult a qualified healthcare professional for medical decisions.",
                MARGIN, yPos, disclaimerPaint
            )
            yPos += 12f
            canvas.drawText(
                "Report generated by Health Calculator: BMI Tracker",
                MARGIN, yPos, disclaimerPaint
            )

            document.finishPage(currentPage)

            // Save file
            val prefix = if (isDoctorReport) "bp_doctor_report" else "bp_report"
            val fileName = "${prefix}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            return FileProvider.getUriForFile(context, AUTHORITY, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareFile(uri: Uri, mimeType: String, title: String = "Share Report") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun viewFile(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareFile(uri, mimeType)
        }
    }

    private fun getCategoryColorInt(category: BpCategory): Int {
        return when (category) {
            BpCategory.HYPOTENSION -> Color.rgb(66, 165, 245)
            BpCategory.OPTIMAL -> Color.rgb(76, 175, 80)
            BpCategory.NORMAL -> Color.rgb(139, 195, 74)
            BpCategory.HIGH_NORMAL -> Color.rgb(255, 193, 7)
            BpCategory.ISOLATED_SYSTOLIC -> Color.rgb(255, 152, 0)
            BpCategory.GRADE_1_HYPERTENSION -> Color.rgb(255, 112, 67)
            BpCategory.GRADE_2_HYPERTENSION -> Color.rgb(244, 67, 54)
            BpCategory.GRADE_3_HYPERTENSION -> Color.rgb(211, 47, 47)
            BpCategory.HYPERTENSIVE_CRISIS -> Color.rgb(183, 28, 28)
        }
    }
}
