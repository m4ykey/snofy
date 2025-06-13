package com.m4ykey.snofy.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.m4ykey.snofy.download.downloadVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _downloadMessage = MutableStateFlow<String?>(null)
    val downloadMessage = _downloadMessage.asStateFlow()

    fun startDownload(context : Context, url : String) {
        downloadVideo(
            context = context,
            url = url,
            onProgress = { progress ->
                _downloadProgress.value = progress
            },
            onError = { error ->
                _downloadMessage.value = error
                _downloadProgress.value = null
            },
            onSuccess = { msg ->
                _downloadProgress.value = null
                _downloadMessage.value = msg
            }
        )
    }
}