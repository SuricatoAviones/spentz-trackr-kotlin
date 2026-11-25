package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.EntityInUseException
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SourcesUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val items: List<PaymentSource> = emptyList(),
    val deleteBlocked: Boolean = false,
    /**
     * Estado del diálogo: `null` ⇒ cerrado; `-1` ⇒ alta nueva; `≥ 0` ⇒ edición
     * de ese id.
     */
    val dialogEditingId: Long? = null,
)

/** Gestión de orígenes de pago (docs/06): CRUD con borrado bloqueado. */
@HiltViewModel
class SourcesViewModel
    @Inject
    constructor(
        private val sources: PaymentSourceRepository,
    ) : ViewModel() {
        private val deleteBlockedFlag = MutableStateFlow(false)
        private val dialogState = MutableStateFlow<Long?>(null)

        val uiState: StateFlow<SourcesUiState> =
            combine(
                sources.observe(),
                deleteBlockedFlag,
                dialogState,
            ) { items, blocked, dialog ->
                SourcesUiState(
                    loading = false,
                    items = items,
                    deleteBlocked = blocked,
                    dialogEditingId = dialog,
                )
            }.catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(SourcesUiState(loading = false, error = true))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SourcesUiState(),
            )

        fun openNewDialog() {
            dialogState.value = NEW_DIALOG
        }

        fun openEditDialog(id: Long) {
            dialogState.value = id
        }

        fun closeDialog() {
            dialogState.value = null
        }

        fun dismissDeleteBlocked() {
            deleteBlockedFlag.value = false
        }

        fun delete(id: Long) {
            viewModelScope.launch {
                try {
                    sources.delete(id)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    if (t is EntityInUseException) {
                        deleteBlockedFlag.value = true
                    }
                }
            }
        }

        fun save(
            id: Long?,
            name: String,
            icon: String,
            color: String,
        ) {
            viewModelScope.launch {
                try {
                    val draft = PaymentSourceDraft(name = name.trim(), icon = icon, color = color)
                    if (id == null) {
                        sources.save(draft)
                    } else {
                        sources.update(id, draft)
                    }
                    closeDialog()
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    // El diálogo se mantiene; el usuario reintenta.
                }
            }
        }

        private companion object {
            const val NEW_DIALOG = -1L
        }
    }
