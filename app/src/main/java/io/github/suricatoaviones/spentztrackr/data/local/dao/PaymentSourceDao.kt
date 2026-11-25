package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity

@Dao
interface PaymentSourceDao {
    @Insert
    suspend fun insertAll(sources: List<PaymentSourceEntity>)

    @Query("SELECT COUNT(*) FROM payment_sources")
    suspend fun count(): Int
}
