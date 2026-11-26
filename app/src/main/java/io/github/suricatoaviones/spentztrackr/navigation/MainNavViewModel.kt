package io.github.suricatoaviones.spentztrackr.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.domain.model.UserPreferences
import io.github.suricatoaviones.spentztrackr.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Preferencias que gobiernan las pestañas visibles (`tracking_type`). */
@HiltViewModel
class MainNavViewModel
    @Inject
    constructor(
        preferences: UserPreferencesRepository,
    ) : ViewModel() {
        val preferences: StateFlow<UserPreferences?> =
            preferences
                .observe()
                .catch { emit(UserPreferences()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }
