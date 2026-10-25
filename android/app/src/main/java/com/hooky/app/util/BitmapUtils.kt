package com.hooky.app.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

// Standard Android bounds-then-decode pattern: read only the image dimensions first,
// then decode at the largest power-of-2 downsample that still covers maxDimension —
// avoids ever holding a full-resolution camera bitmap in memory when the caller only
// needs it at a much smaller display/processing size.
private fun calculateInSampleSize(options: BitmapFactory.Options, maxDimension: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > maxDimension || width > maxDimension) {
        var halfHeight = height / 2
        var halfWidth = width / 2
        while (halfHeight / inSampleSize >= maxDimension || halfWidth / inSampleSize >= maxDimension) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun decodeSampledBitmapFromFile(path: String, maxDimension: Int): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, boundsOptions)
    val sampleOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(boundsOptions, maxDimension)
    }
    val decoded = BitmapFactory.decodeFile(path, sampleOptions) ?: return null
    return applyExifOrientation(path, decoded)
}

// Camera photos (CameraX and most phone cameras) are stored sideways with an EXIF tag
// saying how to turn them. Coil honours the tag when displaying, BitmapFactory does not,
// so without this the photo editor rotated a different picture from the one it showed
// (the first rotate-and-save came out wrong), and the share card / OCR got it sideways.
private fun applyExifOrientation(path: String, bitmap: Bitmap): Bitmap {
    val orientation = try {
        ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (_: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
        else -> return bitmap
    }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotated != bitmap) bitmap.recycle()
    return rotated
}

fun decodeSampledBitmapFromResource(resources: Resources, resId: Int, maxDimension: Int): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(resources, resId, boundsOptions)
    val sampleOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(boundsOptions, maxDimension)
    }
    return BitmapFactory.decodeResource(resources, resId, sampleOptions)
}
