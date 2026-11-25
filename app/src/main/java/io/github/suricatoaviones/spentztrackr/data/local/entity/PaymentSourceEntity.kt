package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Origen de pago (etiqueta sin estado; las tarjetas de crédito llegan en
 * fases posteriores con su tabla propia) — espejo de `payment_sources`.
 */
@Entity(tableName = "payment_sources")
data class PaymentSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: String,
    @ColumnInfo(name = "is_system") val isSystem: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
