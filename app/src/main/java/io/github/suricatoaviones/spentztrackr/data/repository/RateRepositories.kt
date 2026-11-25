package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.local.dao.ExchangeRateDao
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
import io.github.suricatoaviones.spentztrackr.data.remote.DolarApiSource
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ManualRateRequestDto
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Tasas vigentes — local/remoto/delegador. En local salen de las filas del
 * día (Room, con el override MANUAL ganando); en remoto de `GET /rates`.
 */
@Singleton
class LocalRateRepository
    @Inject
    constructor(
        private val dao: ExchangeRateDao,
        private val dolarApi: DolarApiSource,
        private val clock: Clock,
    ) : RateRepository {
        override fun observeSnapshot(): Flow<RateSnapshot> =
            dao.observeAll().map { rows -> rows.map(ExchangeRateEntity::toDomain).toSnapshot(today()) }

        override suspend fun snapshot(): RateSnapshot = dao.all().map(ExchangeRateEntity::toDomain).toSnapshot(today())

        override fun observeHistory(days: Int): Flow<List<ExchangeRate>> {
            val from = today().minus(days, DateTimeUnit.DAY)
            return dao.observeSince(from.toString()).map { rows -> rows.map(ExchangeRateEntity::toDomain) }
        }

        override suspend fun saveManualRate(rate: Rate) {
            val now = System.currentTimeMillis()
            dao.upsert(
                ExchangeRateEntity(
                    source = RateSource.MANUAL,
                    provider = RateProvider.USER,
                    rate = rate.toCanonicalString(),
                    rateDate = today().toString(),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        override suspend fun syncNow(): Boolean {
            val fetched = dolarApi.fetchTodayRates()
            if (fetched.isEmpty()) return false
            val now = System.currentTimeMillis()
            fetched.forEach { rate ->
                dao.upsert(
                    ExchangeRateEntity(
                        source = rate.source,
                        provider = rate.provider,
                        rate = rate.rate.toCanonicalString(),
                        rateDate = rate.rateDate.toString(),
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }
            return true
        }

        override suspend fun refreshIfStale(maxAgeMinutes: Int) {
            val last = dao.latestUpdateFor(RateSource.API)
            val age = System.currentTimeMillis() - (last ?: 0L)
            if (last == null || age > maxAgeMinutes * 60_000L) syncNow()
        }

        private fun today(): LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
    }

@Singleton
class RemoteRateRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : RateRepository {
        override fun observeSnapshot(): Flow<RateSnapshot> =
            flow {
                emit(fetchSnapshot())
            }

        override suspend fun snapshot(): RateSnapshot = fetchSnapshot()

        override fun observeHistory(days: Int): Flow<List<ExchangeRate>> = flowOf(emptyList())

        override suspend fun saveManualRate(rate: Rate) {
            api.saveManualRate(ManualRateRequestDto(rate = rate.value))
        }

        override suspend fun syncNow(): Boolean =
            try {
                api.syncRates()
                true
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) throw t
                false
            }

        override suspend fun refreshIfStale(maxAgeMinutes: Int) = Unit

        private suspend fun fetchSnapshot(): RateSnapshot {
            val rates = api.getRates().data?.rates ?: return RateSnapshot()
            val manual = rates["manual"]?.let(Rate::parseOrNull)
            val bcv = rates["bcv"]?.let(Rate::parseOrNull)
            val paralelo = rates["paralelo"]?.let(Rate::parseOrNull)
            return RateSnapshot(
                bcv = bcv,
                paralelo = paralelo,
                manual = manual,
                provider =
                    when {
                        manual != null -> RateProvider.USER
                        bcv != null -> RateProvider.BCV
                        paralelo != null -> RateProvider.PARALELO
                        else -> null
                    },
            )
        }
    }

@Singleton
class DelegatingRateRepository
    @Inject
    constructor(
        @LocalSource private val local: RateRepository,
        @RemoteSource private val remote: RateRepository,
        private val settings: SettingsStore,
    ) : RateRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observeSnapshot(): Flow<RateSnapshot> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode -> active(mode).observeSnapshot() }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observeHistory(days: Int): Flow<List<ExchangeRate>> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode -> active(mode).observeHistory(days) }

        override suspend fun snapshot(): RateSnapshot = active().snapshot()

        override suspend fun saveManualRate(rate: Rate) = active().saveManualRate(rate)

        override suspend fun syncNow(): Boolean = active().syncNow()

        override suspend fun refreshIfStale(maxAgeMinutes: Int) = active().refreshIfStale(maxAgeMinutes)

        private suspend fun active(): RateRepository = active(settings.snapshot().mode)

        private fun active(mode: AppMode?): RateRepository = if (mode == AppMode.REMOTE) remote else local
    }

private fun ExchangeRateEntity.toDomain(): ExchangeRate =
    ExchangeRate(
        id = id,
        source = source,
        provider = provider,
        rate = Rate.parse(rate),
        rateDate = LocalDate.parse(rateDate),
        updatedAt = updatedAt,
    )

/**
 * Filas → snapshot BCV/paralelo/manual. Si no hay filas del día, usa la
 * última conocida por proveedor y marca [RateSnapshot.estimated] (ADR-002:
 * los formularios nunca se bloquean por falta de red).
 */
internal fun List<ExchangeRate>.toSnapshot(date: LocalDate): RateSnapshot {
    val ofDay = filter { it.rateDate == date }
    val source = ofDay.ifEmpty { this }
    if (source.isEmpty()) return RateSnapshot()

    fun latest(predicate: (ExchangeRate) -> Boolean): ExchangeRate? =
        source
            .filter(predicate)
            .maxWithOrNull(compareBy<ExchangeRate> { it.rateDate }.thenBy { it.id })

    val manual = latest { it.source == RateSource.MANUAL }
    val bcv = latest { it.source == RateSource.API && it.provider == RateProvider.BCV }
    val paralelo = latest { it.source == RateSource.API && it.provider == RateProvider.PARALELO }
    val preferred = manual ?: bcv ?: paralelo
    return RateSnapshot(
        bcv = bcv?.rate,
        paralelo = paralelo?.rate,
        manual = manual?.rate,
        estimated = ofDay.isEmpty(),
        provider = preferred?.provider,
        updatedAt = preferred?.updatedAt?.takeIf { it > 0L },
    )
}
