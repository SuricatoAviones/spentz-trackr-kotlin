package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode

/**
 * Porción adicional de un gasto mixto (ADR-003 backend): cada ítem congela
 * su propio equivalente. CASCADE: se va con su gasto. Dinero como texto
 * canónico (ver nota en [ExpenseEntity]).
 */
@Entity(
    tableName = "expense_items",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expense_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("expense_id")],
)
data class ExpenseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "expense_id") val expenseId: Long,
    val currency: CurrencyCode,
    val amount: String,
    @ColumnInfo(name = "exchange_rate") val exchangeRate: String? = null,
    @ColumnInfo(name = "usd_amount") val usdAmount: String,
    @ColumnInfo(name = "usdt_amount") val usdtAmount: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
