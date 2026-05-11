package com.chading.photos.data

import android.net.Uri

/**
 * A single photo or video discovered via MediaStore.
 */
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTakenMillis: Long,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val durationMillis: Long,
    val bucketId: Long,
    val bucketName: String,
    val relativePath: String,
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/", ignoreCase = true)
}

/**
 * An album is a MediaStore bucket (folder) plus a cover MediaItem and a count.
 */
data class Album(
    val bucketId: Long,
    val name: String,
    val cover: MediaItem,
    val count: Int,
)
