package com.hooky.app.data.scanner

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Developer tool, not a check: re-reads the label photos already taken on this device
 * (filesDir/photos/temp) and logs the raw OCR lines (tag HookyOCR, from the scanner service)
 * plus the parsed result (tag HookyOCRResult). Lets the parser be tuned from real scans
 * without asking for a new scan. Run only on a side-by-side debug install:
 *   adb shell am instrument -w -e class com.hooky.app.data.scanner.LabelPhotoOcrDump \
 *     com.laguh.hooky.debug.test/androidx.test.runner.AndroidJUnitRunner
 * Passes trivially when there are no photos.
 */
@RunWith(AndroidJUnit4::class)
class LabelPhotoOcrDump {

    @Test
    fun dumpRecentLabelPhotos() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val args = InstrumentationRegistry.getArguments()
        val only = args.getString("photo")
        val photos = File(context.filesDir, "photos/temp").listFiles { f -> f.extension == "jpg" }
            ?.filter { only == null || it.name.contains(only) }
            ?.sortedByDescending { it.lastModified() }
            ?.take(args.getString("count")?.toIntOrNull() ?: 4)
            .orEmpty()
        val service = YarnLabelScannerService(context)
        for (photo in photos) {
            Log.d("HookyOCR", "=== ${photo.name}")
            val result = service.scanFromPath(photo.absolutePath)
            Log.d("HookyOCRResult", "${photo.name}: $result")
        }
    }
}
