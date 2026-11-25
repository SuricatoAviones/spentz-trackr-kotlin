package io.github.suricatoaviones.spentztrackr.feature.expenses

import android.app.Application
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettings
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.EntityInUseException
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateSnapshot
import io.github.suricatoaviones.spentztrackr.domain.service.CommissionCalculator
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.util.Locale

/**
 * UI test del happy path del formulario (criterio de salida F3): alta de un
 * gasto en Bs con comisión — el ejemplo canónico del README del backend:
 * (280 + 14) / 28 = $10.50 / 10.50 USDT.
 *
 * Sin Hilt: el ViewModel se construye con fakes por interfaz. Locale forzado
 * a español para que los asserts de texto sean deterministas.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseFormScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fakeExpenses = FakeExpenseRepository()

    private fun buildViewModel(): ExpenseFormViewModel =
        ExpenseFormViewModel(
            expenses = fakeExpenses,
            categoriesRepo =
                FakeCategoryRepository(
                    listOf(
                        Category(
                            id = 1L,
                            name = "Alimentación",
                            icon = "cart",
                            color = "#10B981",
                            type = CategoryType.EXPENSE,
                            budget = null,
                            isSystem = true,
                        ),
                    ),
                ),
            sourcesRepo =
                FakePaymentSourceRepository(
                    listOf(
                        PaymentSource(id = 1L, name = "Efectivo", icon = "cash", color = "#F59E0B", isSystem = true),
                    ),
                ),
            rates = FakeRateRepository(RateSnapshot(bcv = Rate.parse("30.5000"))),
            commissionSettings =
                FakeCommissionSettingsRepository(
                    CommissionSettings(minCommission = Money.parse("14.00"), ratePercent = BigDecimal("0.30")),
                ),
            conversion = ConversionService(),
            commissionCalculator = CommissionCalculator(),
            clock = kotlin.time.Clock.System,
        )

    private fun setContentWithViewModel(viewModel: ExpenseFormViewModel) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val esContext =
            app.createConfigurationContext(
                Configuration().apply { setLocale(Locale("es")) },
            )
        composeRule.setContent {
            CompositionLocalProvider(LocalContext provides esContext) {
                ExpenseFormScreen(editingId = null, onSaved = {}, viewModel = viewModel)
            }
        }
        // Catálogos cargan async: esperar a que la categoría esté en pantalla.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onNodeWithText("Alimentación").assertExists()
                true
            } catch (expected: AssertionError) {
                false
            }
        }
    }

    @Test
    fun altaDeGastoEnBsConComisionCongelaElEjemploCanonico() {
        setContentWithViewModel(buildViewModel())

        // 1. Monto 280 (USD por defecto) y moneda Bs.
        composeRule.onNodeWithTag(ExpenseFormTags.AMOUNT).performTextInput("280")
        composeRule.onNodeWithText("VES").performClick()

        // 2. La tasa del día (BCV 30.5000) viene precargada → reemplazar por 28.
        composeRule.onNodeWithTag(ExpenseFormTags.RATE).performTextReplacement("28")

        // 3. Método de pago → comisión precargada: max(14.00, 280×0.30%) = 14.00.
        composeRule.onNodeWithText("Pago móvil").performClick()
        composeRule.onNodeWithText("14.00").assertExists()

        // 4. El equivalente en vivo muestra el ejemplo canónico del README.
        composeRule.onNodeWithText("$10.50 · 10.50 USDT").assertExists()

        // 5. Categoría, origen y descripción.
        composeRule.onNodeWithText("Alimentación").performClick()
        composeRule.onNodeWithText("Origen de pago").performClick()
        composeRule.onNodeWithText("Efectivo").performClick()
        composeRule.onNodeWithTag(ExpenseFormTags.DESCRIPTION).performTextInput("Mercado semanal")

        // 6. Guardar: el borrador llega congelable al repositorio.
        composeRule.onNodeWithTag(ExpenseFormTags.SAVE).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) { fakeExpenses.saved.isNotEmpty() }

        val draft = fakeExpenses.saved.single()
        assertEquals(CurrencyCode.VES, draft.currency)
        assertEquals("280.00", draft.amount.toCanonicalString())
        assertEquals("14.00", draft.commission?.toCanonicalString())
        assertEquals("28.0000", draft.exchangeRate?.toCanonicalString())
        assertEquals(PaymentMethod.PAGO_MOVIL, draft.paymentMethod)
        assertEquals(1L, draft.categoryId)
        assertEquals(1L, draft.paymentSourceId)
        assertEquals("Mercado semanal", draft.description)
    }

    @Test
    fun gastoEnUsdOcultaLosCamposDeBs() {
        setContentWithViewModel(buildViewModel())

        composeRule.onNodeWithTag(ExpenseFormTags.RATE).assertDoesNotExist()
        composeRule.onNodeWithText("Comisión").assertDoesNotExist()
        composeRule.onNodeWithText("Método de pago").assertDoesNotExist()
    }

    // ---------- Fakes por interfaz (sin Hilt, sin Room, sin red) ----------

    private class FakeExpenseRepository : ExpenseRepository {
        val saved = mutableListOf<ExpenseDraft>()

        override fun observe(filter: ExpenseFilter): Flow<List<io.github.suricatoaviones.spentztrackr.domain.model.Expense>> =
            flowOf(emptyList())

        override suspend fun find(id: Long): io.github.suricatoaviones.spentztrackr.domain.model.Expense? = null

        override suspend fun save(draft: ExpenseDraft): Long {
            saved += draft
            return saved.size.toLong()
        }

        override suspend fun update(
            id: Long,
            draft: ExpenseDraft,
        ) {
            saved += draft
        }

        override suspend fun delete(id: Long) = Unit
    }

    private class FakeCategoryRepository(
        private val items: List<Category>,
    ) : CategoryRepository {
        override fun observe(type: CategoryType?): Flow<List<Category>> = flowOf(items)

        override suspend fun save(draft: CategoryDraft): Long = 1L

        override suspend fun update(
            id: Long,
            draft: CategoryDraft,
        ) = Unit

        override suspend fun delete(id: Long): Unit = throw EntityInUseException()
    }

    private class FakePaymentSourceRepository(
        private val items: List<PaymentSource>,
    ) : PaymentSourceRepository {
        override fun observe(): Flow<List<PaymentSource>> = flowOf(items)

        override suspend fun save(draft: PaymentSourceDraft): Long = 1L

        override suspend fun update(
            id: Long,
            draft: PaymentSourceDraft,
        ) = Unit

        override suspend fun delete(id: Long) = Unit
    }

    private class FakeRateRepository(
        private val snapshot: RateSnapshot,
    ) : RateRepository {
        override fun observeSnapshot(): Flow<RateSnapshot> = flowOf(snapshot)

        override suspend fun snapshot(): RateSnapshot = snapshot

        override suspend fun saveManualRate(rate: Rate) = Unit
    }

    private class FakeCommissionSettingsRepository(
        private val settings: CommissionSettings,
    ) : CommissionSettingsRepository {
        override suspend fun settings(): CommissionSettings = settings
    }
}
