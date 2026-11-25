package io.github.suricatoaviones.spentztrackr.data.repository

import android.database.sqlite.SQLiteConstraintException
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
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.EntityInUseException
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** Orígenes de pago — local/remoto/delegador, como las categorías. */
@Singleton
class LocalPaymentSourceRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
    ) : PaymentSourceRepository {
        override fun observe(): Flow<List<PaymentSource>> =
            database.paymentSourceDao().observeAll().map { rows -> rows.map { it.toDomain() } }

        override suspend fun save(draft: PaymentSourceDraft): Long {
            val now = System.currentTimeMillis()
            return database.paymentSourceDao().insert(draft.toEntity(now))
        }

        override suspend fun update(
            id: Long,
            draft: PaymentSourceDraft,
        ) {
            val existing = database.paymentSourceDao().findById(id) ?: return
            val now = System.currentTimeMillis()
            database.paymentSourceDao().update(draft.toEntity(now, isSystem = existing.isSystem, id = id))
        }

        override suspend fun delete(id: Long) {
            try {
                database.paymentSourceDao().deleteById(id)
            } catch (e: SQLiteConstraintException) {
                throw EntityInUseException()
            }
        }
    }

@Singleton
class RemotePaymentSourceRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : PaymentSourceRepository {
        override fun observe(): Flow<List<PaymentSource>> =
            flow {
                emit(
                    api
                        .listSources()
                        .data
                        ?.sources
                        .orEmpty()
                        .map { it.toDomain() },
                )
            }

        override suspend fun save(draft: PaymentSourceDraft): Long =
            api.createSource(draft.toRequestDto()).data?.id
                ?: error("Respuesta de creación sin id")

        override suspend fun update(
            id: Long,
            draft: PaymentSourceDraft,
        ) {
            api.updateSource(id, draft.toRequestDto())
        }

        override suspend fun delete(id: Long) {
            try {
                api.deleteSource(id)
            } catch (e: HttpException) {
                if (e.code() == 422 || e.code() == 409) {
                    throw EntityInUseException()
                }
                throw e
            }
        }
    }

@Singleton
class DelegatingPaymentSourceRepository
    @Inject
    constructor(
        @LocalSource private val local: PaymentSourceRepository,
        @RemoteSource private val remote: PaymentSourceRepository,
        private val settings: SettingsStore,
    ) : PaymentSourceRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(): Flow<List<PaymentSource>> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe() else local.observe()
                }

        override suspend fun save(draft: PaymentSourceDraft): Long = active().save(draft)

        override suspend fun update(
            id: Long,
            draft: PaymentSourceDraft,
        ) = active().update(id, draft)

        override suspend fun delete(id: Long) = active().delete(id)

        private suspend fun active(): PaymentSourceRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }
