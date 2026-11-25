package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.data.session.SessionManager
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.LocalSetup
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocalSetupUiState(
    val displayCurrency: DisplayCurrency = DisplayCurrency.USD,
    val trackingType: TrackingType = TrackingType.BOTH,
    val budgetText: String = "",
    /** true si el presupuesto escrito no es un número válido. */
    val budgetError: Boolean = false,
    val loading: Boolean = false,
    val done: Boolean = false,
)

/**
 * Rama local del onboarding: preferencias iniciales (todo con defaults
 * razonables) y creación de los datos por defecto (docs/04).
 */
@HiltViewModel
class LocalSetupViewModel
    @Inject
    constructor(
        private val session: SessionManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LocalSetupUiState())
        val uiState: StateFlow<LocalSetupUiState> = _uiState.asStateFlow()

        fun onCurrencyChange(currency: DisplayCurrency) = _uiState.update { it.copy(displayCurrency = currency) }

        fun onTrackingChange(tracking: TrackingType) = _uiState.update { it.copy(trackingType = tracking) }

        fun onBudgetChange(value: String) = _uiState.update { it.copy(budgetText = value, budgetError = false) }

        fun submit() {
            val state = _uiState.value
            if (state.loading || state.done) return

            val budget =
                state.budgetText
                    .trim()
                    .replace(',', '.')
                    .takeIf { it.isNotEmpty() }
            if (budget != null && budget.toBigDecimalOrNull() == null) {
                _uiState.update { it.copy(budgetError = true) }
                return
            }

            _uiState.update { it.copy(loading = true) }
            viewModelScope.launch {
                session.setupLocal(
                    LocalSetup(
                        displayCurrency = state.displayCurrency,
                        trackingType = state.trackingType,
                        monthlyBudget = budget,
                    ),
                )
                session.completeOnboarding()
                _uiState.update { it.copy(loading = false, done = true) }
            }
        }
    }
