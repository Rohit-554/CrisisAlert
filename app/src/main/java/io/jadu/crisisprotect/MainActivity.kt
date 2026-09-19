package io.jadu.crisisprotect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import io.jadu.crisisprotect.ui.theme.CrisisProtectTheme
import io.jadu.crisisprotect.navigation.CrisisProtectApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrisisProtectTheme {
                Surface { CrisisProtectApp() }
            }
        }
    }
}
