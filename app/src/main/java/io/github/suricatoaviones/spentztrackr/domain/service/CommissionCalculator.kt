package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.Money
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comisión Bs de pago móvil / transferencia (ADR-006 backend):
 *
 * ```
 * commission = max(minCommission, amount × ratePercent / 100)   // HALF_UP, 2 dec
 * ```
 *
 * El formulario la precalcula y deja el valor **editable** (con "sin
 * comisión" disponible): este cálculo solo alimenta el precargado.
 */
@Singleton
class CommissionCalculator
    @Inject
    constructor() {
        fun calculate(
            amount: Money,
            minCommission: Money,
            ratePercent: BigDecimal,
        ): Money {
            val byPercent =
                amount.value
                    .multiply(ratePercent)
                    .divide(PERCENT_DIVISOR, Money.SCALE, RoundingMode.HALF_UP)
            return maxOf(minCommission, Money.of(byPercent))
        }

        private companion object {
            val PERCENT_DIVISOR = BigDecimal(100)
        }
    }
