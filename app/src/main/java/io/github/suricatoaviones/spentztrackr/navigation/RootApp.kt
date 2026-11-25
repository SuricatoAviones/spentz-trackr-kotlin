package io.github.suricatoaviones.spentztrackr.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.core.designsystem.SpentzMonogram

/**
 * Raíz de la app (docs/04, diagrama de arranque):
 * splash mientras carga el estado → onboarding si no está completado → main.
 * El "modo" no decide la pantalla raíz: decide qué datos ven las pantallas.
 */
@Composable
fun RootApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val settings by rootViewModel.settings.collectAsStateWithLifecycle()

    when (val current = settings) {
        null -> SpentzSplash()
        else ->
            if (current.onboardingCompleted) {
                MainNavHost()
            } else {
                OnboardingNavHost()
            }
    }
}

@Composable
private fun SpentzSplash() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            SpentzMonogram()
        }
    }
}
