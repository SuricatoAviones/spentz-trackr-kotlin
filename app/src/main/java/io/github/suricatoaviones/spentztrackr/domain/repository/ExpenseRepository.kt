package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Filtros del listado de gastos (espejo de los query params del backend). */
data class ExpenseFilter(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val currency: CurrencyCode? = null,
    val categoryId: Long? = null,
    /** Busca en descripción o nota. */
    val search: String? = null,
)

/**
 * Gastos en cualquier modo (ADR-A01): la UI no sabe si hay Room o Retrofit
 * detrás. `save`/`update` reciben el borrador: quien persiste congela la
 * conversión (local: [io.github.suricatoaviones.spentztrackr.domain.service.ConversionService];
 * remoto: el backend con sus Actions).
 */
interface ExpenseRepository {
    fun observe(filter: ExpenseFilter = ExpenseFilter()): Flow<List<Expense>>

    suspend fun find(id: Long): Expense?

    /** @return id del gasto creado. @throws ConversionService.MissingRateException si es Bs sin tasa. */
    suspend fun save(draft: ExpenseDraft): Long

    suspend fun update(
        id: Long,
        draft: ExpenseDraft,
    )

    suspend fun delete(id: Long)
}
