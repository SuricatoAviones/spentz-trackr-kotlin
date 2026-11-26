package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.BudgetProgress
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardCategory
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovement
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovementType
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardSection
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardSource
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardTotals
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardTrendPoint
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agregación del dashboard local (docs/08 F6) sobre los equivalentes
 * **congelados** del mes. Se calcula en `BigDecimal` (`Money`) para que las
 * sumas del dashboard cuadren exactamente con las de las listas; Room hace el
 * filtrado por fecha.
 *
 * También se usa como forma canónica para mapear el dashboard remoto: la UI
 * recibe el mismo [Dashboard] en ambos modos.
 */
@Singleton
class DashboardAggregator
    @Inject
    constructor() {
        fun build(
            month: YearMonth,
            expenses: List<Expense>,
            incomes: List<Income>,
            categories: List<Category>,
            sources: List<PaymentSource>,
        ): Dashboard {
            val categoriesById = categories.associateBy { it.id }
            val sourcesById = sources.associateBy { it.id }

            return Dashboard(
                month = month,
                expenses =
                    buildSection(
                        month = month,
                        rows = expenses.map { it.toRow() },
                        categoriesById = categoriesById,
                        sourcesById = sourcesById,
                        withBudgets = true,
                    ),
                incomes =
                    buildSection(
                        month = month,
                        rows = incomes.map { it.toRow() },
                        categoriesById = categoriesById,
                        sourcesById = sourcesById,
                        withBudgets = false,
                    ),
            )
        }

        private fun buildSection(
            month: YearMonth,
            rows: List<Row>,
            categoriesById: Map<Long, Category>,
            sourcesById: Map<Long, PaymentSource>,
            withBudgets: Boolean,
        ): DashboardSection {
            val monthRows = rows.filter { it.date.inMonth(month) }

            val totals =
                DashboardTotals(
                    usd = monthRows.sumOf { it.usdAmount },
                    usdt = monthRows.sumOf { it.usdtAmount },
                    byCurrency =
                        CurrencyCode.entries.associateWith { code ->
                            monthRows.filter { it.currency == code }.sumOf { it.amount }
                        },
                )

            val categories =
                monthRows
                    .groupBy { it.categoryId }
                    .map { (categoryId, group) ->
                        val category = categoriesById[categoryId]
                        DashboardCategory(
                            id = categoryId,
                            name = category?.name ?: "—",
                            icon = category?.icon.orEmpty(),
                            color = category?.color.orEmpty(),
                            total = group.sumOf { it.usdAmount },
                            count = group.size,
                        )
                    }.sortedByDescending { it.total }
                    .take(TOP_CATEGORIES)

            val sources =
                monthRows
                    .filter { it.sourceId != null }
                    .groupBy { it.sourceId }
                    .map { (sourceId, group) ->
                        val source = sourceId?.let { sourcesById[it] }
                        DashboardSource(
                            id = sourceId ?: 0L,
                            name = source?.name ?: "—",
                            icon = source?.icon.orEmpty(),
                            color = source?.color.orEmpty(),
                            total = group.sumOf { it.usdAmount },
                        )
                    }.sortedByDescending { it.total }

            val trend =
                (TREND_MONTHS - 1 downTo 0).map { offset ->
                    val trendMonth = month.minus(offset, DateTimeUnit.MONTH)
                    DashboardTrendPoint(
                        month = trendMonth,
                        total = rows.filter { it.date.inMonth(trendMonth) }.sumOf { it.usdAmount },
                    )
                }

            val budgets =
                if (!withBudgets) {
                    emptyList()
                } else {
                    categoriesById.values
                        .filter { it.type == CategoryType.EXPENSE && it.budget != null }
                        .map { category ->
                            BudgetProgress(
                                id = category.id,
                                name = category.name,
                                icon = category.icon,
                                color = category.color,
                                amount = category.budget ?: Money.ZERO,
                                spent =
                                    monthRows
                                        .filter { it.categoryId == category.id }
                                        .sumOf { it.usdAmount },
                            )
                        }.sortedByDescending { it.spent }
                }

            val recent =
                monthRows
                    .sortedWith(compareByDescending<Row> { it.date }.thenByDescending { it.id })
                    .take(RECENT_LIMIT)
                    .map { row ->
                        val category = categoriesById[row.categoryId]
                        DashboardMovement(
                            id = row.id,
                            type = row.type,
                            description = row.description,
                            amount = row.amount,
                            currency = row.currency,
                            usdAmount = row.usdAmount,
                            date = row.date,
                            categoryName = category?.name.orEmpty(),
                            categoryIcon = category?.icon.orEmpty(),
                            categoryColor = category?.color.orEmpty(),
                        )
                    }

            return DashboardSection(
                totals = totals,
                categories = categories,
                sources = sources,
                trend = trend,
                budgets = budgets,
                recent = recent,
            )
        }

        private fun Expense.toRow(): Row =
            Row(
                id = id,
                type = DashboardMovementType.EXPENSE,
                description = description,
                amount = amount,
                currency = currency,
                usdAmount = usdAmount,
                usdtAmount = usdtAmount,
                date = spentAt,
                categoryId = categoryId,
                sourceId = paymentSourceId,
            )

        private fun Income.toRow(): Row =
            Row(
                id = id,
                type = DashboardMovementType.INCOME,
                description = description,
                amount = amount,
                currency = currency,
                usdAmount = usdAmount,
                usdtAmount = usdtAmount,
                date = receivedAt,
                categoryId = categoryId,
                sourceId = null,
            )

        private fun LocalDate.inMonth(month: YearMonth): Boolean = this >= month.firstDay && this <= month.lastDay

        private fun List<Row>.sumOf(selector: (Row) -> Money): Money = fold(Money.ZERO) { acc, row -> acc + selector(row) }

        private data class Row(
            val id: Long,
            val type: DashboardMovementType,
            val description: String,
            val amount: Money,
            val currency: CurrencyCode,
            val usdAmount: Money,
            val usdtAmount: Money,
            val date: LocalDate,
            val categoryId: Long,
            val sourceId: Long?,
        )

        private companion object {
            const val TOP_CATEGORIES = 6
            const val TREND_MONTHS = 6
            const val RECENT_LIMIT = 10
        }
    }
