package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource

/**
 * Tasa de un día — espejo de `exchange_rates` (docs/03). El índice único
 * (source, provider, rate_date) hace idempotente el upsert diario.
 * La tasa va como texto canónico a 4 decimales (ADR-A03).
 */
@Entity(
    tableName = "exchange_rates",
    indices = [Index(value = ["source", "provider", "rate_date"], unique = true)],
)
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val source: RateSource,
    val provider: RateProvider,
    /** "28.5000". */
    val rate: String,
    /** ISO "YYYY-MM-DD". */
    @ColumnInfo(name = "rate_date") val rateDate: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
