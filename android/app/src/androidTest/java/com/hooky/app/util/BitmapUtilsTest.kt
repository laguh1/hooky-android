package com.hooky.app.util

import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Camera photos are saved sideways with an EXIF orientation tag; the decoder must
// return them upright, or the photo editor rotates the wrong picture (fix #14).
@RunWith(AndroidJUnit4::class)
class BitmapUtilsTest {

    private fun writeJpeg(orientation: Int): File {
        val dir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val file = File(dir, "exif_$orientation.jpg")
        // 200 wide x 100 tall, left half red, right half blue
        val bmp = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        for (x in 0 until 200) for (y in 0 until 100) bmp.setPixel(x, y, if (x < 100) Color.RED else Color.BLUE)
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveAttributes()
        }
        return file
    }

    private fun isRed(c: Int) = Color.red(c) > 200 && Color.blue(c) < 60

    @Test
    fun rotate90_isDecodedUpright() {
        val bmp = decodeSampledBitmapFromFile(writeJpeg(ExifInterface.ORIENTATION_ROTATE_90).absolutePath, 2048)!!
        assertEquals(100, bmp.width)
        assertEquals(200, bmp.height)
        // Turned 90° clockwise: the red (left) half ends up on top
        assertEquals(true, isRed(bmp.getPixel(50, 20)))
    }

    @Test
    fun rotate270_isDecodedUpright() {
        val bmp = decodeSampledBitmapFromFile(writeJpeg(ExifInterface.ORIENTATION_ROTATE_270).absolutePath, 2048)!!
        assertEquals(100, bmp.width)
        assertEquals(200, bmp.height)
        assertEquals(true, isRed(bmp.getPixel(50, 180)))
    }

    @Test
    fun normal_isUnchanged() {
        val bmp = decodeSampledBitmapFromFile(writeJpeg(ExifInterface.ORIENTATION_NORMAL).absolutePath, 2048)!!
        assertEquals(200, bmp.width)
        assertEquals(100, bmp.height)
        assertEquals(true, isRed(bmp.getPixel(20, 50)))
    }
}
