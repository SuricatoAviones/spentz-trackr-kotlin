package io.github.suricatoaviones.spentztrackr.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExchangeRateEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseItemEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integración de Room v2 (criterio de salida de la Fase 2): insertar un gasto
 * con ítems y leerlo con sus equivalentes congelados; FK RESTRICT bloquea
 * borrar una categoría con gastos; el upsert de tasas es idempotente por el
 * índice único (source, provider, rate_date).
 *
 * Requiere emulador o dispositivo (androidTest): correr con
 * `gradlew connectedDebugAndroidTest` o desde Android Studio.
 */
@RunWith(AndroidJUnit4::class)
class SpentzDatabaseTest {
    private lateinit var database: SpentzDatabase

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SpentzDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun seedCategory(): Long =
        database.categoryDao().insert(
            CategoryEntity(
                name = "Alimentación",
                icon = "shopping-cart",
                color = "#10B981",
                type = CategoryType.EXPENSE,
                isSystem = true,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )

    private suspend fun seedSource(): Long =
        database.paymentSourceDao().insert(
            PaymentSourceEntity(
                name = "Efectivo",
                icon = "banknote",
                color = "#F59E0B",
                isSystem = true,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )

    @Test
    fun insertarGastoConItemsYLeerlo() =
        runBlocking {
            val categoryId = seedCategory()
            val sourceId = seedSource()

            val expenseId =
                database.expenseDao().insert(
                    ExpenseEntity(
                        categoryId = categoryId,
                        paymentSourceId = sourceId,
                        currency = CurrencyCode.VES,
                        paymentMethod = PaymentMethod.PAGO_MOVIL,
                        commission = "14.00",
                        amount = "280.00",
                        exchangeRate = "28.0000",
                        usdAmount = "10.50",
                        usdtAmount = "10.50",
                        description = "Mercado",
                        spentAt = "2026-11-25",
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                )
            database.expenseDao().insertItems(
                listOf(
                    ExpenseItemEntity(
                        expenseId = expenseId,
                        currency = CurrencyCode.USD,
                        amount = "5.00",
                        exchangeRate = null,
                        usdAmount = "5.00",
                        usdtAmount = "5.00",
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                ),
            )

            val loaded = database.expenseDao().findWithItems(expenseId)!!

            assertEquals("10.50", loaded.expense.usdAmount)
            assertEquals("28.0000", loaded.expense.exchangeRate)
            assertEquals(1, loaded.items.size)
            assertEquals("5.00", loaded.items.first().usdAmount)
        }

    @Test
    fun categoriaConGastosNoSeBorra() =
        runBlocking {
            val categoryId = seedCategory()
            val sourceId = seedSource()
            database.expenseDao().insert(
                ExpenseEntity(
                    categoryId = categoryId,
                    paymentSourceId = sourceId,
                    currency = CurrencyCode.USD,
                    amount = "10.00",
                    usdAmount = "10.00",
                    usdtAmount = "10.00",
                    description = "Café",
                    spentAt = "2026-11-25",
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            )

            assertThrows(SQLiteConstraintException::class.java) {
                runBlocking { database.categoryDao().deleteById(categoryId) }
            }
        }

    @Test
    fun borrarElGastoSeLlevaSusItemsPorCascade() =
        runBlocking {
            val categoryId = seedCategory()
            val sourceId = seedSource()
            val expenseId =
                database.expenseDao().insert(
                    ExpenseEntity(
                        categoryId = categoryId,
                        paymentSourceId = sourceId,
                        currency = CurrencyCode.USD,
                        amount = "10.00",
                        usdAmount = "10.00",
                        usdtAmount = "10.00",
                        description = "Café",
                        spentAt = "2026-11-25",
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                )
            database.expenseDao().insertItems(
                listOf(
                    ExpenseItemEntity(
                        expenseId = expenseId,
                        currency = CurrencyCode.USDT,
                        amount = "2.00",
                        usdAmount = "2.00",
                        usdtAmount = "2.00",
                        createdAt = 1L,
                        updatedAt = 1L,
                    ),
                ),
            )

            database.expenseDao().deleteById(expenseId)

            assertNull(database.expenseDao().findWithItems(expenseId))
        }

    @Test
    fun upsertDeTasaEsIdempotentePorDia() =
        runBlocking {
            val day = "2026-11-25"

            fun manualRate(value: String) =
                ExchangeRateEntity(
                    source = RateSource.MANUAL,
                    provider = RateProvider.USER,
                    rate = value,
                    rateDate = day,
                    createdAt = 1L,
                    updatedAt = 1L,
                )

            database.exchangeRateDao().upsert(manualRate("30.0000"))
            database.exchangeRateDao().upsert(manualRate("29.7500"))

            val ofDay = database.exchangeRateDao().forDate(day)

            assertEquals(1, ofDay.size)
            assertEquals("29.7500", ofDay.first().rate)
        }
}
