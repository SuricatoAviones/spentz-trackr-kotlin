package io.github.suricatoaviones.spentztrackr.feature.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/** Fila del histórico (un día con sus tres tasas, las que existan). */
data class RateHistoryRow(
    val date: LocalDate,
    val bcv: String? = null,
    val paralelo: String? = null,
    val manual: String? = null,
)

data class RatesUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val localMode: Boolean = true,
    val bcv: String? = null,
    val paralelo: String? = null,
    val manual: String? = null,
    /** true si las tarjetas muestran la última tasa conocida (no la de hoy). */
    val estimated: Boolean = false,
    val updatedAt: Long? = null,
    val history: List<RateHistoryRow> = emptyList(),
    val manualText: String = "",
    val manualError: Boolean = false,
    val manualSaved: Boolean = false,
    val syncing: Boolean = false,
    val syncFailed: Boolean = false,
)

private data class ManualState(
    val text: String = "",
    val error: Boolean = false,
    val saved: Boolean = false,
)

private data class RatesData(
    val mode: AppMode?,
    val snapshot: RateSnapshot,
    val history: List<RateHistoryRow>,
)

/**
 * Pantalla de Tasas (docs/06): tarjetas BCV/Paralelo, override manual del día,
 * histórico de 30 días y "Actualizar ahora" (local: dolarapi; remoto:
 * `POST /rates/sync`).
 */
@HiltViewModel
class RatesViewModel
    @Inject
    constructor(
        private val rates: RateRepository,
        private val settings: SettingsStore,
    ) : ViewModel() {
        private val manualState = MutableStateFlow(ManualState())
        private val syncing = MutableStateFlow(false)
        private val syncFailed = MutableStateFlow(false)

        private val data: Flow<RatesData> =
            combine(
                settings.settings.map { it.mode },
                rates.observeSnapshot(),
                rates.observeHistory(HISTORY_DAYS),
            ) { mode, snapshot, history ->
                RatesData(mode = mode, snapshot = snapshot, history = history.toHistoryRows())
            }.catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(RatesData(mode = null, snapshot = RateSnapshot(), history = emptyList()))
            }

        val uiState: StateFlow<RatesUiState> =
            combine(data, manualState, syncing, syncFailed) { data, manual, isSyncing, failed ->
                RatesUiState(
                    loading = false,
                    localMode = data.mode != AppMode.REMOTE,
                    bcv = data.snapshot.bcv?.toCanonicalString(),
                    paralelo = data.snapshot.paralelo?.toCanonicalString(),
                    manual = data.snapshot.manual?.toCanonicalString(),
                    estimated = data.snapshot.estimated,
                    updatedAt = data.snapshot.updatedAt,
                    history = data.history,
                    manualText = manual.text,
                    manualError = manual.error,
                    manualSaved = manual.saved,
                    syncing = isSyncing,
                    syncFailed = failed,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RatesUiState(),
            )

        fun onManualTextChange(value: String) {
            manualState.value = ManualState(text = value.filterInput())
        }

        fun saveManualRate() {
            val rate = Rate.parseOrNull(manualState.value.text)
            if (rate == null) {
                manualState.update { it.copy(error = true, saved = false) }
                return
            }
            viewModelScope.launch {
                try {
                    rates.saveManualRate(rate)
                    manualState.update { it.copy(error = false, saved = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    manualState.update { it.copy(error = true, saved = false) }
                }
            }
        }

        fun consumeManualSaved() = manualState.update { it.copy(saved = false) }

        fun syncNow() {
            if (syncing.value) return
            syncing.value = true
            syncFailed.value = false
            viewModelScope.launch {
                try {
                    val ok = rates.syncNow()
                    syncFailed.value = !ok
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    syncFailed.value = true
                } finally {
                    syncing.value = false
                }
            }
        }

        fun dismissSyncFailed() {
            syncFailed.value = false
        }

        private fun String.filterInput(): String = filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.')

        private companion object {
            const val HISTORY_DAYS = 30
        }
    }

private fun List<ExchangeRate>.toHistoryRows(): List<RateHistoryRow> =
    groupBy { it.rateDate }
        .map { (date, rows) ->
            RateHistoryRow(
                date = date,
                bcv =
                    rows
                        .firstOrNull { it.source == RateSource.API && it.provider == RateProvider.BCV }
                        ?.rate
                        ?.toCanonicalString(),
                paralelo =
                    rows
                        .firstOrNull { it.source == RateSource.API && it.provider == RateProvider.PARALELO }
                        ?.rate
                        ?.toCanonicalString(),
                manual =
                    rows
                        .firstOrNull { it.source == RateSource.MANUAL }
                        ?.rate
                        ?.toCanonicalString(),
            )
        }.sortedByDescending { it.date }
