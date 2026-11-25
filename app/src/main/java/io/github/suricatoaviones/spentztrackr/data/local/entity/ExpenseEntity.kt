package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider

/**
 * Gasto — espejo de `expenses` del backend (docs/03). FKs RESTRICT: no se
 * borra una categoría u origen con movimientos, igual que el backend.
 *
 * El dinero va como **texto decimal canónico** (ADR-A03): Room + KSP2 no
 * procesa value classes en campos, así que `Money`/`Rate` se materializan
 * en los mappers de `data.local.EntityMappers`. `amount` guarda la base;
 * la comisión va aparte; los equivalentes están **congelados** (ADR-001).
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = PaymentSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["payment_source_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("category_id"),
        Index("payment_source_id"),
        Index("currency"),
        Index("spent_at"),
    ],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "payment_source_id") val paymentSourceId: Long,
    val currency: CurrencyCode,
    @ColumnInfo(name = "payment_method") val paymentMethod: PaymentMethod? = null,
    /** "14.00"; solo VES. */
    val commission: String? = null,
    /** Base sin comisión: "280.00". */
    val amount: String,
    /** "28.0000"; obligatoria cuando currency = VES. */
    @ColumnInfo(name = "exchange_rate") val exchangeRate: String? = null,
    @ColumnInfo(name = "rate_provider") val rateProvider: RateProvider? = null,
    /** Congelado al escribir (porción principal + ítems). */
    @ColumnInfo(name = "usd_amount") val usdAmount: String,
    @ColumnInfo(name = "usdt_amount") val usdtAmount: String,
    val description: String,
    val note: String? = null,
    /** ISO "YYYY-MM-DD": ordena cronológicamente en SQL y es el formato del backend. */
    @ColumnInfo(name = "spent_at") val spentAt: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
