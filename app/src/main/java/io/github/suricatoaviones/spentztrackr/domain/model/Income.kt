package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import kotlinx.datetime.LocalDate

/**
 * Ingreso persistido con su conversión congelada — espejo de `incomes`
 * del backend: sin método, comisión ni ítems (docs/03).
 */
data class Income(
    val id: Long,
    val categoryId: Long,
    val currency: CurrencyCode,
    val amount: Money,
    val exchangeRate: Rate?,
    val rateProvider: RateProvider?,
    val usdAmount: Money,
    val usdtAmount: Money,
    val description: String,
    val note: String?,
    val receivedAt: LocalDate,
)

/** Ingreso antes de persistir (ver [ExpenseDraft]). */
data class IncomeDraft(
    val categoryId: Long,
    val currency: CurrencyCode,
    val amount: Money,
    val exchangeRate: Rate? = null,
    val rateProvider: RateProvider? = null,
    val description: String,
    val note: String? = null,
    val receivedAt: LocalDate,
)
