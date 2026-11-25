package io.github.suricatoaviones.spentztrackr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.suricatoaviones.spentztrackr.data.local.dao.CategoryDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.PaymentSourceDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.UserPrefsDao
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity

/**
 * BD del modo local. La Fase 1 crea las tablas de catálogo y preferencias;
 * la Fase 2 añade expenses/expense_items/incomes/exchange_rates y sube la
 * versión. Hasta v1.0.0 (sin usuarios reales) las migraciones son
 * destructivas — a partir del primer release se escriben migraciones reales.
 */
@Database(
    entities = [
        UserPrefsEntity::class,
        CategoryEntity::class,
        PaymentSourceEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SpentzDatabase : RoomDatabase() {
    abstract fun userPrefsDao(): UserPrefsDao

    abstract fun categoryDao(): CategoryDao

    abstract fun paymentSourceDao(): PaymentSourceDao
}
