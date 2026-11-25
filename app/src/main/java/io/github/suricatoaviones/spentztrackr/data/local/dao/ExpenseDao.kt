package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseItemEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert
    suspend fun insertItems(items: List<ExpenseItemEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expense_items WHERE expense_id = :expenseId")
    suspend fun deleteItemsByExpense(expenseId: Long)

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun findWithItems(id: Long): ExpenseWithItems?

    /**
     * Listado con filtros (docs/03: índices por fecha/categoría/moneda).
     * `from`/`to` son ISO "YYYY-MM-DD": ordenan cronológicamente en SQL.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM expenses
        WHERE (:from IS NULL OR spent_at >= :from)
          AND (:to IS NULL OR spent_at <= :to)
          AND (:currency IS NULL OR currency = :currency)
          AND (:categoryId IS NULL OR category_id = :categoryId)
          AND (:search IS NULL OR description LIKE '%' || :search || '%')
        ORDER BY spent_at DESC, id DESC
        """,
    )
    fun observeFiltered(
        from: String?,
        to: String?,
        currency: CurrencyCode?,
        categoryId: Long?,
        search: String?,
    ): Flow<List<ExpenseWithItems>>
}
