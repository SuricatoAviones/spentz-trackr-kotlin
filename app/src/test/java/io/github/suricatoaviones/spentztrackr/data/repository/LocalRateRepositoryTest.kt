package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.local.dao.ExchangeRateDao
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
import io.github.suricatoaviones.spentztrackr.data.remote.DolarApiSource
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Repositorio local de tasas (docs/08 F5): snapshot del día, fallback
 * "estimada" sin red (ADR-002) y sincronización/refresco perezoso.
 */
class LocalRateRepositoryTest {
    private val today = LocalDate.parse("2026-11-25")
    private val yesterday = LocalDate.parse("2026-11-24")
    private val clock: Clock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2026-11-25T12:00:00Z")
        }

    @Test
    fun `la manual de hoy gana y no se marca estimada`() =
        runTest {
            val dao =
                FakeExchangeRateDao(
                    entity(RateSource.API, RateProvider.BCV, "30.0000", today),
                    entity(RateSource.MANUAL, RateProvider.USER, "29.7500", today),
                )
            val repository = LocalRateRepository(dao, FakeDolarApi(), clock)

            val snapshot = repository.snapshot()

            assertEquals("29.7500", snapshot.manual?.toCanonicalString())
            assertEquals("30.0000", snapshot.bcv?.toCanonicalString())
            assertEquals(RateProvider.USER, snapshot.provider)
            assertFalse(snapshot.estimated)
        }

    @Test
    fun `sin tasa de hoy usa la ultima conocida marcada estimada`() =
        runTest {
            val dao =
                FakeExchangeRateDao(
                    entity(RateSource.API, RateProvider.BCV, "28.0000", yesterday, updatedAt = 1_000L),
                )
            val repository = LocalRateRepository(dao, FakeDolarApi(), clock)

            val snapshot = repository.snapshot()

            assertEquals("28.0000", snapshot.bcv?.toCanonicalString())
            assertEquals(RateProvider.BCV, snapshot.provider)
            assertTrue(snapshot.estimated)
            assertEquals(1_000L, snapshot.updatedAt)
        }

    @Test
    fun `sin ninguna tasa devuelve snapshot vacio`() =
        runTest {
            val repository = LocalRateRepository(FakeExchangeRateDao(), FakeDolarApi(), clock)

            val snapshot = repository.snapshot()

            assertEquals(null, snapshot.preferred())
            assertFalse(snapshot.estimated)
        }

    @Test
    fun `syncNow guarda bcv y paralelo del dia`() =
        runTest {
            val dao = FakeExchangeRateDao()
            val api =
                FakeDolarApi(
                    listOf(
                        rate(RateProvider.BCV, "922.5637", today),
                        rate(RateProvider.PARALELO, "1107.2618", today),
                    ),
                )
            val repository = LocalRateRepository(dao, api, clock)

            assertTrue(repository.syncNow())

            val snapshot = repository.snapshot()
            assertEquals("922.5637", snapshot.bcv?.toCanonicalString())
            assertEquals("1107.2618", snapshot.paralelo?.toCanonicalString())
            assertFalse(snapshot.estimated)
        }

    @Test
    fun `syncNow sin respuesta no escribe nada y devuelve false`() =
        runTest {
            val dao = FakeExchangeRateDao()
            val repository = LocalRateRepository(dao, FakeDolarApi(emptyList()), clock)

            assertFalse(repository.syncNow())
            assertTrue(dao.rows.value.isEmpty())
        }

    @Test
    fun `refreshIfStale pide sync solo si la ultima api es vieja`() =
        runTest {
            val staleDao =
                FakeExchangeRateDao(
                    entity(
                        RateSource.API,
                        RateProvider.BCV,
                        "30.0000",
                        today,
                        updatedAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000L,
                    ),
                )
            val staleApi = FakeDolarApi(listOf(rate(RateProvider.BCV, "31.0000", today)))
            LocalRateRepository(staleDao, staleApi, clock).refreshIfStale(maxAgeMinutes = 60)
            assertEquals(1, staleApi.calls)

            val freshDao =
                FakeExchangeRateDao(
                    entity(
                        RateSource.API,
                        RateProvider.BCV,
                        "30.0000",
                        today,
                        updatedAt = System.currentTimeMillis() - 10 * 60 * 1000L,
                    ),
                )
            val freshApi = FakeDolarApi(listOf(rate(RateProvider.BCV, "31.0000", today)))
            LocalRateRepository(freshDao, freshApi, clock).refreshIfStale(maxAgeMinutes = 60)
            assertEquals(0, freshApi.calls)
        }

    @Test
    fun `observeHistory devuelve los ultimos 30 dias`() =
        runTest {
            val dao =
                FakeExchangeRateDao(
                    entity(RateSource.API, RateProvider.BCV, "30.0000", today),
                    entity(RateSource.API, RateProvider.BCV, "28.0000", yesterday),
                )
            val repository = LocalRateRepository(dao, FakeDolarApi(), clock)

            val history = repository.observeHistory(days = 30).first()

            assertEquals(listOf(today, yesterday), history.map { it.rateDate })
        }

    private fun rate(
        provider: RateProvider,
        value: String,
        date: LocalDate,
    ) = ExchangeRate(
        source = RateSource.API,
        provider = provider,
        rate = Rate.parse(value),
        rateDate = date,
    )

    private fun entity(
        source: RateSource,
        provider: RateProvider,
        value: String,
        date: LocalDate,
        updatedAt: Long = 0L,
    ) = ExchangeRateEntity(
        source = source,
        provider = provider,
        rate = value,
        rateDate = date.toString(),
        createdAt = updatedAt,
        updatedAt = updatedAt,
    )
}

private class FakeDolarApi(
    private val rates: List<ExchangeRate> = emptyList(),
) : DolarApiSource {
    var calls: Int = 0
        private set

    override suspend fun fetchTodayRates(): List<ExchangeRate> {
        calls += 1
        return rates
    }
}

private class FakeExchangeRateDao(
    vararg initial: ExchangeRateEntity,
) : ExchangeRateDao {
    val rows = MutableStateFlow(initial.toList())
    private var nextId = initial.size.toLong() + 1

    override suspend fun upsert(rate: ExchangeRateEntity) {
        val withoutSameKey =
            rows.value.filterNot {
                it.source == rate.source && it.provider == rate.provider && it.rateDate == rate.rateDate
            }
        rows.value = withoutSameKey + rate.copy(id = nextId++)
    }

    override suspend fun forDate(date: String): List<ExchangeRateEntity> = rows.value.filter { it.rateDate == date }

    override suspend fun all(): List<ExchangeRateEntity> = rows.value

    override fun observeAll(): Flow<List<ExchangeRateEntity>> = rows

    override fun observeSince(fromDate: String): Flow<List<ExchangeRateEntity>> =
        rows.map { list -> list.filter { it.rateDate >= fromDate } }

    override suspend fun latestUpdateFor(source: RateSource): Long? = rows.value.filter { it.source == source }.maxOfOrNull { it.updatedAt }
}
