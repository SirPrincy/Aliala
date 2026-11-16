package com.example.ui.business.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object BusinessImageStorage {
    private const val PHOTOS_DIR = "product_photos"
    private const val MAX_DIMENSION_PX = 1200

    fun isPhotoUriOrPath(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return false
        return when {
            trimmed.startsWith("file://") -> {
                val path = runCatching { Uri.parse(trimmed).path }.getOrNull()
                !path.isNullOrBlank() && File(path).exists()
            }
            trimmed.startsWith("/") -> File(trimmed).exists()
            trimmed.startsWith("content://") || trimmed.startsWith("http://") || trimmed.startsWith("https://") -> true
            else -> false
        }
    }

    suspend fun persistPickedPhoto(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val photosDir = File(context.filesDir, PHOTOS_DIR).apply {
                if (!exists()) mkdirs()
            }
            val destFile = File(photosDir, "img_${UUID.randomUUID()}.jpg")

            val decoded = decodeSampledAndOrientedBitmap(context, sourceUri)
            if (decoded != null) {
                try {
                    FileOutputStream(destFile).use { out ->
                        decoded.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        out.flush()
                    }
                } finally {
                    decoded.recycle()
                }
            } else {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                } ?: return@withContext null
            }

            if (destFile.exists() && destFile.length() > 0L) {
                Uri.fromFile(destFile).toString()
            } else {
                destFile.delete()
                null
            }
        }.getOrNull()
    }

    private fun decodeSampledAndOrientedBitmap(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: return null

        var sampleSize = 1
        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
        if (maxSide > MAX_DIMENSION_PX) {
            while ((maxSide / sampleSize) > MAX_DIMENSION_PX) {
                sampleSize *= 2
            }
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: return null

        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { exifStream ->
                ExifInterface(exifStream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees == 0f) return rawBitmap

        return runCatching {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            if (rotated != rawBitmap) rawBitmap.recycle()
            rotated
        }.getOrDefault(rawBitmap)
    }
}
