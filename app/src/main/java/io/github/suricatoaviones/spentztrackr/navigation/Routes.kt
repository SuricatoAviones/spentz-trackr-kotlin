package io.github.suricatoaviones.spentztrackr.navigation

/**
 * Rutas de Compose Navigation.
 *
 * Fase 1: onboarding (welcome → modo → login servidor | setup local) + main
 * (inicio + ajustes). El login del servidor se reutiliza desde Ajustes para
 * el cambio de modo, de ahí el parámetro `fromSettings`.
 */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"

    const val WELCOME = "welcome"
    const val MODE_SELECTION = "mode-selection"
    const val LOCAL_SETUP = "local-setup"
    const val SERVER_LOGIN = "server-login/{fromSettings}"

    fun serverLogin(fromSettings: Boolean): String = "server-login/$fromSettings"

    /** Nombre del argumento de ruta de [SERVER_LOGIN]. */
    const val ARG_FROM_SETTINGS = "fromSettings"
}
