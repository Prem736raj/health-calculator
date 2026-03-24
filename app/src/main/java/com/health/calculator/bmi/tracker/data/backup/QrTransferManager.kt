package com.health.calculator.bmi.tracker.data.backup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

class QrTransferManager(
    private val context: Context
) {
    companion object {
        private const val QR_SIZE = 512
        private const val TRANSFER_PREFIX = "healthcalc://"

        @Volatile
        private var INSTANCE: QrTransferManager? = null

        fun getInstance(context: Context): QrTransferManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QrTransferManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Generate a QR code that encodes a URI pointing to the backup file
     */
    fun generateTransferQrContent(backupFile: File): String {
        val timestamp = System.currentTimeMillis()
        val fileSize = backupFile.length()
        val fileName = backupFile.name

        return "${TRANSFER_PREFIX}transfer?file=$fileName&size=$fileSize&ts=$timestamp"
    }

    fun generateQrBitmap(content: String): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints)

        val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888)
        for (x in 0 until QR_SIZE) {
            for (y in 0 until QR_SIZE) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    fun saveQrBitmap(bitmap: Bitmap): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "transfer_qr.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun parseTransferContent(content: String): TransferInfo? {
        if (!content.startsWith(TRANSFER_PREFIX)) return null

        return try {
            val params = content.removePrefix("${TRANSFER_PREFIX}transfer?")
                .split("&")
                .associate {
                    val parts = it.split("=", limit = 2)
                    parts[0] to parts.getOrElse(1) { "" }
                }

            TransferInfo(
                fileName = params["file"] ?: "",
                fileSize = params["size"]?.toLongOrNull() ?: 0L,
                timestamp = params["ts"]?.toLongOrNull() ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getBackupFileForSharing(backupFile: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
    }
}

data class TransferInfo(
    val fileName: String,
    val fileSize: Long,
    val timestamp: Long
)
