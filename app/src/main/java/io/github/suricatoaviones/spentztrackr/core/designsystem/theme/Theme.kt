package io.github.suricatoaviones.spentztrackr.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Tema raíz de la app. Modo claro/oscuro según el sistema;
 * el override manual de apariencia llega con los Ajustes (Fase 6).
 */
@Composable
fun SpentzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SpentzTypography,
        content = content,
    )
}
