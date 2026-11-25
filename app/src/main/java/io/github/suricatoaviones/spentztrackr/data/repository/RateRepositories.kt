package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
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
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
        private val database: SpentzDatabase,
        private val clock: Clock,
    ) : RateRepository {
        override fun observeSnapshot(): Flow<RateSnapshot> =
            database.exchangeRateDao().observeAll().map { rows ->
                rows
                    .map { entity ->
                        ExchangeRate(
                            id = entity.id,
                            source = entity.source,
                            provider = entity.provider,
                            rate = Rate.parse(entity.rate),
                            rateDate = LocalDate.parse(entity.rateDate),
                        )
                    }.toSnapshot(today())
            }

        override suspend fun snapshot(): RateSnapshot = observeSnapshotToday(today())

        override suspend fun saveManualRate(rate: Rate) {
            val now = System.currentTimeMillis()
            database.exchangeRateDao().upsert(
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

        private suspend fun observeSnapshotToday(date: LocalDate): RateSnapshot {
            val ofDay =
                database.exchangeRateDao().forDate(date.toString()).map { entity ->
                    ExchangeRate(
                        id = entity.id,
                        source = entity.source,
                        provider = entity.provider,
                        rate = Rate.parse(entity.rate),
                        rateDate = LocalDate.parse(entity.rateDate),
                    )
                }
            return ofDay.toSnapshot(date)
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

        override suspend fun saveManualRate(rate: Rate) {
            api.saveManualRate(ManualRateRequestDto(rate = rate.value))
        }

        private suspend fun fetchSnapshot(): RateSnapshot {
            val rates = api.getRates().data?.rates ?: return RateSnapshot()
            return RateSnapshot(
                bcv = rates["bcv"]?.let(Rate::parseOrNull),
                paralelo = rates["paralelo"]?.let(Rate::parseOrNull),
                manual = rates["manual"]?.let(Rate::parseOrNull),
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
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observeSnapshot() else local.observeSnapshot()
                }

        override suspend fun snapshot(): RateSnapshot = active().snapshot()

        override suspend fun saveManualRate(rate: Rate) = active().saveManualRate(rate)

        private suspend fun active(): RateRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }

/** Filas del día → snapshot BCV/paralelo/manual (la manual gana si existe). */
private fun List<ExchangeRate>.toSnapshot(date: LocalDate): RateSnapshot {
    val ofDay = filter { it.rateDate == date }
    return RateSnapshot(
        bcv = ofDay.firstOrNull { it.source == RateSource.API && it.provider == RateProvider.BCV }?.rate,
        paralelo = ofDay.firstOrNull { it.source == RateSource.API && it.provider == RateProvider.PARALELO }?.rate,
        manual = ofDay.firstOrNull { it.source == RateSource.MANUAL }?.rate,
    )
}
