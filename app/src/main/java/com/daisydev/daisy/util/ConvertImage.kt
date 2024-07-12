package com.daisydev.daisy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream

/**
 * Converts an [ImageProxy] to a [File].
 * @param context Application context.
 * @param image Image to convert.
 * @return [File] with the converted image.
 */
fun convertImage(context: Context, image: ImageProxy): File {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val file = File(context.cacheDir, "image.jpg")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
    }

    return file
}

/**
 * Gets the filename from a [Uri].
 * @param context Application context.
 * @param uri Uri to convert.
 * @return [String] with the filename.
 */
fun getFilenameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(
        uri, null, null, null, null
    )

    val filename = if (cursor != null && cursor.moveToFirst()) {
        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (columnIndex != -1) {
            cursor.getString(columnIndex)
        } else {
            null
        }
    } else {
        null
    }
    cursor?.close()

    return filename
}