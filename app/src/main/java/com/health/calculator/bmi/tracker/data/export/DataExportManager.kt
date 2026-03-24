package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

class DataExportManager(private val context: Context) {
    private val pdfHelper = PdfExportHelper(context)
    private val csvHelper = CsvExportHelper(context)
    private val jsonHelper = JsonExportHelper(context)
    private val shareImageHelper = ShareImageHelper(context)

    private val _exportProgress = MutableStateFlow(ExportProgress())
    val exportProgress: StateFlow<ExportProgress> = _exportProgress.asStateFlow()

    private val authority = "${context.packageName}.fileprovider"

    companion object {
        @Volatile
        private var INSTANCE: DataExportManager? = null

        fun getInstance(context: Context): DataExportManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataExportManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun exportData(
        entries: List<HistoryDisplayEntry>,
        config: ExportConfig,
        profileName: String? = null,
        profileData: Map<String, String>? = null
    ) {
        _exportProgress.update {
            ExportProgress(isExporting = true, statusMessage = "Preparing export...")
        }

        try {
            val file = withContext(Dispatchers.IO) {
                when (config.format) {
                    ExportFormat.PDF -> {
                        _exportProgress.update { it.copy(statusMessage = "Generating PDF report...") }
                        pdfHelper.generateReport(entries, config, profileName) { progress ->
                            _exportProgress.update { it.copy(progress = progress) }
                        }
                    }

                    ExportFormat.CSV -> {
                        _exportProgress.update { it.copy(statusMessage = "Generating CSV file...") }
                        if (config.calculatorType != null) {
                            csvHelper.exportByCalculator(entries, config.calculatorType) { progress ->
                                _exportProgress.update { it.copy(progress = progress) }
                            }
                        } else {
                            csvHelper.exportAll(entries) { progress ->
                                _exportProgress.update { it.copy(progress = progress) }
                            }
                        }
                    }

                    ExportFormat.JSON -> {
                        _exportProgress.update { it.copy(statusMessage = "Generating JSON backup...") }
                        jsonHelper.exportAll(entries, profileData) { progress ->
                            _exportProgress.update { it.copy(progress = progress) }
                        }
                    }
                }
            }

            val uri = FileProvider.getUriForFile(context, authority, file)

            _exportProgress.update {
                ExportProgress(
                    isExporting = false,
                    progress = 1f,
                    isComplete = true,
                    resultUri = uri,
                    statusMessage = "Export complete!"
                )
            }
        } catch (e: Exception) {
            _exportProgress.update {
                ExportProgress(
                    isExporting = false,
                    error = "Export failed: ${e.localizedMessage}"
                )
            }
        }
    }

    suspend fun generateShareImage(card: HealthReportCard): Uri? {
        return try {
            val file = withContext(Dispatchers.IO) {
                shareImageHelper.generateHealthReportCard(card)
            }
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            null
        }
    }

    fun shareFile(uri: Uri, format: ExportFormat) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = format.mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Health Data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun shareImage(uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Health Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun resetProgress() {
        _exportProgress.update { ExportProgress() }
    }

    fun clearExportCache() {
        val exportDir = File(context.cacheDir, "exports")
        exportDir.listFiles()?.forEach { it.delete() }
    }
}
