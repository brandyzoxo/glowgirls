package com.example.glowgirls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.glowgirls.ui.theme.GlowGirlsTheme

import android.os.Build
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.glowgirls.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlowGirlsTheme {
                // Start navigation with AppNavHost
                AppNavHost()  // This is where the app's navigation begins
            }
        }
    }
}
