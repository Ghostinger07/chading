package com.chading.photos.data

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Scans the device for photos and videos via MediaStore. Everything stays on-device.
 */
class MediaStoreRepository(private val context: Context) {

    private val imagesUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    private val videosUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    /** Emits the current list of media each time MediaStore changes. */
    fun observeMedia(): Flow<List<MediaItem>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(imagesUri, true, observer)
        context.contentResolver.registerContentObserver(videosUri, true, observer)
        trySend(Unit)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.map { queryAll() }.flowOn(Dispatchers.IO)

    suspend fun loadAll(): List<MediaItem> = withContext(Dispatchers.IO) { queryAll() }

    @SuppressLint("InlinedApi")
    private fun queryAll(): List<MediaItem> {
        val items = ArrayList<MediaItem>(1024)
        items += query(imagesUri, isVideo = false)
        items += query(videosUri, isVideo = true)
        items.sortByDescending { it.dateTakenMillis }
        return items
    }

    @SuppressLint("InlinedApi")
    private fun query(collection: Uri, isVideo: Boolean): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DURATION,
        )

        val result = ArrayList<MediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val dateTakenCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_TAKEN)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val bucketIdCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val relativePathCol = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            val durationCol = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val dateTaken = dateTakenCol
                    .takeIf { it != -1 }
                    ?.let { cursor.getLongOrNullSafe(it) }
                    ?: (cursor.getLong(dateAddedCol) * 1000L)
                    .takeIf { it > 0 }
                    ?: (cursor.getLong(dateModifiedCol) * 1000L)

                result += MediaItem(
                    id = id,
                    uri = uri,
                    displayName = cursor.getString(nameCol) ?: "",
                    dateTakenMillis = dateTaken,
                    mimeType = cursor.getString(mimeCol) ?: if (isVideo) "video/*" else "image/*",
                    width = if (widthCol != -1) cursor.getInt(widthCol) else 0,
                    height = if (heightCol != -1) cursor.getInt(heightCol) else 0,
                    sizeBytes = cursor.getLong(sizeCol),
                    durationMillis = if (durationCol != -1) cursor.getLong(durationCol) else 0L,
                    bucketId = if (bucketIdCol != -1) cursor.getLong(bucketIdCol) else 0L,
                    bucketName = (if (bucketNameCol != -1) cursor.getString(bucketNameCol) else null) ?: "Unknown",
                    relativePath = (if (relativePathCol != -1) cursor.getString(relativePathCol) else null) ?: "",
                )
            }
        }
        return result
    }

    private fun android.database.Cursor.getLongOrNullSafe(index: Int): Long? =
        if (isNull(index)) null else getLong(index)
}

/** Groups a flat media list into albums keyed by bucketId with newest cover first. */
fun List<MediaItem>.toAlbums(): List<Album> {
    if (isEmpty()) return emptyList()
    val byBucket = groupBy { it.bucketId }
    return byBucket.map { (id, items) ->
        val sorted = items.sortedByDescending { it.dateTakenMillis }
        Album(
            bucketId = id,
            name = sorted.first().bucketName,
            cover = sorted.first(),
            count = sorted.size,
        )
    }.sortedByDescending { it.cover.dateTakenMillis }
}
