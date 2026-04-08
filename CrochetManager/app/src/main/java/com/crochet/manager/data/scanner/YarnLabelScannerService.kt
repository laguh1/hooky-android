package com.crochet.manager.data.scanner

import android.content.Context
import android.graphics.BitmapFactory
import com.crochet.manager.domain.model.NeedleScanResult
import com.crochet.manager.domain.model.YarnLabelScanResult
import com.google.mlkit.vision.common.InputImage
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
        val bitmap = BitmapFactory.decodeFile(photoPath)
            ?: throw IllegalStateException("Could not decode image at: $photoPath")
        val image = InputImage.fromBitmap(bitmap, 0)
        val rawText = recognizeText(image)
        return YarnLabelParser.parse(rawText)
    }

    suspend fun scanNeedleFromPath(photoPath: String): NeedleScanResult? {
        val bitmap = BitmapFactory.decodeFile(photoPath)
            ?: throw IllegalStateException("Could not decode image at: $photoPath")
        val image = InputImage.fromBitmap(bitmap, 0)
        val rawText = recognizeText(image)
        return NeedleParser.parse(rawText)
    }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result -> continuation.resume(result.text) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
}
