package io.github.suricatoaviones.spentztrackr.feature.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import javax.inject.Inject
import kotlin.time.Clock

data class IncomeFilterState(
    val month: YearMonth? = null,
    val categoryId: Long? = null,
    val currency: CurrencyCode? = null,
)

data class IncomeDayGroup(
    val date: LocalDate,
    val entries: List<Income>,
    val dayTotalUsd: String,
)

data class IncomesUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val groups: List<IncomeDayGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filter: IncomeFilterState = IncomeFilterState(),
    val currentMonth: YearMonth = YearMonth(2026, 1),
    val monthTotalUsd: String = "0.00",
    val monthTotalUsdt: String = "0.00",
)

/**
 * Listado de ingresos — como el de gastos pero sin origen (los ingresos no
 * llevan `payment_source` en el backend).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class IncomesViewModel
    @Inject
    constructor(
        private val incomes: IncomeRepository,
        private val categories: CategoryRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val currentMonth: YearMonth = clock.todayIn(TimeZone.currentSystemDefault()).yearMonth

        private val filter = MutableStateFlow(IncomeFilterState())

        private val retryTick = MutableStateFlow(0)

        val uiState: StateFlow<IncomesUiState> =
            combine(
                filter.flatMapLatest { f -> incomes.observe(f.toRepositoryFilter()) },
                categories.observe(CategoryType.INCOME).distinctUntilChanged(),
                filter,
                retryTick,
            ) { list, cats, currentFilter, _ ->
                IncomesUiState(
                    loading = false,
                    groups = list.groupByDay(),
                    categories = cats,
                    filter = currentFilter,
                    currentMonth = currentMonth,
                    monthTotalUsd =
                        list
                            .fold(Money.ZERO) { acc, i -> acc + i.usdAmount }
                            .toCanonicalString(),
                    monthTotalUsdt =
                        list
                            .fold(Money.ZERO) { acc, i -> acc + i.usdtAmount }
                            .toCanonicalString(),
                )
            }.catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(IncomesUiState(loading = false, error = true))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = IncomesUiState(loading = true),
            )

        fun setMonth(month: YearMonth?) = filter.update { it.copy(month = month) }

        fun setCategory(categoryId: Long?) = filter.update { it.copy(categoryId = categoryId) }

        fun setCurrency(currency: CurrencyCode?) = filter.update { it.copy(currency = currency) }

        fun retry() {
            retryTick.value = retryTick.value + 1
        }

        private fun IncomeFilterState.toRepositoryFilter(): IncomeFilter =
            IncomeFilter(
                from = month?.firstDay,
                to = month?.lastDay,
                currency = currency,
                categoryId = categoryId,
            )

        private fun List<Income>.groupByDay(): List<IncomeDayGroup> =
            groupBy { it.receivedAt }
                .toSortedMap(compareByDescending<LocalDate> { it })
                .map { (date, entries) ->
                    IncomeDayGroup(
                        date = date,
                        entries = entries,
                        dayTotalUsd = entries.fold(Money.ZERO) { acc, i -> acc + i.usdAmount }.toCanonicalString(),
                    )
                }
    }
