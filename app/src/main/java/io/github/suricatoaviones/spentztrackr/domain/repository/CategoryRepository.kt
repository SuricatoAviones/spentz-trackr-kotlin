package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

/**
 * Categorías en cualquier modo. Borrar una categoría con movimientos está
 * bloqueado (FK restrict / 422 del backend): [EntityInUseException].
 */
interface CategoryRepository {
    fun observe(type: CategoryType? = null): Flow<List<Category>>

    suspend fun save(draft: CategoryDraft): Long

    suspend fun update(
        id: Long,
        draft: CategoryDraft,
    )

    @Throws(EntityInUseException::class)
    suspend fun delete(id: Long)
}
