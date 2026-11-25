package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentSourceDao {
    @Insert
    suspend fun insertAll(sources: List<PaymentSourceEntity>)

    @Insert
    suspend fun insert(source: PaymentSourceEntity): Long

    @Update
    suspend fun update(source: PaymentSourceEntity)

    @Query("SELECT COUNT(*) FROM payment_sources")
    suspend fun count(): Int

    @Query("SELECT * FROM payment_sources ORDER BY name")
    fun observeAll(): Flow<List<PaymentSourceEntity>>

    @Query("SELECT * FROM payment_sources WHERE id = :id")
    suspend fun findById(id: Long): PaymentSourceEntity?

    /** FK RESTRICT: lanza SQLiteConstraintException si tiene movimientos. */
    @Query("DELETE FROM payment_sources WHERE id = :id")
    suspend fun deleteById(id: Long)
}
