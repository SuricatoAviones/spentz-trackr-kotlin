package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Mapeo de `ve.dolarapi.com/v1/dolares` a la tasa del día (docs/08 F5). */
class DolarApiMappingTest {
    private val today = LocalDate.parse("2026-11-25")

    @Test
    fun `oficial es bcv y paralelo es paralelo`() {
        val bcv = DolarApiRow(fuente = "oficial", promedio = 922.5637).toExchangeRate(today)
        val paralelo = DolarApiRow(fuente = "paralelo", promedio = 1107.261789).toExchangeRate(today)

        assertEquals(RateProvider.BCV, bcv?.provider)
        assertEquals("922.5637", bcv?.rate?.toCanonicalString())
        assertEquals(RateSource.API, bcv?.source)
        assertEquals(RateProvider.PARALELO, paralelo?.provider)
        assertEquals("1107.2618", paralelo?.rate?.toCanonicalString())
    }

    @Test
    fun `sin promedio usa el promedio de compra y venta`() {
        val row = DolarApiRow(fuente = "oficial", compra = 30.0, venta = 32.0)

        assertEquals("31.0000", row.toExchangeRate(today)?.rate?.toCanonicalString())
    }

    @Test
    fun `usa la fecha de actualizacion de la api`() {
        val row = DolarApiRow(fuente = "oficial", promedio = 30.0, fechaActualizacion = "2026-11-20T09:00:00-04:00")

        assertEquals(LocalDate.parse("2026-11-20"), row.toExchangeRate(today)?.rateDate)
    }

    @Test
    fun `ignora fuentes desconocidas y tasas no positivas`() {
        assertNull(DolarApiRow(fuente = "otro", promedio = 30.0).toExchangeRate(today))
        assertNull(DolarApiRow(fuente = "oficial", promedio = 0.0).toExchangeRate(today))
        assertNull(DolarApiRow(fuente = "oficial").toExchangeRate(today))
    }
}
