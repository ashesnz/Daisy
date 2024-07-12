package com.daisydev.daisy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.daisydev.daisy.ui.components.Layout
import com.daisydev.daisy.ui.theme.DaisyTheme
import dagger.hilt.android.AndroidEntryPoint

// Main activity of the application
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaisyTheme {
                // A Surface is a container that applies the Material Design theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Layout() // Main layout of the application
                }
            }
        }
    }
}