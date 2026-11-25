package io.github.suricatoaviones.spentztrackr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.suricatoaviones.spentztrackr.data.local.dao.CategoryDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.ExchangeRateDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.ExpenseDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.IncomeDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.PaymentSourceDao
import io.github.suricatoaviones.spentztrackr.data.local.dao.UserPrefsDao
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseItemEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.IncomeEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity

/**
 * BD del modo local — esquema espejo del backend (docs/03, ADR-A05).
 * v2 (Fase 2): expenses, expense_items, incomes y exchange_rates.
 * Hasta v1.0.0 (sin usuarios reales) las migraciones son destructivas; a
 * partir del primer release se escriben migraciones reales.
 */
@Database(
    entities = [
        UserPrefsEntity::class,
        CategoryEntity::class,
        PaymentSourceEntity::class,
        ExpenseEntity::class,
        ExpenseItemEntity::class,
        IncomeEntity::class,
        ExchangeRateEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(SpentzConverters::class)
abstract class SpentzDatabase : RoomDatabase() {
    abstract fun userPrefsDao(): UserPrefsDao

    abstract fun categoryDao(): CategoryDao

    abstract fun paymentSourceDao(): PaymentSourceDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun incomeDao(): IncomeDao

    abstract fun exchangeRateDao(): ExchangeRateDao
}
