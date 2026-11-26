package io.github.suricatoaviones.spentztrackr.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.data.remote.dto.UserDto
import io.github.suricatoaviones.spentztrackr.data.session.SessionManager
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import io.github.suricatoaviones.spentztrackr.domain.model.UserPreferences
import io.github.suricatoaviones.spentztrackr.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Resultado efímero de una escritura de preferencias (se muestra inline). */
enum class SettingsFeedback { SAVED, INVALID_BUDGET, INVALID_COMMISSION, SAVE_ERROR }

data class SettingsUiState(
    val mode: AppMode? = null,
    val serverUrl: String? = null,
    val serverEmail: String? = null,
    val remoteUser: UserDto? = null,
    val checkingSession: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val saving: Boolean = false,
    val feedback: SettingsFeedback? = null,
)

/**
 * Ajustes completos (docs/06, Fase 6): modo/sesión, moneda de visualización,
 * `tracking_type`, presupuesto, comisiones, tema e idioma. Local escribe en
 * `user_prefs`/DataStore; remoto manda moneda y tracking a `PUT /user/profile`.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val session: SessionManager,
        private val preferences: UserPreferencesRepository,
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
            viewModelScope.launch {
                preferences
                    .observe()
                    .catch { emit(UserPreferences()) }
                    .collect { prefs ->
                        _uiState.update { it.copy(preferences = prefs) }
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

        fun setDisplayCurrency(currency: DisplayCurrency) = persist { preferences.setDisplayCurrency(currency) }

        fun setTrackingType(type: TrackingType) = persist { preferences.setTrackingType(type) }

        fun setTheme(theme: AppTheme) = persist { preferences.setTheme(theme) }

        fun setLanguage(language: AppLanguage) = persist { preferences.setLanguage(language) }

        fun saveBudget(raw: String) {
            val trimmed = raw.trim().replace(',', '.')
            val budget = if (trimmed.isEmpty()) null else Money.parseOrNull(trimmed)
            if (trimmed.isNotEmpty() && budget == null) {
                _uiState.update { it.copy(feedback = SettingsFeedback.INVALID_BUDGET) }
                return
            }
            persist { preferences.setMonthlyBudget(budget) }
        }

        fun saveCommission(
            minRaw: String,
            rateRaw: String,
        ) {
            val min = Money.parseOrNull(minRaw.trim().replace(',', '.'))
            val rate = rateRaw.trim().replace(',', '.').toBigDecimalOrNull()
            if (min == null || min <= Money.ZERO || rate == null || rate.signum() < 0) {
                _uiState.update { it.copy(feedback = SettingsFeedback.INVALID_COMMISSION) }
                return
            }
            persist { preferences.setCommission(min, rate) }
        }

        fun clearFeedback() {
            _uiState.update { it.copy(feedback = null) }
        }

        private fun persist(block: suspend () -> Unit) {
            if (_uiState.value.saving) return
            _uiState.update { it.copy(saving = true, feedback = null) }
            viewModelScope.launch {
                val result = runCatching { block() }
                _uiState.update {
                    it.copy(
                        saving = false,
                        feedback = if (result.isSuccess) SettingsFeedback.SAVED else SettingsFeedback.SAVE_ERROR,
                    )
                }
            }
        }
    }
