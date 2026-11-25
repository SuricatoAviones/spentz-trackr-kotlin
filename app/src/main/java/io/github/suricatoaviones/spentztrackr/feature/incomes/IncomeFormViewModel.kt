package io.github.suricatoaviones.spentztrackr.feature.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.IncomeDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateSnapshot
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
import io.github.suricatoaviones.spentztrackr.feature.expenses.RateBadge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class IncomeFormUiState(
    val editingId: Long? = null,
    val loading: Boolean = true,
    val amountText: String = "",
    val currency: CurrencyCode = CurrencyCode.USD,
    val rateText: String = "",
    val rateBadge: RateBadge? = null,
    val categoryId: Long? = null,
    val date: LocalDate? = null,
    val description: String = "",
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val saving: Boolean = false,
    val saved: Boolean = false,
    val amountError: Boolean = false,
    val rateError: Boolean = false,
    val descriptionError: Boolean = false,
    val categoryError: Boolean = false,
    val saveError: Boolean = false,
    val previewUsd: String? = null,
) {
    val isEditing: Boolean get() = editingId != null
}

/**
 * Formulario de ingreso (docs/06): como el de gasto sin método, comisión ni
 * origen — el backend no los tiene para ingresos (docs/03).
 */
@HiltViewModel
class IncomeFormViewModel
    @Inject
    constructor(
        private val incomes: IncomeRepository,
        private val categoriesRepo: CategoryRepository,
        private val rates: RateRepository,
        private val conversion: ConversionService,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(IncomeFormUiState())
        val uiState: StateFlow<IncomeFormUiState> = _uiState.asStateFlow()

        fun load(editingId: Long?) {
            if (!_uiState.value.loading) return
            viewModelScope.launch {
                try {
                    val cats = categoriesRepo.observe(CategoryType.INCOME).first()
                    val snapshot = runCatching { rates.snapshot() }.getOrNull() ?: RateSnapshot()

                    if (editingId != null) {
                        val income = incomes.find(editingId)
                        if (income != null) {
                            _uiState.update { it.fromIncome(income, cats) }
                            return@launch
                        }
                    }
                    _uiState.update { state ->
                        recompute(
                            state.copy(
                                loading = false,
                                categories = cats,
                                date = clock.todayIn(TimeZone.currentSystemDefault()),
                                rateText = snapshot.preferred()?.toCanonicalString().orEmpty(),
                                rateBadge = snapshot.badge(),
                            ),
                        )
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(loading = false) }
                }
            }
        }

        fun onAmountChange(value: String) = _uiState.update { recompute(it.copy(amountText = value.filterInput())) }

        fun onCurrencyChange(currency: CurrencyCode) =
            _uiState.update {
                recompute(
                    it.copy(
                        currency = currency,
                        rateBadge = if (currency == CurrencyCode.VES) it.rateBadge else null,
                    ),
                )
            }

        fun onRateChange(value: String) = _uiState.update { recompute(it.copy(rateText = value.filterInput(), rateBadge = null)) }

        fun onCategoryChange(categoryId: Long?) = _uiState.update { it.copy(categoryId = categoryId, categoryError = false) }

        fun onDateChange(date: LocalDate) = _uiState.update { it.copy(date = date) }

        fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value, descriptionError = false) }

        fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

        fun save() {
            val state = _uiState.value
            if (state.saving || state.saved) return

            val amount = Money.parseOrNull(state.amountText)
            val rate = state.rateText.takeIf { it.isNotBlank() }?.let(Rate::parseOrNull)

            val invalidAmount = amount == null || amount.value.signum() <= 0
            val invalidRate = state.currency == CurrencyCode.VES && rate == null
            if (invalidAmount || invalidRate || state.description.isBlank() || state.categoryId == null) {
                _uiState.update {
                    it.copy(
                        amountError = invalidAmount,
                        rateError = invalidRate,
                        descriptionError = state.description.isBlank(),
                        categoryError = state.categoryId == null,
                    )
                }
                return
            }

            val draft =
                IncomeDraft(
                    categoryId = state.categoryId,
                    currency = state.currency,
                    amount = amount,
                    exchangeRate = rate.takeIf { state.currency == CurrencyCode.VES },
                    rateProvider =
                        when (state.rateBadge) {
                            RateBadge.MANUAL -> RateProvider.USER
                            RateBadge.BCV -> RateProvider.BCV
                            RateBadge.PARALELO -> RateProvider.PARALELO
                            null -> rate?.let { RateProvider.CUSTOM }
                        },
                    description = state.description.trim(),
                    note = state.note.trim().takeIf { text -> text.isNotBlank() },
                    receivedAt = state.date ?: clock.todayIn(TimeZone.currentSystemDefault()),
                )

            _uiState.update { it.copy(saving = true, saveError = false) }
            viewModelScope.launch {
                try {
                    val editingId = state.editingId
                    if (editingId == null) {
                        incomes.save(draft)
                    } else {
                        incomes.update(editingId, draft)
                    }
                    _uiState.update { it.copy(saving = false, saved = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(saving = false, saveError = true) }
                }
            }
        }

        private fun recompute(state: IncomeFormUiState): IncomeFormUiState {
            val amount = Money.parseOrNull(state.amountText) ?: return state.copy(previewUsd = null)
            val rate = state.rateText.takeIf { it.isNotBlank() }?.let(Rate::parseOrNull)
            val frozen =
                try {
                    conversion.convert(state.currency, amount, commission = null, rate = rate)
                } catch (expected: ConversionService.MissingRateException) {
                    return state.copy(previewUsd = null)
                }
            return state.copy(previewUsd = frozen.usd.toCanonicalString())
        }

        private fun IncomeFormUiState.fromIncome(
            income: Income,
            cats: List<Category>,
        ): IncomeFormUiState =
            recompute(
                copy(
                    loading = false,
                    editingId = income.id,
                    categories = cats,
                    amountText = income.amount.toCanonicalString(),
                    currency = income.currency,
                    rateText = income.exchangeRate?.toCanonicalString().orEmpty(),
                    rateBadge =
                        when (income.rateProvider) {
                            RateProvider.BCV -> RateBadge.BCV
                            RateProvider.PARALELO -> RateBadge.PARALELO
                            RateProvider.USER, RateProvider.CUSTOM -> RateBadge.MANUAL
                            null -> null
                        },
                    categoryId = income.categoryId,
                    date = income.receivedAt,
                    description = income.description,
                    note = income.note.orEmpty(),
                ),
            )

        private fun RateSnapshot.badge(): RateBadge? =
            when {
                manual != null -> RateBadge.MANUAL
                bcv != null -> RateBadge.BCV
                paralelo != null -> RateBadge.PARALELO
                else -> null
            }

        private fun String.filterInput(): String = filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.')
    }
