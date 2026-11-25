package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import kotlinx.datetime.LocalDate

/**
 * Gasto persistido con su conversión **congelada** (ADR-001 backend):
 * `usdAmount`/`usdtAmount` se calcularon al escribir y jamás se recalculan.
 * Espejo de `expenses` del backend (docs/03).
 */
data class Expense(
    val id: Long,
    val categoryId: Long,
    val paymentSourceId: Long,
    val currency: CurrencyCode,
    /** Solo VES: pago móvil / transferencia (habilita comisión). */
    val paymentMethod: PaymentMethod?,
    /** Solo VES; `amount` guarda la base sin comisión. */
    val commission: Money?,
    val amount: Money,
    /** Obligatoria (> 0) cuando currency = VES. */
    val exchangeRate: Rate?,
    val rateProvider: RateProvider?,
    /** Congelado: porción principal + ítems (ER del backend). */
    val usdAmount: Money,
    val usdtAmount: Money,
    val description: String,
    val note: String?,
    val spentAt: LocalDate,
    val items: List<ExpenseItem> = emptyList(),
)

/**
 * Gasto antes de persistir. Sin equivalentes USD/USDT: quien persiste decide
 * — en local los calcula [io.github.suricatoaviones.spentztrackr.domain.service.ConversionService],
 * en remoto el backend con sus propias Actions (ADR-A02).
 */
data class ExpenseDraft(
    val categoryId: Long,
    val paymentSourceId: Long,
    val currency: CurrencyCode,
    val paymentMethod: PaymentMethod? = null,
    val commission: Money? = null,
    val amount: Money,
    val exchangeRate: Rate? = null,
    val rateProvider: RateProvider? = null,
    val description: String,
    val note: String? = null,
    val spentAt: LocalDate,
    val items: List<ExpenseItemDraft> = emptyList(),
)

/** Porción adicional de un gasto mixto en otra moneda (ADR-003 backend). */
data class ExpenseItem(
    val id: Long = 0L,
    val expenseId: Long = 0L,
    val currency: CurrencyCode,
    val amount: Money,
    /** Obligatoria si el ítem es VES. */
    val exchangeRate: Rate?,
    val usdAmount: Money,
    val usdtAmount: Money,
)

data class ExpenseItemDraft(
    val currency: CurrencyCode,
    val amount: Money,
    val exchangeRate: Rate? = null,
)
