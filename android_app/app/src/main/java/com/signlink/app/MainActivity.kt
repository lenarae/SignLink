// ============================================================
// File: MainActivity.kt  [FINAL — Phase 8]
// Now reads dark mode + high contrast from DataStore so the
// theme updates instantly when settings change.
// ============================================================

package com.signlink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.navigation.SignLinkNavGraph
import com.signlink.app.ui.theme.SignLinkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject DataStore so we can observe dark mode / high contrast live
    @Inject
    lateinit var settingsDataStore: AppSettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Collect settings as Compose State — any change triggers recomposition
            val settings by settingsDataStore.settings.collectAsState(
                initial = com.signlink.app.data.local.AppSettings()
            )

            SignLinkTheme(
                themeMode     = settings.theme,
                textSizeScale = settings.textSizeScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SignLinkNavGraph(navController = navController)
                }
            }
        }
    }
}