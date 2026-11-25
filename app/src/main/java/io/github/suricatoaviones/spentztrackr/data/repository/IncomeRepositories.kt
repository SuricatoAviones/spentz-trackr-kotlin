package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.local.toDomain
import io.github.suricatoaviones.spentztrackr.data.local.toEntity
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.toDomain
import io.github.suricatoaviones.spentztrackr.data.remote.toRequestDto
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.IncomeDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Ingresos — local/remoto/delegador, como los gastos (ADR-A01). */
@Singleton
class LocalIncomeRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
        private val conversion: ConversionService,
    ) : IncomeRepository {
        override fun observe(filter: IncomeFilter): Flow<List<Income>> =
            database
                .incomeDao()
                .observeFiltered(
                    from = filter.from?.toString(),
                    to = filter.to?.toString(),
                    currency = filter.currency,
                    categoryId = filter.categoryId,
                    search = filter.search?.takeIf { it.isNotBlank() },
                ).map { rows -> rows.map { it.toDomain() } }

        override suspend fun find(id: Long): Income? = database.incomeDao().findById(id)?.toDomain()

        override suspend fun save(draft: IncomeDraft): Long {
            val now = System.currentTimeMillis()
            val frozen = conversion.convert(draft.currency, draft.amount, commission = null, rate = draft.exchangeRate)
            return database.incomeDao().insert(draft.toEntity(frozen, now))
        }

        override suspend fun update(
            id: Long,
            draft: IncomeDraft,
        ) {
            val now = System.currentTimeMillis()
            val frozen = conversion.convert(draft.currency, draft.amount, commission = null, rate = draft.exchangeRate)
            database.incomeDao().update(draft.toEntity(frozen, now, id))
        }

        override suspend fun delete(id: Long) {
            database.incomeDao().deleteById(id)
        }
    }

@Singleton
class RemoteIncomeRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : IncomeRepository {
        override fun observe(filter: IncomeFilter): Flow<List<Income>> =
            flow {
                emit(fetch(filter))
            }

        override suspend fun find(id: Long): Income? = api.getIncome(id).data?.toDomain()

        override suspend fun save(draft: IncomeDraft): Long =
            api.createIncome(draft.toRequestDto()).data?.id
                ?: error("Respuesta de creación sin id")

        override suspend fun update(
            id: Long,
            draft: IncomeDraft,
        ) {
            api.updateIncome(id, draft.toRequestDto())
        }

        override suspend fun delete(id: Long) {
            api.deleteIncome(id)
        }

        private suspend fun fetch(filter: IncomeFilter): List<Income> =
            api
                .listIncomes(
                    search = filter.search?.takeIf { it.isNotBlank() },
                    currency = filter.currency?.value,
                    categoryId = filter.categoryId,
                    from = filter.from?.toString(),
                    to = filter.to?.toString(),
                ).data
                ?.incomes
                .orEmpty()
                .map { it.toDomain() }
    }

@Singleton
class DelegatingIncomeRepository
    @Inject
    constructor(
        @LocalSource private val local: IncomeRepository,
        @RemoteSource private val remote: IncomeRepository,
        private val settings: SettingsStore,
    ) : IncomeRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(filter: IncomeFilter): Flow<List<Income>> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe(filter) else local.observe(filter)
                }

        override suspend fun find(id: Long): Income? = active().find(id)

        override suspend fun save(draft: IncomeDraft): Long = active().save(draft)

        override suspend fun update(
            id: Long,
            draft: IncomeDraft,
        ) = active().update(id, draft)

        override suspend fun delete(id: Long) = active().delete(id)

        private suspend fun active(): IncomeRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }
