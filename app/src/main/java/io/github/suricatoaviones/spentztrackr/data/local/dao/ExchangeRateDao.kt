package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    /** REPLACE + índice único (source, provider, rate_date) = upsert diario idempotente. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rate: ExchangeRateEntity)

    @Query("SELECT * FROM exchange_rates WHERE rate_date = :date ORDER BY id DESC")
    suspend fun forDate(date: String): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates ORDER BY rate_date DESC, id DESC")
    fun observeAll(): Flow<List<ExchangeRateEntity>>
}
