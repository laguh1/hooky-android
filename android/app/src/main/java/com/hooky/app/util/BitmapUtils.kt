package com.hooky.app.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

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
    return BitmapFactory.decodeFile(path, sampleOptions)
}

fun decodeSampledBitmapFromResource(resources: Resources, resId: Int, maxDimension: Int): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(resources, resId, boundsOptions)
    val sampleOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(boundsOptions, maxDimension)
    }
    return BitmapFactory.decodeResource(resources, resId, sampleOptions)
}
