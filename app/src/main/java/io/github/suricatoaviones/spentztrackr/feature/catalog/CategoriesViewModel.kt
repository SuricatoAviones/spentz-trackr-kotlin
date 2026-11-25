package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.EntityInUseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val type: CategoryType = CategoryType.EXPENSE,
    val items: List<Category> = emptyList(),
    val deleteBlocked: Boolean = false,
)

/** Gestión de categorías (docs/06): lista por tipo, borrar bloqueado con movimientos. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel
    @Inject
    constructor(
        private val categories: CategoryRepository,
    ) : ViewModel() {
        private val selectedType = MutableStateFlow(CategoryType.EXPENSE)
        private val deleteBlockedFlag = MutableStateFlow(false)

        val uiState: StateFlow<CategoriesUiState> =
            combine(
                selectedType.flatMapLatest { t -> categories.observe(t) },
                selectedType,
                deleteBlockedFlag,
            ) { items, currentType, blocked ->
                CategoriesUiState(
                    loading = false,
                    type = currentType,
                    items = items,
                    deleteBlocked = blocked,
                )
            }.catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(CategoriesUiState(loading = false, error = true))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CategoriesUiState(),
            )

        fun setType(newType: CategoryType) {
            selectedType.value = newType
        }

        fun dismissDeleteBlocked() {
            deleteBlockedFlag.value = false
        }

        fun delete(id: Long) {
            viewModelScope.launch {
                try {
                    categories.delete(id)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    if (t is EntityInUseException) {
                        deleteBlockedFlag.value = true
                    }
                }
            }
        }
    }
