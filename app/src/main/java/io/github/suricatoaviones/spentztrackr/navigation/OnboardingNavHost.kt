package io.github.suricatoaviones.spentztrackr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.feature.onboarding.LocalSetupScreen
import io.github.suricatoaviones.spentztrackr.feature.onboarding.ModeSelectionScreen
import io.github.suricatoaviones.spentztrackr.feature.onboarding.ServerLoginScreen
import io.github.suricatoaviones.spentztrackr.feature.onboarding.WelcomeScreen

/**
 * Flujo de primer arranque (docs/04). Al completarse, `onboarding_completed`
 * pasa a true y [RootApp] conmuta solo a [MainNavHost]: ninguna pantalla
 * navega "hacia main" a mano.
 */
@Composable
fun OnboardingNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStart = {
                    navController.navigate(Routes.MODE_SELECTION) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.MODE_SELECTION) {
            ModeSelectionScreen(
                onChoose = { mode ->
                    when (mode) {
                        AppMode.LOCAL -> navController.navigate(Routes.LOCAL_SETUP)
                        AppMode.REMOTE -> navController.navigate(Routes.serverLogin(fromSettings = false))
                    }
                },
            )
        }
        composable(Routes.LOCAL_SETUP) {
            LocalSetupScreen()
        }
        serverLoginDestination(navController)
    }
}

/** Destino compartido: login del servidor, desde onboarding o desde Ajustes. */
internal fun NavGraphBuilder.serverLoginDestination(navController: NavHostController) {
    composable(
        route = Routes.SERVER_LOGIN,
        arguments = listOf(navArgument(Routes.ARG_FROM_SETTINGS) { type = NavType.StringType }),
    ) { entry ->
        val fromSettings = entry.arguments?.getString(Routes.ARG_FROM_SETTINGS) == "true"
        ServerLoginScreen(
            fromSettings = fromSettings,
            onConnected = {
                // Vuelve a Ajustes (o al flujo previo); si vino del onboarding,
                // el flip de onboarding_completed ya conmutó la raíz.
                navController.popBackStack()
            },
        )
    }
}
