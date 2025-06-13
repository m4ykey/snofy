package com.m4ykey.snofy.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun downloadVideo(
    context : Context,
    url : String,
    onSuccess : (String) -> Unit,
    onError : (String) -> Unit,
    onProgress : (Int) -> Unit
) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    appScope.launch {
        try {
            val outputPath : String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "youtube_audio_${System.currentTimeMillis()}.mp3")
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IOException("Failed to create new MediaStore record.")

                val fileDescriptor = resolver.openFileDescriptor(uri, "w")?.fileDescriptor
                    ?: throw IOException("Failed to open file descriptor.")

                val tempFile = File(context.cacheDir, "temp.mp3")
                val request = YoutubeDLRequest(url).apply {
                    addOption("-f", "bestaudio")
                    addOption("-x")
                    addOption("--audio-format", "mp3")
                    addOption("-o", tempFile.absolutePath)
                }

                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    onProgress(progress.toInt())
                }

                FileInputStream(tempFile).use { input ->
                    FileOutputStream(fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }

                tempFile.delete()

                "Downloaded to Download/ by MediaStore"
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val filePath = "${downloadsDir.absolutePath}/youtube_audio_${System.currentTimeMillis()}.mp3"
                val request = YoutubeDLRequest(url).apply {
                    addOption("-f", "bestaudio")
                    addOption("-x")
                    addOption("--audio-format", "mp3")
                    addOption("-o", filePath)
                }

                YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                    onProgress(progress.toInt())
                }

                filePath
            }

            withContext(Dispatchers.Main) {
                onSuccess("Success: $outputPath")
            }
        } catch (e : Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError("Download failed: ${e.message}")
            }
        }
    }
}