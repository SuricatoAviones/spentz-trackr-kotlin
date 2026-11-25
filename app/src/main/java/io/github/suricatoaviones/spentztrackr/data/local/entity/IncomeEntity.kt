package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider

/**
 * Ingreso — espejo de `incomes` (docs/03): sin método, comisión ni ítems.
 * Dinero como texto canónico (ver nota en [ExpenseEntity]).
 */
@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("category_id"), Index("currency"), Index("received_at")],
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val currency: CurrencyCode,
    val amount: String,
    @ColumnInfo(name = "exchange_rate") val exchangeRate: String? = null,
    @ColumnInfo(name = "rate_provider") val rateProvider: RateProvider? = null,
    @ColumnInfo(name = "usd_amount") val usdAmount: String,
    @ColumnInfo(name = "usdt_amount") val usdtAmount: String,
    val description: String,
    val note: String? = null,
    /** ISO "YYYY-MM-DD" (misma convención que [ExpenseEntity.spentAt]). */
    @ColumnInfo(name = "received_at") val receivedAt: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
