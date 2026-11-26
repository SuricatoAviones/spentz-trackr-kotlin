package io.github.suricatoaviones.spentztrackr.navigation

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.designsystem.SpentzMonogram
import io.github.suricatoaviones.spentztrackr.core.designsystem.theme.SpentzTheme
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import java.util.Locale

/**
 * Raíz de la app (docs/04, diagrama de arranque):
 * splash mientras carga el estado → onboarding si no está completado → main.
 * El "modo" no decide la pantalla raíz: decide qué datos ven las pantallas.
 * También aplica el tema y el idioma elegidos en Ajustes (Fase 6).
 */
@Composable
fun RootApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val settings by rootViewModel.settings.collectAsStateWithLifecycle()
    val theme = settings?.theme ?: AppTheme.SYSTEM
    val language = settings?.language ?: AppLanguage.SYSTEM

    val darkTheme =
        when (theme) {
            AppTheme.SYSTEM -> isSystemInDarkTheme()
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
        }

    SpentzTheme(darkTheme = darkTheme) {
        AppLocale(language) {
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
    }
}

/**
 * Override de locale para la UI (tema/idioma en DataStore). Los formateadores
 * (`DateText`, `MoneyFormat`) usan `Locale.getDefault()`, por eso también se
 * ajusta el default del proceso mientras la app está en primer plano.
 */
@Composable
private fun AppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemLocale = remember { Locale.getDefault() }
    val localeTag =
        when (language) {
            AppLanguage.SYSTEM -> null
            AppLanguage.SPANISH -> "es"
            AppLanguage.ENGLISH -> "en"
        }

    if (localeTag == null) {
        LaunchedEffect(Unit) { Locale.setDefault(systemLocale) }
        content()
    } else {
        val locale = remember(localeTag) { Locale.forLanguageTag(localeTag) }
        val currentConfiguration = LocalConfiguration.current
        val localizedContext =
            remember(context, localeTag, currentConfiguration) {
                val configuration = Configuration(currentConfiguration)
                configuration.setLocale(locale)
                context.createConfigurationContext(configuration)
            }
        LaunchedEffect(localeTag) { Locale.setDefault(locale) }
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedContext.resources.configuration,
            content = content,
        )
    }
}

@Composable
private fun SpentzSplash() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            SpentzMonogram(contentDescription = stringResource(R.string.app_name))
        }
    }
}
