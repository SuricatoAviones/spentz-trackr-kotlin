package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettings
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateSnapshot
import io.github.suricatoaviones.spentztrackr.domain.service.CommissionCalculator
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
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

/** De dónde viene la tasa precargada (badge en el formulario). */
enum class RateBadge { BCV, PARALELO, MANUAL, ESTIMADA }

data class ExpenseFormUiState(
    val editingId: Long? = null,
    val loading: Boolean = true,
    // Campos
    val amountText: String = "",
    val currency: CurrencyCode = CurrencyCode.USD,
    val paymentMethod: PaymentMethod? = null,
    /** null = sin comisión. */
    val commissionText: String? = null,
    /** true cuando el usuario tocó la comisión: se deja de autocalcular. */
    val commissionEdited: Boolean = false,
    val rateText: String = "",
    val rateBadge: RateBadge? = null,
    /** Proveedor real de la tasa (se conserva aunque el badge diga Estimada). */
    val rateProvider: RateProvider? = null,
    /** true cuando el usuario tocó la tasa: deja de refrescarse sola. */
    val rateEdited: Boolean = false,
    val categoryId: Long? = null,
    val sourceId: Long? = null,
    val date: LocalDate? = null,
    val description: String = "",
    val note: String = "",
    // Catálogos
    val categories: List<Category> = emptyList(),
    val sources: List<PaymentSource> = emptyList(),
    // Estado
    val saving: Boolean = false,
    val saved: Boolean = false,
    val amountError: Boolean = false,
    val rateError: Boolean = false,
    val descriptionError: Boolean = false,
    val categoryError: Boolean = false,
    val sourceError: Boolean = false,
    val saveError: Boolean = false,
    /** Vista previa del congelado ("10.50"), en vivo (docs/06). */
    val previewUsd: String? = null,
) {
    val isEditing: Boolean get() = editingId != null
}

/**
 * Formulario de gasto (docs/06): moneda, monto, método/comisión/tasa para Bs,
 * equivalente congelado en vivo, categoría y origen. En edición repuebla el
 * gasto tal cual quedó congelado; al guardar el repositorio congela de nuevo
 * (local) o delega al backend (remoto).
 */
