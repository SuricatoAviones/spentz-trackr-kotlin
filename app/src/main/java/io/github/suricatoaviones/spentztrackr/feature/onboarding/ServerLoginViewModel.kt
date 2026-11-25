package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.data.remote.ApiError
import io.github.suricatoaviones.spentztrackr.data.session.LoginOutcome
import io.github.suricatoaviones.spentztrackr.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerLoginUiState(
    val url: String = "",
    val email: String = "",
    val password: String = "",
    val register: Boolean = false,
    val name: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: ApiError? = null,
    /** true tras conectar: la navegación reacciona (docs/04). */
    val done: Boolean = false,
)

/**
 * Formulario de conexión al servidor: login (o registro opcional). Cuando
 * `fromSettings` es true NO se completa el onboarding — es un cambio de
 * modo desde Ajustes.
 */
@HiltViewModel
class ServerLoginViewModel
    @Inject
    constructor(
        private val session: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ServerLoginUiState())
        val uiState: StateFlow<ServerLoginUiState> = _uiState.asStateFlow()

        fun onUrlChange(value: String) = _uiState.update { it.copy(url = value, error = null) }

        fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

        fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

        fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }

        fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, error = null) }

        fun onRegisterToggle(enabled: Boolean) = _uiState.update { it.copy(register = enabled, error = null) }

        fun submit(fromSettings: Boolean) {
            val state = _uiState.value
            if (state.loading || state.done) return

            val email = state.email.trim()
            if (state.url.isBlank() || email.isBlank() || state.password.isBlank()) return

            _uiState.update { it.copy(loading = true, error = null) }
            viewModelScope.launch {
                val outcome =
                    if (state.register) {
                        session.registerAndConnect(
                            rawUrl = state.url,
                            name = state.name.trim(),
                            email = email,
                            password = state.password,
                            passwordConfirmation = state.confirmPassword,
                        )
                    } else {
                        session.loginToServer(rawUrl = state.url, email = email, password = state.password)
                    }

                when (outcome) {
                    is LoginOutcome.Success -> {
                        if (!fromSettings) session.completeOnboarding()
                        _uiState.update { it.copy(loading = false, done = true) }
                    }
                    is LoginOutcome.Failure -> _uiState.update { it.copy(loading = false, error = outcome.error) }
                }
            }
        }
    }
