package io.github.suricatoaviones.spentztrackr.feature.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncomeDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val income: Income? = null,
    val categoryName: String? = null,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val deleteError: Boolean = false,
)

@HiltViewModel
class IncomeDetailViewModel
    @Inject
    constructor(
        private val incomes: IncomeRepository,
        private val categoriesRepo: CategoryRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(IncomeDetailUiState())
        val uiState: StateFlow<IncomeDetailUiState> = _uiState.asStateFlow()

        fun load(id: Long) {
            if (!_uiState.value.loading) return
            viewModelScope.launch {
                try {
                    val income = incomes.find(id)
                    if (income == null) {
                        _uiState.update { it.copy(loading = false, notFound = true) }
                        return@launch
                    }
                    val category = categoriesRepo.observe().first().firstOrNull { it.id == income.categoryId }
                    _uiState.update {
                        it.copy(loading = false, income = income, categoryName = category?.name)
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(loading = false, notFound = true) }
                }
            }
        }

        fun delete() {
            val id = _uiState.value.income?.id ?: return
            if (_uiState.value.deleting) return
            _uiState.update { it.copy(deleting = true, deleteError = false) }
            viewModelScope.launch {
                try {
                    incomes.delete(id)
                    _uiState.update { it.copy(deleting = false, deleted = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(deleting = false, deleteError = true) }
                }
            }
        }
    }
