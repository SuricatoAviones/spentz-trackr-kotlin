package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun findById(id: Long): IncomeEntity?

    @Query(
        """
        SELECT * FROM incomes
        WHERE (:from IS NULL OR received_at >= :from)
          AND (:to IS NULL OR received_at <= :to)
          AND (:currency IS NULL OR currency = :currency)
          AND (:categoryId IS NULL OR category_id = :categoryId)
          AND (:search IS NULL OR description LIKE '%' || :search || '%')
        ORDER BY received_at DESC, id DESC
        """,
    )
    fun observeFiltered(
        from: String?,
        to: String?,
        currency: CurrencyCode?,
        categoryId: Long?,
        search: String?,
    ): Flow<List<IncomeEntity>>
}
