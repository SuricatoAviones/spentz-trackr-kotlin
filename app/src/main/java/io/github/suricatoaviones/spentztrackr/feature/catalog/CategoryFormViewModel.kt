package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryFormUiState(
    val loading: Boolean = true,
    val editingId: Long? = null,
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val icon: String = "tag",
    val color: String = "#10B981",
    /** Presupuesto mensual; solo se conserva en categorías de gasto (backend). */
    val budgetText: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val nameError: Boolean = false,
    val budgetError: Boolean = false,
    val saveError: Boolean = false,
)

/** Creación/edición de categoría (docs/06). */
@HiltViewModel
class CategoryFormViewModel
    @Inject
    constructor(
        private val categories: CategoryRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CategoryFormUiState())
        val uiState: StateFlow<CategoryFormUiState> = _uiState.asStateFlow()

        fun load(
            editingId: Long?,
            defaultType: CategoryType,
        ) {
            if (!_uiState.value.loading) return
            viewModelScope.launch {
                try {
                    if (editingId != null) {
                        val existing = categories.observe(null).first().firstOrNull { it.id == editingId }
                        if (existing != null) {
                            _uiState.update {
                                it.fromCategory(existing)
                            }
                            return@launch
                        }
                    }
                    _uiState.update { it.copy(loading = false, type = defaultType) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(loading = false) }
                }
            }
        }

        fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = false) }

        fun onTypeChange(type: CategoryType) =
            _uiState.update {
                // El presupuesto solo vive en categorías de gasto (backend).
                it.copy(type = type, budgetText = if (type == CategoryType.EXPENSE) it.budgetText else "")
            }

        fun onIconChange(icon: String) = _uiState.update { it.copy(icon = icon) }

        fun onColorChange(color: String) = _uiState.update { it.copy(color = color) }

        fun onBudgetChange(value: String) =
            _uiState.update { it.copy(budgetText = value.filter { c -> c.isDigit() || c == '.' || c == ',' }, budgetError = false) }

        fun save() {
            val state = _uiState.value
            if (state.saving || state.saved) return

            if (state.name.isBlank()) {
                _uiState.update { it.copy(nameError = true) }
                return
            }
            val budget =
                state.budgetText
                    .trim()
                    .replace(',', '.')
                    .takeIf { it.isNotBlank() }
            if (budget != null && (budget.toBigDecimalOrNull() == null || budget.toBigDecimalOrNull()!!.signum() <= 0)) {
                _uiState.update { it.copy(budgetError = true) }
                return
            }

            val draft =
                CategoryDraft(
                    name = state.name.trim(),
                    icon = state.icon,
                    color = state.color,
                    type = state.type,
                    budget = budget?.let(Money::parse),
                )

            _uiState.update { it.copy(saving = true, saveError = false) }
            viewModelScope.launch {
                try {
                    val editingId = state.editingId
                    if (editingId == null) {
                        categories.save(draft)
                    } else {
                        categories.update(editingId, draft)
                    }
                    _uiState.update { it.copy(saving = false, saved = true) }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    _uiState.update { it.copy(saving = false, saveError = true) }
                }
            }
        }

        private fun CategoryFormUiState.fromCategory(category: Category): CategoryFormUiState =
            copy(
                loading = false,
                editingId = category.id,
                name = category.name,
                type = category.type,
                icon = category.icon,
                color = category.color,
                budgetText = category.budget?.toCanonicalString().orEmpty(),
            )
    }
