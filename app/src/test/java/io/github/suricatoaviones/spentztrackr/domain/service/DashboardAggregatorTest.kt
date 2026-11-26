package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovementType
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Criterio de salida de la Fase 6: los totales del dashboard cuadran con la
 * suma de los movimientos del mes, las categorías se agrupan y el presupuesto
 * compara gastado contra tope.
 */
class DashboardAggregatorTest {
    private val aggregator = DashboardAggregator()
    private val month = YearMonth(2026, 11)
    private val today = LocalDate.parse("2026-11-25")
    private val yesterday = LocalDate.parse("2026-11-24")
    private val previousMonthDay = LocalDate.parse("2026-10-10")

    private val expenseCategory =
        Category(
            id = 1L,
            name = "Comida",
            icon = "coffee",
            color = "#FF8800",
            type = CategoryType.EXPENSE,
            budget = Money.parse("100.00"),
            isSystem = false,
        )
    private val otherCategory =
        Category(
            id = 2L,
            name = "Transporte",
            icon = "car",
            color = "#0088FF",
            type = CategoryType.EXPENSE,
            budget = null,
            isSystem = false,
        )
    private val incomeCategory =
        Category(
            id = 10L,
            name = "Salario",
            icon = "wallet",
            color = "#00AA55",
            type = CategoryType.INCOME,
            budget = null,
            isSystem = false,
        )
    private val source = PaymentSource(id = 5L, name = "Efectivo", icon = "wallet", color = "#333333", isSystem = false)