@HiltViewModel
class ExpenseFormViewModel
    @Inject
    constructor(
        private val expenses: ExpenseRepository,
        private val categoriesRepo: CategoryRepository,
        private val sourcesRepo: PaymentSourceRepository,
        private val rates: RateRepository,
        private val commissionSettings: CommissionSettingsRepository,
        private val conversion: ConversionService,
        private val commissionCalculator: CommissionCalculator,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ExpenseFormUiState())
        val uiState: StateFlow<ExpenseFormUiState> = _uiState.asStateFlow()

        private var settings: CommissionSettings? = null

        /**
         * La tasa puede refrescarse sola (worker o "si > 60 min") mientras el
         * usuario no haya editado el campo ni esté editando un gasto existente.
         */
        init {
            viewModelScope.launch {
                rates.observeSnapshot().collect { snapshot ->
                    _uiState.update { state ->
                        if (state.loading || state.editingId != null || state.rateEdited) {
                            state
                        } else {
                            recompute(
                                state.copy(
                                    rateText = snapshot.preferred()?.toCanonicalString().orEmpty(),
                                    rateBadge = snapshot.badge(),
                                    rateProvider = snapshot.provider,
                                ),
                            )
                        }
                    }
                }
            }
        }

        /** Carga catálogos + tasa del día; repuebla si es edición. Idempotente. */
        fun load(editingId: Long?) {
            if (!_uiState.value.loading) return
            // Refresco no bloqueante (docs/08 F5): si la tasa tiene > 60 min,
            // se pide en segundo plano; el formulario usa la cache al instante.
            viewModelScope.launch { runCatching { rates.refreshIfStale() } }
            viewModelScope.launch {
                try {
                    val cats = categoriesRepo.observe(CategoryType.EXPENSE).first()
                    val srcs = sourcesRepo.observe().first()
                    val snapshot = runCatching { rates.snapshot() }.getOrNull() ?: RateSnapshot()
                    val commission = runCatching { commissionSettings.settings() }.getOrNull()
                    settings = commission

                    if (editingId != null) {
                        val expense = expenses.find(editingId)
                        if (expense != null) {
                            _uiState.update { it.fromExpense(expense, cats, srcs) }
                            return@launch
                        }
                    }
                    _uiState.update { state ->
                        recompute(
                            state.copy(
                                loading = false,
                                categories = cats,
                                sources = srcs,
                                date = clock.todayIn(TimeZone.currentSystemDefault()),
                                rateText = snapshot.preferred()?.toCanonicalString().orEmpty(),
                                rateBadge = snapshot.badge(),
                                rateProvider = snapshot.provider,
                            ),
                        )
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(loading = false) }
                }
            }
        }

        fun onAmountChange(value: String) = _uiState.update { recompute(autoCommission(it.copy(amountText = value.filterInput()))) }

        fun onCurrencyChange(currency: CurrencyCode) =
            _uiState.update {
                recompute(
                    autoCommission(
                        it.copy(
                            currency = currency,
                            // Al dejar Bs se limpian método y comisión; la tasa
                            // se conserva por si vuelve (el congelado no la usa).
                            paymentMethod = if (currency == CurrencyCode.VES) it.paymentMethod else null,
                            commissionText = if (currency == CurrencyCode.VES) it.commissionText else null,
                            commissionEdited = if (currency == CurrencyCode.VES) it.commissionEdited else false,
                            rateBadge = if (currency == CurrencyCode.VES) it.rateBadge else null,
                        ),
                    ),
                )
            }

        fun onMethodChange(method: PaymentMethod?) = _uiState.update { recompute(autoCommission(it.copy(paymentMethod = method))) }

        fun onCommissionChange(value: String) =
            _uiState.update {
                recompute(
                    it.copy(
                        commissionText = value.filterInput().takeIf { text -> text.isNotBlank() },
                        commissionEdited = true,
                    ),
                )
            }

        fun onClearCommission() = _uiState.update { recompute(it.copy(commissionText = null, commissionEdited = true)) }

        fun onRateChange(value: String) =
            _uiState.update {
                val text = value.filterInput()
                recompute(
                    it.copy(
                        rateText = text,
                        rateBadge = null,
                        rateProvider = if (text.isNotBlank()) RateProvider.CUSTOM else null,
                        rateEdited = true,
                    ),
                )
            }

        fun onCategoryChange(categoryId: Long?) = _uiState.update { it.copy(categoryId = categoryId, categoryError = false) }

        fun onSourceChange(sourceId: Long?) = _uiState.update { it.copy(sourceId = sourceId, sourceError = false) }

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
            val invalidDescription = state.description.isBlank()
            val invalidCategory = state.categoryId == null
            val invalidSource = state.sourceId == null
            if (invalidAmount || invalidRate || invalidDescription || invalidCategory || invalidSource) {
                _uiState.update {
                    it.copy(
                        amountError = invalidAmount,
                        rateError = invalidRate,
                        descriptionError = invalidDescription,
                        categoryError = invalidCategory,
                        sourceError = invalidSource,
                    )
                }
                return
            }

            val draft =
                ExpenseDraft(
                    categoryId = state.categoryId,
                    paymentSourceId = state.sourceId,
                    currency = state.currency,
                    paymentMethod = state.paymentMethod.takeIf { state.currency == CurrencyCode.VES },
                    commission = state.commission.takeIf { state.currency == CurrencyCode.VES },
                    amount = amount,
                    exchangeRate = rate.takeIf { state.currency == CurrencyCode.VES },
                    rateProvider = state.rateProvider,
                    description = state.description.trim(),
                    note = state.note.trim().takeIf { text -> text.isNotBlank() },
                    spentAt = state.date ?: clock.todayIn(TimeZone.currentSystemDefault()),
                    items = emptyList(),
                )

            _uiState.update { it.copy(saving = true, saveError = false) }
            viewModelScope.launch {
                try {
                    val editingId = state.editingId
                    if (editingId == null) {
                        expenses.save(draft)
                    } else {
                        expenses.update(editingId, draft)
                    }
                    _uiState.update { it.copy(saving = false, saved = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(saving = false, saveError = true) }
                }
            }
        }

        // ---------- helpers ----------

        /** Recalcula la comisión solo si el usuario no la ha tocado (ADR-006). */
        private fun autoCommission(state: ExpenseFormUiState): ExpenseFormUiState {
            if (state.currency != CurrencyCode.VES || state.commissionEdited) return state
            val settings = settings ?: return state
            val amount = Money.parseOrNull(state.amountText) ?: return state.copy(commissionText = null)
            val method = state.paymentMethod ?: return state.copy(commissionText = null)
            val commission = commissionCalculator.calculate(amount, settings.minCommission, settings.ratePercent)
            return state.copy(commissionText = commission.toCanonicalString())
        }

        private fun recompute(state: ExpenseFormUiState): ExpenseFormUiState {
            val amount = Money.parseOrNull(state.amountText) ?: return state.copy(previewUsd = null)
            val rate = state.rateText.takeIf { it.isNotBlank() }?.let(Rate::parseOrNull)
            val frozen =
                try {
                    conversion.convert(state.currency, amount, state.commission, rate)
                } catch (expected: ConversionService.MissingRateException) {
                    return state.copy(previewUsd = null)
                }
            return state.copy(previewUsd = frozen.usd.toCanonicalString())
        }

        private fun ExpenseFormUiState.fromExpense(
            expense: Expense,
            cats: List<Category>,
            srcs: List<PaymentSource>,
        ): ExpenseFormUiState =
            recompute(
                copy(
                    loading = false,
                    editingId = expense.id,
                    categories = cats,
                    sources = srcs,
                    amountText = expense.amount.toCanonicalString(),
                    currency = expense.currency,
                    paymentMethod = expense.paymentMethod,
                    commissionText = expense.commission?.toCanonicalString(),
                    commissionEdited = expense.commission != null,
                    rateText = expense.exchangeRate?.toCanonicalString().orEmpty(),
                    rateBadge =
                        when (expense.rateProvider) {
                            RateProvider.BCV -> RateBadge.BCV
                            RateProvider.PARALELO -> RateBadge.PARALELO
                            RateProvider.USER, RateProvider.CUSTOM -> RateBadge.MANUAL
                            null -> null
                        },
                    rateProvider = expense.rateProvider,
                    categoryId = expense.categoryId,
                    sourceId = expense.paymentSourceId,
                    date = expense.spentAt,
                    description = expense.description,
                    note = expense.note.orEmpty(),
                ),
            )

        private fun RateSnapshot.badge(): RateBadge? =
            when {
                estimated -> RateBadge.ESTIMADA
                manual != null -> RateBadge.MANUAL
                bcv != null -> RateBadge.BCV
                paralelo != null -> RateBadge.PARALELO
                else -> null
            }

        private fun String.filterInput(): String = filter { c -> c.isDigit() || c == '.' || c == ',' }.replace(',', '.')
    }

private val ExpenseFormUiState.commission: Money?
    get() = commissionText?.let(Money::parseOrNull)
