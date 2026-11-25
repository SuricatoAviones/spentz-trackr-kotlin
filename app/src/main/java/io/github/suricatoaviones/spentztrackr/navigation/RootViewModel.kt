package io.github.suricatoaviones.spentztrackr.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.data.session.SessionManager
import io.github.suricatoaviones.spentztrackr.data.settings.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Settings para el gating de la raíz. `null` = aún no hay emisión de DataStore
 * (arranque): la UI muestra splash en lugar de "adivinar" el estado.
 */
@HiltViewModel
class RootViewModel
    @Inject
    constructor(
        session: SessionManager,
    ) : ViewModel() {
        val settings: StateFlow<AppSettings?> =
            session.settingsFlow
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }
