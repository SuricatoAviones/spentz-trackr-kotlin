package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.IncomeDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Filtros del listado de ingresos (espejo de los query params del backend). */
data class IncomeFilter(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val currency: CurrencyCode? = null,
    val categoryId: Long? = null,
    val search: String? = null,
)

/** Ingresos en cualquier modo (misma idea que [ExpenseRepository]). */
interface IncomeRepository {
    fun observe(filter: IncomeFilter = IncomeFilter()): Flow<List<Income>>

    suspend fun find(id: Long): Income?

    suspend fun save(draft: IncomeDraft): Long

    suspend fun update(
        id: Long,
        draft: IncomeDraft,
    )

    suspend fun delete(id: Long)
}
