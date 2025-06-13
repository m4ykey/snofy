package com.m4ykey.snofy.ui.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    var text by remember { mutableStateOf("") }

    Scaffold { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBox(
                    value = text,
                    onValueChange = { text = it }
                )
                Spacer(modifier = modifier.height(10.dp))
                DownloadButton(url = text)
                Spacer(modifier = modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp)
            }
        }
    }
}

@Composable
fun DownloadButton(url : String) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            downloadVideo(
                context = context,
                url = url,
                onSuccess = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            )
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Button(onClick = {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            } else {
                downloadVideo(
                    context = context,
                    url = url,
                    onSuccess = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                    onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                )
            }
        } else {
            downloadVideo(
                context = context,
                url = url,
                onSuccess = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            )
        }
    }) {
        Text(text = "Download")
    }
}

fun downloadVideo(
    context : Context,
    url : String,
    onSuccess : (String) -> Unit,
    onError : (String) -> Unit
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

                YoutubeDL.getInstance().execute(request)

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

                YoutubeDL.getInstance().execute(request)
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

@Preview
@Composable
private fun HomeScreenPrev() {
    HomeScreen()
}

@Composable
fun SearchBox(
    modifier: Modifier = Modifier,
    value : String,
    onValueChange : (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        },
        placeholder = {
            Text(text = "Enter URL")
        }
    )
}