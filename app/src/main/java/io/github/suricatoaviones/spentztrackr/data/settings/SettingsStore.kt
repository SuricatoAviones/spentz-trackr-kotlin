package io.github.suricatoaviones.spentztrackr.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuente de verdad del modo y la sesión (docs/04, tabla de claves del
 * DataStore). Exposed como [AppSettings] para que nada fuera de esta clase
 * toque las claves crudas.
 */
@Singleton
class SettingsStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val settings: Flow<AppSettings> = dataStore.data.map(::toAppSettings)

        suspend fun snapshot(): AppSettings = settings.first()

        suspend fun setOnboardingCompleted() = edit { it[ONBOARDING_COMPLETED] = true }

        suspend fun setMode(mode: AppMode) = edit { it[MODE] = mode.value }

        suspend fun setServer(
            url: String,
            email: String,
        ) = edit {
            it[SERVER_URL] = url
            it[SERVER_EMAIL] = email
        }

        suspend fun clearServer() =
            edit {
                it.remove(SERVER_URL)
                it.remove(SERVER_EMAIL)
            }

        suspend fun setTheme(theme: AppTheme) = edit { it[THEME] = theme.value }

        suspend fun setLanguage(language: AppLanguage) = edit { it[LANGUAGE] = language.value }

        private suspend fun edit(transform: (MutablePreferences) -> Unit) {
            dataStore.edit(transform)
        }

        private fun toAppSettings(prefs: Preferences): AppSettings =
            AppSettings(
                onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false,
                mode = AppMode.fromValue(prefs[MODE]),
                serverUrl = prefs[SERVER_URL],
                serverEmail = prefs[SERVER_EMAIL],
                theme = AppTheme.fromValue(prefs[THEME]),
                language = AppLanguage.fromValue(prefs[LANGUAGE]),
            )

        private companion object {
            val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
            val MODE = stringPreferencesKey("mode")
            val SERVER_URL = stringPreferencesKey("server_url")
            val SERVER_EMAIL = stringPreferencesKey("server_email")
            val THEME = stringPreferencesKey("theme")
            val LANGUAGE = stringPreferencesKey("app_language")
        }
    }
