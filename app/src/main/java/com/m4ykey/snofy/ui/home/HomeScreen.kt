package com.m4ykey.snofy.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {

    val progress by viewModel.downloadProgress.collectAsState()
    val message by viewModel.downloadMessage.collectAsState()
    var url by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
                SearchBox(value = url, onValueChange = { url = it })
                Spacer(modifier = modifier.height(10.dp))

                DownloadButton(url = url)

                Spacer(modifier = modifier.height(10.dp))

                progress?.let {
                    LinearProgressIndicator(
                        progress = { it.coerceIn(0, 100) / 100f },
                        modifier = modifier,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$it%")
                }
            }
        }
    }
}

@Composable
fun DownloadButton(
    url: String,
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startDownload(context = context, url = url)
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Button(onClick = {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(permission)
            } else {
                viewModel.startDownload(context, url)
            }
        } else {
            viewModel.startDownload(context, url)
        }
    }) {
        Text(text = "Download")
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
    value: String,
    onValueChange: (String) -> Unit
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