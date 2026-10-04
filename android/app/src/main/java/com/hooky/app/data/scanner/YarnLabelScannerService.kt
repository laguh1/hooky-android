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
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizeTextResult(image)
        // Line height (from ML Kit's bounding boxes) tells brand/logo text (largest print)
        // apart from the product name and small-print details — order alone is unreliable.
        val ocrLines = result.textBlocks.flatMap { block ->
            block.lines.map { line -> OcrLine(line.text, line.boundingBox?.height() ?: 0) }
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
