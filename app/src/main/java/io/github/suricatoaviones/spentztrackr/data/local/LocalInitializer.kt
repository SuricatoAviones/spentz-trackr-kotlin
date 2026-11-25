package io.github.suricatoaviones.spentztrackr.data.local

import androidx.room.withTransaction
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity
import io.github.suricatoaviones.spentztrackr.domain.model.LocalSetup
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inicializa el modo local: set de categorías/orígenes por defecto (como el
 * backend al crear usuario) y preferencias iniciales. **Idempotente**: cada
 * set solo se crea si no existe — re-ejecutar jamás duplica.
 */
@Singleton
class LocalInitializer
    @Inject
    constructor(
        private val database: SpentzDatabase,
    ) {
        suspend fun initialize(setup: LocalSetup) {
            val now = System.currentTimeMillis()

            database.withTransaction {
                if (database.categoryDao().count() == 0) {
                    database.categoryDao().insertAll(
                        DefaultData.categories.map { item ->
                            CategoryEntity(
                                name = item.name,
                                icon = item.icon,
                                color = item.color,
                                type = CATEGORY_TYPE_EXPENSE,
                                isSystem = true,
                                createdAt = now,
                                updatedAt = now,
                            )
                        },
                    )
                }

                if (database.paymentSourceDao().count() == 0) {
                    database.paymentSourceDao().insertAll(
                        DefaultData.paymentSources.map { item ->
                            PaymentSourceEntity(
                                name = item.name,
                                icon = item.icon,
                                color = item.color,
                                isSystem = true,
                                createdAt = now,
                                updatedAt = now,
                            )
                        },
                    )
                }

                database.userPrefsDao().upsert(
                    UserPrefsEntity(
                        defaultDisplayCurrency = setup.displayCurrency.value,
                        trackingType = setup.trackingType.value,
                        monthlyBudget = setup.monthlyBudget,
                    ),
                )
            }
        }

        private companion object {
            const val CATEGORY_TYPE_EXPENSE = "expense"
        }
    }
