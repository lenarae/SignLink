package com.signlink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.navigation.Screen
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
            // Collect settings as Compose State
            val settings by settingsDataStore.settings.collectAsState(initial = null)
            if (settings == null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SignLinkTheme(
                    themeMode     = settings!!.theme,
                    textSizeScale = settings!!.textSizeScale
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        SignLinkNavGraph(
                            navController    = navController,
                            startDestination = Screen.Welcome.route
                        )
                    }
                }
            }
        }
    }
}