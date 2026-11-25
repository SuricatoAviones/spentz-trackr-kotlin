package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseItemDraft
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conversión congelada de montos a USD/USDT — espejo de
 * `ExpenseConversionService` del backend (ADR-A02, ADR-001):
 *
 * - USD y USDT se registran por su valor nominal (USDT 1:1 con USD).
 * - VES: `(monto + comisión) / tasa` redondeado a 2 decimales con HALF_UP
 *   — exactamente como `round($amount / $rate, 2)` de PHP.
 * - El resultado se guarda al persistir y **jamás se recalcula**.
 */
@Singleton
class ConversionService
    @Inject
    constructor() {
        data class FrozenAmounts(
            val usd: Money,
            val usdt: Money,
        )

        /** Un gasto en Bs sin tasa válida no se puede congelar. */
        class MissingRateException : IllegalStateException("Un monto en Bs exige una tasa de cambio mayor que 0")

        /** Convierte una porción (gasto principal o ítem) a los equivalentes congelados. */
        fun convert(
            currency: CurrencyCode,
            amount: Money,
            commission: Money? = null,
            rate: Rate? = null,
        ): FrozenAmounts {
            if (currency == CurrencyCode.VES) {
                val effectiveRate = rate ?: throw MissingRateException()
                val total = amount.value.add(commission?.value ?: BigDecimal.ZERO)
                val usd = Money.of(total.divide(effectiveRate.value, Money.SCALE, RoundingMode.HALF_UP))
                return FrozenAmounts(usd, usd)
            }
            return FrozenAmounts(amount, amount)
        }

        /** Porción de un ítem mixto (sin comisión). */
        fun convertItem(item: ExpenseItemDraft): FrozenAmounts =
            convert(item.currency, item.amount, commission = null, rate = item.exchangeRate)

        /**
         * Total congelado de un gasto completo: porción principal + ítems
         * (ER del backend: `usd_amount = (amount+commission)/rate + items`).
         */
        fun convertExpense(draft: ExpenseDraft): FrozenAmounts {
            var usd = convert(draft.currency, draft.amount, draft.commission, draft.exchangeRate).usd
            var usdt = usd
            for (item in draft.items) {
                val itemFrozen = convertItem(item)
                usd += itemFrozen.usd
                usdt += itemFrozen.usdt
            }
            return FrozenAmounts(usd, usdt)
        }
    }
