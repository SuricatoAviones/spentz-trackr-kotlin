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
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.EntityInUseException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Categorías — local/remoto/delegador. Borrar una categoría con movimientos
 * está bloqueado en ambos lados (FK restrict / 422-409 del backend).
 */
@Singleton
class LocalCategoryRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
    ) : CategoryRepository {
        override fun observe(type: CategoryType?): Flow<List<Category>> =
            when (type) {
                null -> database.categoryDao().observeAll()
                else -> database.categoryDao().observeByType(type)
            }.map { rows -> rows.map { it.toDomain() } }

        override suspend fun save(draft: CategoryDraft): Long {
            val now = System.currentTimeMillis()
            return database.categoryDao().insert(draft.toEntity(now))
        }

        override suspend fun update(
            id: Long,
            draft: CategoryDraft,
        ) {
            val existing = database.categoryDao().findById(id) ?: return
            val now = System.currentTimeMillis()
            database.categoryDao().update(draft.toEntity(now, isSystem = existing.isSystem, id = id))
        }

        override suspend fun delete(id: Long) {
            try {
                database.categoryDao().deleteById(id)
            } catch (e: SQLiteConstraintException) {
                throw EntityInUseException()
            }
        }
    }

@Singleton
class RemoteCategoryRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : CategoryRepository {
        override fun observe(type: CategoryType?): Flow<List<Category>> =
            flow {
                emit(
                    api
                        .listCategories()
                        .data
                        ?.categories
                        .orEmpty()
                        .map { it.toDomain() }
                        .let { list -> if (type == null) list else list.filter { it.type == type } },
                )
            }

        override suspend fun save(draft: CategoryDraft): Long =
            api.createCategory(draft.toRequestDto()).data?.id
                ?: error("Respuesta de creación sin id")

        override suspend fun update(
            id: Long,
            draft: CategoryDraft,
        ) {
            api.updateCategory(id, draft.toRequestDto())
        }

        override suspend fun delete(id: Long) {
            try {
                api.deleteCategory(id)
            } catch (e: HttpException) {
                if (e.code() == 422 || e.code() == 409) {
                    throw EntityInUseException()
                }
                throw e
            }
        }
    }

@Singleton
class DelegatingCategoryRepository
    @Inject
    constructor(
        @LocalSource private val local: CategoryRepository,
        @RemoteSource private val remote: CategoryRepository,
        private val settings: SettingsStore,
    ) : CategoryRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(type: CategoryType?): Flow<List<Category>> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe(type) else local.observe(type)
                }

        override suspend fun save(draft: CategoryDraft): Long = active().save(draft)

        override suspend fun update(
            id: Long,
            draft: CategoryDraft,
        ) = active().update(id, draft)

        override suspend fun delete(id: Long) = active().delete(id)

        private suspend fun active(): CategoryRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }
