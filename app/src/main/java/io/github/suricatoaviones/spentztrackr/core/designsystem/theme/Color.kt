package io.github.suricatoaviones.spentztrackr.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de Spentz Trackr: semilla verde (finanzas).
 *
 * Los roles no listados los resuelve Material 3 con sus defaults, de modo que
 * la app queda bien tanto en claro como en oscuro sin mantener 30 colores a mano.
 * Cuando el proyecto web defina branding oficial, esta paleta se ajusta aquí.
 */

val LightColors =
    lightColorScheme(
        primary = Color(0xFF006C3C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF9BF6B5),
        onPrimaryContainer = Color(0xFF00210E),
        secondary = Color(0xFF4F6355),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD2E8D7),
        onSecondaryContainer = Color(0xFF101F13),
        tertiary = Color(0xFF3B6470),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBFE9F8),
        onTertiaryContainer = Color(0xFF001F28),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF6FBF4),
        onBackground = Color(0xFF171D19),
        surface = Color(0xFFF6FBF4),
        onSurface = Color(0xFF171D19),
        surfaceVariant = Color(0xFFDDE5DC),
        onSurfaceVariant = Color(0xFF414942),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF0F5EF),
        surfaceContainer = Color(0xFFEAF0E9),
        surfaceContainerHigh = Color(0xFFE4EAE3),
        surfaceContainerHighest = Color(0xFFDEE4DD),
        outline = Color(0xFF717971),
        outlineVariant = Color(0xFFC0C9C0),
    )

val DarkColors =
    darkColorScheme(
        primary = Color(0xFF7FDBA1),
        onPrimary = Color(0xFF00391D),
        primaryContainer = Color(0xFF00522C),
        onPrimaryContainer = Color(0xFF9BF6B5),
        secondary = Color(0xFFB3CCBB),
        onSecondary = Color(0xFF1F3328),
        secondaryContainer = Color(0xFF354A3D),
        onSecondaryContainer = Color(0xFFD2E8D7),
        tertiary = Color(0xFFA3CEDD),
        onTertiary = Color(0xFF043543),
        tertiaryContainer = Color(0xFF224C5A),
        onTertiaryContainer = Color(0xFFBFE9F8),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0F1511),
        onBackground = Color(0xFFDFE4DE),
        surface = Color(0xFF0F1511),
        onSurface = Color(0xFFDFE4DE),
        surfaceVariant = Color(0xFF414942),
        onSurfaceVariant = Color(0xFFC0C9C0),
        surfaceContainerLowest = Color(0xFF0A0F0C),
        surfaceContainerLow = Color(0xFF171D19),
        surfaceContainer = Color(0xFF1B211D),
        surfaceContainerHigh = Color(0xFF252B27),
        surfaceContainerHighest = Color(0xFF303632),
        outline = Color(0xFF8A938C),
        outlineVariant = Color(0xFF414942),
    )
