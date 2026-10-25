package com.hooky.app.data.db

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Photos lists are stored newest first since DB version 10. Data saved before
 * that (and backups with format version 1) are oldest first, so they get
 * reversed once with this.
 */
private val photoListSerializer = ListSerializer(String.serializer())

fun reversePhotosJson(photosJson: String): String {
    val photos = try {
        Json.decodeFromString(photoListSerializer, photosJson)
    } catch (_: Exception) {
        return photosJson
    }
    if (photos.size < 2) return photosJson
    return Json.encodeToString(photoListSerializer, photos.reversed())
}
