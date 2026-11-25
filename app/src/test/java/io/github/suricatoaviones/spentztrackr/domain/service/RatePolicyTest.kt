package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Política de tasa del día (espejo de `ExchangeRateService::rateForUser`):
 * manual > api del día (BCV preferido) > última conocida (estimada) > nada.
 */
class RatePolicyTest {
    private val policy = RatePolicy()
    private val today = LocalDate.parse("2026-11-25")
    private val yesterday = LocalDate.parse("2026-11-24")

    private fun rate(
        id: Long,
        source: RateSource,
        provider: RateProvider,
        value: String,
        date: LocalDate = today,
    ) = ExchangeRate(id = id, source = source, provider = provider, rate = Rate.parse(value), rateDate = date)

    @Test
    fun `la manual del dia gana sobre la api`() {
        val rates =
            listOf(
                rate(1, RateSource.API, RateProvider.BCV, "30.0000"),
                rate(2, RateSource.MANUAL, RateProvider.USER, "29.7500"),
            )

        val selection = policy.rateFor(rates, today)

        assertEquals("29.7500", selection?.rate?.toCanonicalString())
        assertEquals(RateProvider.USER, selection?.provider)
        assertEquals(false, selection?.estimated)
    }

    @Test
    fun `sin manual usa la api con proveedor preferido`() {
        val rates =
            listOf(
                rate(1, RateSource.API, RateProvider.PARALELO, "32.1000"),
                rate(2, RateSource.API, RateProvider.BCV, "30.5000"),
            )

        val selection = policy.rateFor(rates, today)

        assertEquals("30.5000", selection?.rate?.toCanonicalString())
        assertEquals(RateProvider.BCV, selection?.provider)
    }

    @Test
    fun `sin bcv cae al paralelo del dia`() {
        val rates = listOf(rate(1, RateSource.API, RateProvider.PARALELO, "32.1000"))

        val selection = policy.rateFor(rates, today)

        assertEquals("32.1000", selection?.rate?.toCanonicalString())
        assertEquals(RateProvider.PARALELO, selection?.provider)
    }

    @Test
    fun `sin nada del dia usa la ultima conocida marcada estimada`() {
        val rates =
            listOf(
                rate(1, RateSource.API, RateProvider.BCV, "28.0000", date = yesterday),
            )

        val selection = policy.rateFor(rates, today)

        assertEquals("28.0000", selection?.rate?.toCanonicalString())
        assertEquals(true, selection?.estimated)
    }

    @Test
    fun `sin tasas devuelve null`() {
        assertNull(policy.rateFor(emptyList(), today))
    }
}
