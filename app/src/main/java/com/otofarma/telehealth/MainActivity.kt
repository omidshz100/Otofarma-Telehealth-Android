package com.otofarma.telehealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.otofarma.telehealth.ui.AppRoot
import com.otofarma.telehealth.ui.theme.TelehealthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TelehealthTheme {
                AppRoot()
            }
        }
    }
}
