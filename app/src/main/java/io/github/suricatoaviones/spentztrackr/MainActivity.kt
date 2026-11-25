package io.github.suricatoaviones.spentztrackr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.suricatoaviones.spentztrackr.core.designsystem.theme.SpentzTheme
import io.github.suricatoaviones.spentztrackr.navigation.RootApp

/**
 * Único Activity de la app: host de la UI de Compose.
 * El gating onboarding/módulos vive en la navegación (RootApp).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpentzTheme {
                RootApp()
            }
        }
    }
}