    @Test
    fun `totales y desglose por moneda suman los equivalentes congelados`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "10.00", currency = CurrencyCode.USD, usd = "10.00", date = today),
                expense(id = 2, amount = "280.00", currency = CurrencyCode.VES, usd = "10.50", date = yesterday),
                expense(id = 3, amount = "5.00", currency = CurrencyCode.USDT, usd = "5.00", date = today),
            )

        val dashboard =
            aggregator.build(
                month = month,
                expenses = expenses,
                incomes = emptyList(),
                categories = listOf(expenseCategory, otherCategory),
                sources = listOf(source),
            )

        assertEquals(
            "25.50",
            dashboard.expenses.totals.usd
                .toCanonicalString(),
        )
        assertEquals(
            "25.50",
            dashboard.expenses.totals.usdt
                .toCanonicalString(),
        )
        assertEquals(
            "10.00",
            dashboard.expenses.totals.byCurrency[CurrencyCode.USD]
                ?.toCanonicalString(),
        )
        assertEquals(
            "280.00",
            dashboard.expenses.totals.byCurrency[CurrencyCode.VES]
                ?.toCanonicalString(),
        )
        assertEquals(
            "5.00",
            dashboard.expenses.totals.byCurrency[CurrencyCode.USDT]
                ?.toCanonicalString(),
        )

        val manualSum = expenses.fold(Money.ZERO) { acc, e -> acc + e.usdAmount }
        assertEquals(
            manualSum.toCanonicalString(),
            dashboard.expenses.totals.usd
                .toCanonicalString(),
        )
    }

    @Test
    fun `agrupa categorias y ordena por total congelado`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "10.00", currency = CurrencyCode.USD, usd = "10.00", date = today, categoryId = 1),
                expense(id = 2, amount = "280.00", currency = CurrencyCode.VES, usd = "10.50", date = yesterday, categoryId = 1),
                expense(id = 3, amount = "5.00", currency = CurrencyCode.USDT, usd = "5.00", date = today, categoryId = 2),
            )

        val dashboard =
            aggregator.build(month, expenses, emptyList(), listOf(expenseCategory, otherCategory), listOf(source))

        val categories = dashboard.expenses.categories
        assertEquals(listOf(1L, 2L), categories.map { it.id })
        assertEquals("20.50", categories[0].total.toCanonicalString())
        assertEquals(2, categories[0].count)
        assertEquals("Comida", categories[0].name)
        assertEquals("5.00", categories[1].total.toCanonicalString())
    }

    @Test
    fun `el presupuesto por categoria compara gastado contra tope`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "10.00", currency = CurrencyCode.USD, usd = "10.00", date = today, categoryId = 1),
                expense(id = 2, amount = "280.00", currency = CurrencyCode.VES, usd = "10.50", date = yesterday, categoryId = 1),
            )

        val dashboard =
            aggregator.build(month, expenses, emptyList(), listOf(expenseCategory, otherCategory), listOf(source))

        assertEquals(1, dashboard.expenses.budgets.size)
        val budget = dashboard.expenses.budgets.single()
        assertEquals("100.00", budget.amount.toCanonicalString())
        assertEquals("20.50", budget.spent.toCanonicalString())
        assertFalse(budget.exceeded)
        assertEquals(0.205f, budget.ratio, 0.0001f)
    }

    @Test
    fun `marca el presupuesto excedido`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "150.00", currency = CurrencyCode.USD, usd = "150.00", date = today, categoryId = 1),
            )

        val dashboard =
            aggregator.build(month, expenses, emptyList(), listOf(expenseCategory), listOf(source))

        val budget = dashboard.expenses.budgets.single()
        assertTrue(budget.exceeded)
        assertEquals(1f, budget.ratio, 0.0001f)
    }

    @Test
    fun `los ingresos tienen su propio total y recientes`() {
        val incomes =
            listOf(
                income(id = 7, amount = "100.00", currency = CurrencyCode.USD, usd = "100.00", date = today),
                income(id = 8, amount = "280.00", currency = CurrencyCode.VES, usd = "10.50", date = yesterday),
            )

        val dashboard =
            aggregator.build(month, emptyList(), incomes, listOf(incomeCategory), listOf(source))

        assertEquals(
            "110.50",
            dashboard.incomes.totals.usd
                .toCanonicalString(),
        )
        assertEquals(2, dashboard.incomes.recent.size)
        assertEquals(
            DashboardMovementType.INCOME,
            dashboard.incomes.recent
                .first()
                .type,
        )
        assertEquals(
            "Salario",
            dashboard.incomes.recent
                .first()
                .categoryName,
        )
        assertTrue(dashboard.expenses.isEmpty)
    }

    @Test
    fun `la tendencia cubre seis meses con el actual al final`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "10.00", currency = CurrencyCode.USD, usd = "10.00", date = today),
                expense(
                    id = 2,
                    amount = "20.00",
                    currency = CurrencyCode.USD,
                    usd = "20.00",
                    date = previousMonthDay,
                ),
            )

        val dashboard =
            aggregator.build(month, expenses, emptyList(), listOf(expenseCategory), listOf(source))

        val trend = dashboard.expenses.trend
        assertEquals(6, trend.size)
        assertEquals(month, trend.last().month)
        assertEquals("10.00", trend.last().total.toCanonicalString())
        assertEquals("20.00", trend[trend.size - 2].total.toCanonicalString())
    }

    @Test
    fun `movimientos fuera del mes no entran en totales del mes`() {
        val expenses =
            listOf(
                expense(id = 1, amount = "10.00", currency = CurrencyCode.USD, usd = "10.00", date = today),
                expense(
                    id = 2,
                    amount = "20.00",
                    currency = CurrencyCode.USD,
                    usd = "20.00",
                    date = previousMonthDay,
                ),
            )

        val dashboard =
            aggregator.build(month, expenses, emptyList(), listOf(expenseCategory), listOf(source))

        assertEquals(
            "10.00",
            dashboard.expenses.totals.usd
                .toCanonicalString(),
        )
        assertEquals(1, dashboard.expenses.recent.size)
    }

    private fun expense(
        id: Long,
        amount: String,
        currency: CurrencyCode,
        usd: String,
        date: LocalDate,
        categoryId: Long = 1L,
    ) = Expense(
        id = id,
        categoryId = categoryId,
        paymentSourceId = 5L,
        currency = currency,
        paymentMethod = null,
        commission = null,
        amount = Money.parse(amount),
        exchangeRate = if (currency == CurrencyCode.VES) Rate.parse("28.0000") else null,
        rateProvider = null,
        usdAmount = Money.parse(usd),
        usdtAmount = Money.parse(usd),
        description = "Gasto $id",
        note = null,
        spentAt = date,
    )

    private fun income(
        id: Long,
        amount: String,
        currency: CurrencyCode,
        usd: String,
        date: LocalDate,
    ) = Income(
        id = id,
        categoryId = 10L,
        currency = currency,
        amount = Money.parse(amount),
        exchangeRate = if (currency == CurrencyCode.VES) Rate.parse("28.0000") else null,
        rateProvider = null,
        usdAmount = Money.parse(usd),
        usdtAmount = Money.parse(usd),
        description = "Ingreso $id",
        note = null,
        receivedAt = date,
    )
}
