package io.github.suricatoaviones.spentztrackr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.suricatoaviones.spentztrackr.navigation.RootApp

/**
 * Única Activity de la app: host de la UI de Compose.
 * El gating onboarding/módulos y el tema/idioma viven en [RootApp].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RootApp()
        }
    }
}
