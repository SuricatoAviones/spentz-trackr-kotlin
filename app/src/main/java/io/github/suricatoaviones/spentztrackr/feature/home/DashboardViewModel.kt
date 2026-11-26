package io.github.suricatoaviones.spentztrackr.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import io.github.suricatoaviones.spentztrackr.domain.model.UserPreferences
import io.github.suricatoaviones.spentztrackr.domain.repository.DashboardRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import javax.inject.Inject
import kotlin.time.Clock

data class HomeUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val month: YearMonth = YearMonth(2026, 1),
    val displayCurrency: DisplayCurrency = DisplayCurrency.USD,
    val trackingType: TrackingType = TrackingType.BOTH,
    val globalBudget: Money? = null,
    val dashboard: Dashboard? = null,
)

/**
 * Inicio (docs/06): combina el dashboard (local o remoto según el modo) con
 * las preferencias de visualización. Un solo [Dashboard] para ambos modos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val dashboard: DashboardRepository,
        private val preferences: UserPreferencesRepository,
        clock: Clock,
    ) : ViewModel() {
        private val currentMonth = clock.todayIn(TimeZone.currentSystemDefault()).yearMonth

        /** Relanza el fetch remoto tras un error. */
        private val retryTick = MutableStateFlow(0)

        val uiState: StateFlow<HomeUiState> =
            combine(
                retryTick.flatMapLatest {
                    dashboard
                        .observe(currentMonth)
                        .map { DashboardResult(dashboard = it, failed = false) }
                        .catch { emit(DashboardResult(dashboard = null, failed = true)) }
                },
                preferences.observe().catch { emit(UserPreferences()) },
            ) { result, prefs ->
                HomeUiState(
                    loading = false,
                    error = result.failed,
                    month = currentMonth,
                    displayCurrency = prefs.displayCurrency,
                    trackingType = prefs.trackingType,
                    globalBudget = prefs.monthlyBudget,
                    dashboard = result.dashboard,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(loading = true, month = currentMonth),
            )

        fun retry() {
            retryTick.value = retryTick.value + 1
        }
    }

private data class DashboardResult(
    val dashboard: Dashboard?,
    val failed: Boolean,
)
