package io.github.suricatoaviones.spentztrackr.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * El contrato del repositorio es idéntico en ambos modos (ADR-A01, criterio
 * de salida de la Fase 2): el delegador manda cada operación a la fuente
 * activa según el modo guardado en DataStore.
 */
class DelegatingExpenseRepositoryTest {
    private lateinit var file: File
    private lateinit var settings: SettingsStore
    private lateinit var local: FakeExpenseRepository
    private lateinit var remote: FakeExpenseRepository
    private lateinit var repository: DelegatingExpenseRepository

    @Before
    fun setUp() {
        file = File.createTempFile("delegation", ".preferences_pb")
        settings = SettingsStore(PreferenceDataStoreFactory.create(produceFile = { file }))
        local = FakeExpenseRepository("local")
        remote = FakeExpenseRepository("remote")
        repository = DelegatingExpenseRepository(local, remote, settings)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `modo local enruta al repositorio local`() =
        runBlocking {
            settings.setMode(AppMode.LOCAL)

            repository.save(sampleDraft())

            assertEquals(1, local.saves)
            assertEquals(0, remote.saves)
        }

    @Test
    fun `modo remoto enruta al repositorio remoto`() =
        runBlocking {
            settings.setMode(AppMode.REMOTE)

            repository.save(sampleDraft())

            assertEquals(0, local.saves)
            assertEquals(1, remote.saves)
        }

    @Test
    fun `observe emite de la fuente activa`() =
        runBlocking {
            settings.setMode(AppMode.REMOTE)

            val source =
                repository
                    .observe(ExpenseFilter())
                    .first()
                    .firstOrNull()
                    ?.description

            assertEquals("remote", source)
        }

    private fun sampleDraft() =
        ExpenseDraft(
            categoryId = 1L,
            paymentSourceId = 1L,
            currency = CurrencyCode.VES,
            amount = Money.parse("280"),
            commission = Money.parse("14.00"),
            exchangeRate = Rate.parse("28"),
            description = "Mercado",
            spentAt = LocalDate.parse("2026-11-25"),
        )

    private class FakeExpenseRepository(
        private val tag: String,
    ) : ExpenseRepository {
        var saves = 0

        override fun observe(filter: ExpenseFilter) =
            flowOf(
                listOf(
                    Expense(
                        id = 1L,
                        categoryId = 1L,
                        paymentSourceId = 1L,
                        currency = CurrencyCode.USD,
                        paymentMethod = null,
                        commission = null,
                        amount = Money.parse("1"),
                        exchangeRate = null,
                        rateProvider = null,
                        usdAmount = Money.parse("1"),
                        usdtAmount = Money.parse("1"),
                        description = tag,
                        note = null,
                        spentAt = LocalDate.parse("2026-11-25"),
                    ),
                ),
            )

        override suspend fun find(id: Long): Expense? = null

        override suspend fun save(draft: ExpenseDraft): Long {
            saves++
            return 1L
        }

        override suspend fun update(
            id: Long,
            draft: ExpenseDraft,
        ) = Unit

        override suspend fun delete(id: Long) = Unit
    }
}
