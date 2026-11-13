package com.hooky.app.data.scanner

import android.content.Context
import com.hooky.app.domain.model.NeedleScanResult
import com.hooky.app.domain.model.YarnLabelScanResult
import com.hooky.app.util.decodeSampledBitmapFromFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class YarnLabelScannerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanFromPath(photoPath: String): YarnLabelScanResult {
        // Capped well above what OCR needs for legible label text, but far below raw
        // camera resolution — avoids decoding a 40MB+ bitmap just to read a label.
        val bitmap = decodeSampledBitmapFromFile(photoPath, maxDimension = 2048)
            ?: throw IllegalStateException("Could not decode image at: $photoPath")
        val primary = parseImage(InputImage.fromBitmap(bitmap, 0))
        // Stickers are often stuck on upside down ("COL 122 / LOT 24F08" on a Carícia label,
        // 2026-11-13). When a key number is missing, read the photo turned 180° too and take
        // only numeric/technical fields from it.
        if (primary.colorCode != null && primary.ballWeightG != null && primary.ballLengthM != null) return primary
        val rotated = try { parseImage(InputImage.fromBitmap(bitmap, 180)) } catch (_: Exception) { null }
        return rotated?.let { YarnLabelParser.fillFromRotated(primary, it) } ?: primary
    }

    private suspend fun parseImage(image: InputImage): YarnLabelScanResult {
        val result = recognizeTextResult(image)
        // Line height (from ML Kit's bounding boxes) tells brand/logo text (largest print)
        // apart from the product name and small-print details — order alone is unreliable.
        val ocrLines = result.textBlocks.withIndex().flatMap { (blockIndex, block) ->
            block.lines.map { line ->
                val box = line.boundingBox
                OcrLine(line.text, box?.height() ?: 0, blockIndex, box?.left ?: -1, box?.top ?: -1, box?.right ?: -1)
            }
        }
        if (com.hooky.app.BuildConfig.DEBUG) {
            // Debug builds only: the raw OCR lines, to tune the parser from real labels
            // (adb logcat -s HookyOCR). Never logged in release builds.
            ocrLines.forEach { android.util.Log.d("HookyOCR", "b${it.block} h${it.heightPx} x${it.left}-${it.right} y${it.top} | ${it.text}") }
        }
        return YarnLabelParser.parse(result.text, ocrLines)
    }

    suspend fun scanNeedleFromPath(photoPath: String, mode: ScanMode = ScanMode.HOOK): NeedleScanResult? {
        val bitmap = decodeSampledBitmapFromFile(photoPath, maxDimension = 2048)
            ?: throw IllegalStateException("Could not decode image at: $photoPath")
        val image = InputImage.fromBitmap(bitmap, 0)
        val rawText = recognizeTextResult(image).text
        return NeedleParser.parse(rawText, mode)
    }

    private suspend fun recognizeTextResult(image: InputImage): Text =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result -> continuation.resume(result) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
}
