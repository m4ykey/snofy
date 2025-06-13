package com.m4ykey.snofy

import android.app.Application
import android.util.Log
import com.m4ykey.snofy.di.modules
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SnofyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SnofyApp)
            modules(modules)
        }

        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        appScope.launch {
            try {
                YoutubeDL.getInstance().init(this@SnofyApp)
                FFmpeg.getInstance().init(this@SnofyApp)
            } catch (e : Exception) {
                Log.i("SnofyApp", "Initialization error: ${e.message}", e)
            }
        }
    }
}