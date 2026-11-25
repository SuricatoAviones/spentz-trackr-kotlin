package io.github.suricatoaviones.spentztrackr.data.repository

import androidx.room.withTransaction
import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseItemEntity
import io.github.suricatoaviones.spentztrackr.data.local.toDomain
import io.github.suricatoaviones.spentztrackr.data.local.toEntity
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.toDomain
import io.github.suricatoaviones.spentztrackr.data.remote.toRequestDto
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gastos — tres implementaciones del mismo contrato (ADR-A01): local (Room,
 * la app congela con [ConversionService]), remoto (API, congela el backend)
 * y el delegador que escoge según el modo activo.
 */
@Singleton
class LocalExpenseRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
        private val conversion: ConversionService,
    ) : ExpenseRepository {
        override fun observe(filter: ExpenseFilter): Flow<List<Expense>> =
            database
                .expenseDao()
                .observeFiltered(
                    from = filter.from?.toString(),
                    to = filter.to?.toString(),
                    currency = filter.currency,
                    categoryId = filter.categoryId,
                    search = filter.search?.takeIf { it.isNotBlank() },
                ).map { rows -> rows.map { it.toDomain() } }

        override suspend fun find(id: Long): Expense? = database.expenseDao().findWithItems(id)?.toDomain()

        override suspend fun save(draft: ExpenseDraft): Long {
            val now = System.currentTimeMillis()
            val frozen = conversion.convertExpense(draft)
            return database.withTransaction {
                val id = database.expenseDao().insert(draft.toEntity(frozen, now))
                insertItems(draft, id, now)
                id
            }
        }

        override suspend fun update(
            id: Long,
            draft: ExpenseDraft,
        ) {
            val now = System.currentTimeMillis()
            // Como el backend: editar recalcula el congelado con la tasa editada.
            val frozen = conversion.convertExpense(draft)
            database.withTransaction {
                database.expenseDao().update(draft.toEntity(frozen, now, id))
                database.expenseDao().deleteItemsByExpense(id)
                insertItems(draft, id, now)
            }
        }

        override suspend fun delete(id: Long) {
            database.expenseDao().deleteById(id)
        }

        private suspend fun insertItems(
            draft: ExpenseDraft,
            expenseId: Long,
            now: Long,
        ) {
            if (draft.items.isEmpty()) return
            database.expenseDao().insertItems(
                draft.items.map { item ->
                    val frozenItem = conversion.convertItem(item)
                    ExpenseItemEntity(
                        expenseId = expenseId,
                        currency = item.currency,
                        amount = item.amount.toCanonicalString(),
                        exchangeRate = item.exchangeRate?.toCanonicalString(),
                        usdAmount = frozenItem.usd.toCanonicalString(),
                        usdtAmount = frozenItem.usdt.toCanonicalString(),
                        createdAt = now,
                        updatedAt = now,
                    )
                },
            )
        }
    }

@Singleton
class RemoteExpenseRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : ExpenseRepository {
        /**
         * Un fetch por recolección (sin caché en v1): la UI de la Fase 3
         * reencola o refresca. El congelado lo hace el backend.
         */
        override fun observe(filter: ExpenseFilter): Flow<List<Expense>> =
            flow {
                emit(fetch(filter))
            }

        override suspend fun find(id: Long): Expense? = api.getExpense(id).data?.toDomain()

        override suspend fun save(draft: ExpenseDraft): Long =
            api.createExpense(draft.toRequestDto()).data?.id
                ?: error("Respuesta de creación sin id")

        override suspend fun update(
            id: Long,
            draft: ExpenseDraft,
        ) {
            api.updateExpense(id, draft.toRequestDto())
        }

        override suspend fun delete(id: Long) {
            api.deleteExpense(id)
        }

        private suspend fun fetch(filter: ExpenseFilter): List<Expense> =
            api
                .listExpenses(
                    search = filter.search?.takeIf { it.isNotBlank() },
                    currency = filter.currency?.value,
                    categoryId = filter.categoryId,
                    from = filter.from?.toString(),
                    to = filter.to?.toString(),
                ).data
                ?.expenses
                .orEmpty()
                .map { it.toDomain() }
    }

@Singleton
class DelegatingExpenseRepository
    @Inject
    constructor(
        @LocalSource private val local: ExpenseRepository,
        @RemoteSource private val remote: ExpenseRepository,
        private val settings: SettingsStore,
    ) : ExpenseRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(filter: ExpenseFilter): Flow<List<Expense>> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe(filter) else local.observe(filter)
                }

        override suspend fun find(id: Long): Expense? = active().find(id)

        override suspend fun save(draft: ExpenseDraft): Long = active().save(draft)

        override suspend fun update(
            id: Long,
            draft: ExpenseDraft,
        ) = active().update(id, draft)

        override suspend fun delete(id: Long) = active().delete(id)

        private suspend fun active(): ExpenseRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }
