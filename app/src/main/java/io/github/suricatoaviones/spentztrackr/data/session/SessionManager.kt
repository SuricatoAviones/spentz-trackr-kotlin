package io.github.suricatoaviones.spentztrackr.data.session

import io.github.suricatoaviones.spentztrackr.data.local.LocalInitializer
import io.github.suricatoaviones.spentztrackr.data.remote.ApiError
import io.github.suricatoaviones.spentztrackr.data.remote.ApiErrorMapper
import io.github.suricatoaviones.spentztrackr.data.remote.ServerUrlNormalizer
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.dto.AuthTokenData
import io.github.suricatoaviones.spentztrackr.data.remote.dto.LoginRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.RegisterRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.UserDto
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.data.token.TokenStore
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.AppSettings
import io.github.suricatoaviones.spentztrackr.domain.model.LocalSetup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orquesta el estado de sesión de la app (onboarding, modos, login).
 * La UI nunca toca stores ni la API directamente: pide aquí (docs/04).
 *
 * La persistencia de la sesión es intencionalmente tolerante: la URL se
 * guarda *antes* de llamar a la API para que el interceptor ya apunte al
 * servidor correcto, y tras un login válido se refina con el email real.
 */
@Singleton
class SessionManager
    @Inject
    constructor(
        private val settings: SettingsStore,
        private val tokens: TokenStore,
        private val api: SpentzApi,
        private val localInitializer: LocalInitializer,
        private val apiErrors: ApiErrorMapper,
    ) {
        val settingsFlow: Flow<AppSettings> get() = settings.settings

        /** Marca el onboarding como completado (solo lo decide la UI del flujo). */
        suspend fun completeOnboarding() = settings.setOnboardingCompleted()

        /** Login contra el backend. Requiere URL + credenciales válidas. */
        suspend fun loginToServer(
            rawUrl: String,
            email: String,
            password: String,
        ): LoginOutcome {
            val url =
                ServerUrlNormalizer.normalize(rawUrl)
                    ?: return LoginOutcome.Failure(ApiError.InvalidUrl)
            return connect(url) { api.login(LoginRequest(email = email, password = password, deviceName = DEVICE_NAME)) }
        }

        /** Registro + conexión inmediata (CreateNewUser del backend exige confirmación). */
        suspend fun registerAndConnect(
            rawUrl: String,
            name: String,
            email: String,
            password: String,
            passwordConfirmation: String,
        ): LoginOutcome {
            val url =
                ServerUrlNormalizer.normalize(rawUrl)
                    ?: return LoginOutcome.Failure(ApiError.InvalidUrl)
            return connect(url) {
                api.register(
                    RegisterRequest(
                        name = name,
                        email = email,
                        password = password,
                        passwordConfirmation = passwordConfirmation,
                        deviceName = DEVICE_NAME,
                    ),
                )
            }
        }

        /** Rama local del onboarding: datos por defecto + preferencias + modo. */
        suspend fun setupLocal(setup: LocalSetup) {
            localInitializer.initialize(setup)
            settings.setMode(AppMode.LOCAL)
        }

        /** Cambio de modo desde Ajustes: nada se borra, nada se migra (docs/04). */
        suspend fun switchToLocal() = settings.setMode(AppMode.LOCAL)

        /**
         * Vuelve a remoto usando la sesión guardada (URL + token).
         * `false` ⇒ hay que pedir login de nuevo.
         */
        suspend fun switchToRemote(): Boolean {
            val snapshot = settings.snapshot()
            if (snapshot.serverUrl == null || tokens.token() == null) return false
            settings.setMode(AppMode.REMOTE)
            return true
        }

        /** Valida el token contra el servidor (`GET auth/me`). Null = sesión inválida. */
        suspend fun validateRemoteSession(): UserDto? =
            try {
                api.me().data?.also { user -> settings.setServer(settings.snapshot().serverUrl.orEmpty(), user.email) }
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                null
            }

        /** Cierra sesión: revoca el token en el servidor (best-effort) y desconecta. */
        suspend fun logoutRemote() {
            try {
                api.logout()
            } catch (_: Throwable) {
                // Best-effort: si no hay red, el token local se descarta igualmente.
            }
            tokens.clear()
            settings.clearServer()
            settings.setMode(AppMode.LOCAL)
        }

        private suspend fun connect(
            url: String,
            call: suspend () -> io.github.suricatoaviones.spentztrackr.data.remote.dto.ApiEnvelope<AuthTokenData>,
        ): LoginOutcome {
            settings.setServer(url, "")
            return try {
                val response = call()
                val data = response.data ?: return LoginOutcome.Failure(ApiError.Unexpected(IllegalStateException("Respuesta sin data")))
                tokens.save(data.token)
                settings.setServer(url, data.user.email)
                settings.setMode(AppMode.REMOTE)
                LoginOutcome.Success(data.user)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                LoginOutcome.Failure(apiErrors.map(t))
            }
        }

        private companion object {
            const val DEVICE_NAME = "android"
        }
    }

sealed interface LoginOutcome {
    data class Success(
        val user: UserDto,
    ) : LoginOutcome

    data class Failure(
        val error: ApiError,
    ) : LoginOutcome
}
