package io.github.suricatoaviones.spentztrackr.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.feature.home.HomeScreen
import io.github.suricatoaviones.spentztrackr.feature.settings.SettingsScreen

/**
 * App principal tras el onboarding: barra inferior Inicio/Ajustes (Fase 1;
 * Gastos/Ingresos llegan con sus fases, según `tracking_type`).
 */
@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                    onClick = { navController.navigateToTab(Routes.HOME) },
                    icon = {
                        Icon(
                            imageVector =
                                if (currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true) {
                                    Icons.Filled.Home
                                } else {
                                    Icons.Outlined.Home
                                },
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.nav_home)) },
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
                    onClick = { navController.navigateToTab(Routes.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector =
                                if (currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true) {
                                    Icons.Filled.Settings
                                } else {
                                    Icons.Outlined.Settings
                                },
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onConnectServer = { navController.navigate(Routes.serverLogin(fromSettings = true)) },
                )
            }
            serverLoginDestination(navController)
        }
    }
}

/** Patrón estándar de tabs: singleTop + estado salvado/restaurado. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
