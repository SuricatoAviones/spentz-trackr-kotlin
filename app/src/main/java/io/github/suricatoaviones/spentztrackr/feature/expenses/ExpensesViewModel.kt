package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import javax.inject.Inject

/** Filtros del listado; month null ⇒ todos los meses. */
data class ExpenseFilterState(
    val month: YearMonth? = null,
    val categoryId: Long? = null,
    val currency: CurrencyCode? = null,
)

/** Grupo de un día: header con fecha localizada + subtotal USD congelado. */
data class ExpenseDayGroup(
    val date: LocalDate,
    val entries: List<Expense>,
    val dayTotalUsd: String,
)

data class ExpensesUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val groups: List<ExpenseDayGroup> = emptyList(),
    val categories: List<Category> = emptyList(),
    val sources: List<PaymentSource> = emptyList(),
    val filter: ExpenseFilterState = ExpenseFilterState(),
    /** Mes en curso, para el selector de meses del filtro. */
    val currentMonth: YearMonth = YearMonth(2026, 1),
    val monthTotalUsd: String = "0.00",
    val monthTotalUsdt: String = "0.00",
)

/**
 * Listado de gastos con filtros reactivos (docs/06): la lista se emite desde
 * el repositorio delegador (local o remoto según el modo) y se reagrupa por
 * día; los filtros viajan al repositorio, que los resuelve en la fuente.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpensesViewModel
    @Inject
    constructor(
        private val expenses: ExpenseRepository,
        private val categories: CategoryRepository,
        private val sourcesRepo: PaymentSourceRepository,
        private val clock: kotlin.time.Clock,
    ) : ViewModel() {
        private val currentMonth: YearMonth =
            clock.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).yearMonth

        private val filter = MutableStateFlow(ExpenseFilterState())

        /** Relanzar el fetch remoto tras un error. */
        private val retryTick = MutableStateFlow(0)

        val uiState: StateFlow<ExpensesUiState> =
            combine(
                filter.flatMapLatest { f -> expenses.observe(f.toRepositoryFilter()) },
                categories.observe(CategoryType.EXPENSE).distinctUntilChanged(),
                sourcesRepo.observe().distinctUntilChanged(),
                filter,
                retryTick,
            ) { list, cats, srcs, currentFilter, _ ->
                ExpensesUiState(
                    loading = false,
                    groups = list.groupByDay(),
                    categories = cats,
                    sources = srcs,
                    filter = currentFilter,
                    currentMonth = currentMonth,
                    monthTotalUsd =
                        list
                            .fold(io.github.suricatoaviones.spentztrackr.core.money.Money.ZERO) { acc, e -> acc + e.usdAmount }
                            .toCanonicalString(),
                    monthTotalUsdt =
                        list
                            .fold(io.github.suricatoaviones.spentztrackr.core.money.Money.ZERO) { acc, e -> acc + e.usdtAmount }
                            .toCanonicalString(),
                )
            }.catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(ExpensesUiState(loading = false, error = true))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExpensesUiState(loading = true),
            )

        fun setMonth(month: YearMonth?) = filter.update { it.copy(month = month) }

        fun setCategory(categoryId: Long?) = filter.update { it.copy(categoryId = categoryId) }

        fun setCurrency(currency: CurrencyCode?) = filter.update { it.copy(currency = currency) }

        fun retry() {
            retryTick.value = retryTick.value + 1
        }

        private fun ExpenseFilterState.toRepositoryFilter(): ExpenseFilter =
            ExpenseFilter(
                from = month?.firstDay,
                to = month?.lastDay,
                currency = currency,
                categoryId = categoryId,
            )

        private fun List<Expense>.groupByDay(): List<ExpenseDayGroup> =
            groupBy { it.spentAt }
                .toSortedMap(compareByDescending<LocalDate> { it })
                .map { (date, entries) ->
                    ExpenseDayGroup(
                        date = date,
                        entries = entries,
                        dayTotalUsd =
                            entries
                                .fold(io.github.suricatoaviones.spentztrackr.core.money.Money.ZERO) { acc, e -> acc + e.usdAmount }
                                .toCanonicalString(),
                    )
                }
    }
