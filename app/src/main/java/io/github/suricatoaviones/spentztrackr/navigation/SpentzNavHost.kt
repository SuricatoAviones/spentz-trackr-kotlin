package io.github.suricatoaviones.spentztrackr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.suricatoaviones.spentztrackr.feature.home.HomeScreen

/**
 * Grafo de navegación raíz.
 *
 * En la Fase 1 el startDestination pasará a decidirse por el estado del
 * onboarding (DataStore); por ahora siempre entra al placeholder de Inicio.
 */
@Composable
fun SpentzNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
