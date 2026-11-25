package io.github.suricatoaviones.spentztrackr.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.data.remote.dto.UserDto
import io.github.suricatoaviones.spentztrackr.data.session.SessionManager
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val mode: AppMode? = null,
    val serverUrl: String? = null,
    val serverEmail: String? = null,
    val remoteUser: UserDto? = null,
    val checkingSession: Boolean = false,
)

/**
 * Ajustes de la Fase 1 (stub): estado del modo y cambio local ↔ remoto.
 * El cambio de modo jamás toca datos (docs/04): solo la preferencia.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val session: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _needsLogin = Channel<Unit>(Channel.BUFFERED)
        val needsLogin = _needsLogin.receiveAsFlow()

        init {
            viewModelScope.launch {
                var lastMode: AppMode? = null
                session.settingsFlow.collect { settings ->
                    _uiState.update {
                        it.copy(
                            mode = settings.mode,
                            serverUrl = settings.serverUrl,
                            serverEmail = settings.serverEmail,
                        )
                    }
                    if (settings.mode == AppMode.REMOTE && lastMode != AppMode.REMOTE) {
                        refreshRemoteSession()
                    }
                    lastMode = settings.mode
                }
            }
        }

        /** Valida el token contra el servidor (GET auth/me). Null = sesión inválida. */
        fun refreshRemoteSession() {
            if (_uiState.value.mode != AppMode.REMOTE || _uiState.value.checkingSession) return
            _uiState.update { it.copy(checkingSession = true) }
            viewModelScope.launch {
                val user = session.validateRemoteSession()
                _uiState.update { it.copy(remoteUser = user, checkingSession = false) }
            }
        }

        fun switchToLocal() {
            viewModelScope.launch {
                session.switchToLocal()
                _uiState.update { it.copy(remoteUser = null, checkingSession = false) }
            }
        }

        /** Vuelve a remoto con la sesión guardada; si no hay, pide login. */
        fun switchToRemote() {
            viewModelScope.launch {
                if (!session.switchToRemote()) {
                    _needsLogin.send(Unit)
                }
            }
        }

        fun logoutRemote() {
            viewModelScope.launch {
                session.logoutRemote()
                _uiState.update { it.copy(remoteUser = null, checkingSession = false) }
            }
        }
    }
