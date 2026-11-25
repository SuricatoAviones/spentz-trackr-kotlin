package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

/**
 * Regla de comisión Bs (ADR-006 backend): `max(mínimo, monto × %)`.
 * Defaults del usuario: 14.00 Bs y 0.30 %.
 */
class CommissionCalculatorTest {
    private val calculator = CommissionCalculator()

    @Test
    fun `porcentaje pequeno activa el piso minimo`() {
        // 100 × 0.30 % = 0.30 < 14.00 ⇒ gana el mínimo.
        val commission =
            calculator.calculate(
                amount = Money.parse("100"),
                minCommission = Money.parse("14.00"),
                ratePercent = BigDecimal("0.30"),
            )

        assertEquals("14.00", commission.toCanonicalString())
    }

    @Test
    fun `monto grande activa el porcentaje`() {
        // 10000 × 0.30 % = 30.00 > 14.00 ⇒ gana el porcentaje.
        val commission =
            calculator.calculate(
                amount = Money.parse("10000"),
                minCommission = Money.parse("14.00"),
                ratePercent = BigDecimal("0.30"),
            )

        assertEquals("30.00", commission.toCanonicalString())
    }

    @Test
    fun `por defecto el piso gana con monto cero`() {
        val commission =
            calculator.calculate(
                amount = Money.ZERO,
                minCommission = Money.parse("14.00"),
                ratePercent = BigDecimal("0.30"),
            )

        assertEquals("14.00", commission.toCanonicalString())
    }

    @Test
    fun `redondea el porcentaje a dos decimales half up`() {
        // 14.99 × 0.30 % = 0.04497 ⇒ 0.04 (por debajo del piso, pero valida el cálculo).
        val commission =
            calculator.calculate(
                amount = Money.parse("14.99"),
                minCommission = Money.ZERO,
                ratePercent = BigDecimal("0.30"),
            )

        assertEquals("0.04", commission.toCanonicalString())
    }
}
