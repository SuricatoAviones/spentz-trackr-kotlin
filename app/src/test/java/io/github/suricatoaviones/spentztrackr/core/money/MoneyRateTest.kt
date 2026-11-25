package io.github.suricatoaviones.spentztrackr.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

/**
 * Primitivas de dinero (ADR-A03): BigDecimal, texto canónico, redondeo
 * HALF_UP a 2/4 decimales — igual que `round(x, 2)` de PHP del backend.
 */
class MoneyRateTest {
    @Test
    fun `money normaliza a 2 decimales con half up`() {
        assertEquals("10.50", Money.parse("10.5").toCanonicalString())
        assertEquals("2.35", Money.of(BigDecimal("2.345")).toCanonicalString())
        assertEquals("0.00", Money.ZERO.toCanonicalString())
    }

    @Test
    fun `money parsea texto sucio`() {
        assertEquals("25.00", Money.parse(" 25 ").toCanonicalString())
        assertEquals(null, Money.parseOrNull("no es numero"))
        assertEquals(null, Money.parseOrNull(""))
    }

    @Test
    fun `money normaliza a la construccion y la suma mantiene escala`() {
        assertEquals("14.01", Money.parse("14.005").toCanonicalString())
        assertEquals("294.01", (Money.parse("280") + Money.parse("14.005")).toCanonicalString())
    }

    @Test
    fun `rate normaliza a 4 decimales`() {
        assertEquals("28.5000", Rate.parse("28.5").toCanonicalString())
        assertEquals("0.7500", Rate.parse("0.75").toCanonicalString())
    }

    @Test
    fun `rate rechaza cero y negativos`() {
        assertThrows(IllegalArgumentException::class.java) { Rate.parse("0") }
        assertThrows(IllegalArgumentException::class.java) { Rate.parse("-1") }
        assertEquals(null, Rate.parseOrNull("0"))
    }

    @Test
    fun `currency y enums resuelven por valor del backend`() {
        assertEquals(CurrencyCode.VES, CurrencyCode.fromValue("ves"))
        assertEquals(null, CurrencyCode.fromValue("EUR"))
        assertEquals(PaymentMethod.PAGO_MOVIL, PaymentMethod.fromValue("pago_movil"))
        assertEquals(RateProvider.PARALELO, RateProvider.fromValue("paralelo"))
    }
}
