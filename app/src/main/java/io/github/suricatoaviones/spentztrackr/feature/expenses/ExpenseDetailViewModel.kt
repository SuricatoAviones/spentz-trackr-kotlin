package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val expense: Expense? = null,
    val categoryName: String? = null,
    val sourceName: String? = null,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val deleteError: Boolean = false,
)

/** Detalle de un gasto: lectura + eliminación (docs/06). */
@HiltViewModel
class ExpenseDetailViewModel
    @Inject
    constructor(
        private val expenses: ExpenseRepository,
        private val categoriesRepo: CategoryRepository,
        private val sourcesRepo: PaymentSourceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ExpenseDetailUiState())
        val uiState: StateFlow<ExpenseDetailUiState> = _uiState.asStateFlow()

        fun load(id: Long) {
            if (!_uiState.value.loading) return
            viewModelScope.launch {
                try {
                    val expense = expenses.find(id)
                    if (expense == null) {
                        _uiState.update { it.copy(loading = false, notFound = true) }
                        return@launch
                    }
                    val category = categoriesRepo.observe().first().firstOrNull { it.id == expense.categoryId }
                    val source = sourcesRepo.observe().first().firstOrNull { it.id == expense.paymentSourceId }
                    _uiState.update {
                        it.copy(
                            loading = false,
                            expense = expense,
                            categoryName = category?.name,
                            sourceName = source?.name,
                        )
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(loading = false, notFound = true) }
                }
            }
        }

        fun delete() {
            val id = _uiState.value.expense?.id ?: return
            if (_uiState.value.deleting) return
            _uiState.update { it.copy(deleting = true, deleteError = false) }
            viewModelScope.launch {
                try {
                    expenses.delete(id)
                    _uiState.update { it.copy(deleting = false, deleted = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(deleting = false, deleteError = true) }
                }
            }
        }
    }
