package com.m4ykey.snofy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.m4ykey.snofy.ui.home.HomeScreen
import com.m4ykey.snofy.ui.theme.SnofyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnofyTheme {
                HomeScreen()
            }
        }
    }
}