package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Lado de un movimiento reciente (para abrir su detalle desde Inicio). */
enum class DashboardMovementType { EXPENSE, INCOME }

/** Respuesta del dashboard en cualquier modo (ADR-A01). */
data class Dashboard(
    val month: YearMonth,
    val expenses: DashboardSection,
    val incomes: DashboardSection,
) {
    val isEmpty: Boolean get() = expenses.isEmpty && incomes.isEmpty
}

/** Totales congelados del mes + desglose por moneda original (como el backend). */
data class DashboardTotals(
    val usd: Money = Money.ZERO,
    val usdt: Money = Money.ZERO,
    val byCurrency: Map<CurrencyCode, Money> = CurrencyCode.entries.associateWith { Money.ZERO },
)

data class DashboardCategory(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    /** Suma de `usd_amount` congelados de la categoría. */
    val total: Money,
    val count: Int,
)

data class DashboardSource(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val total: Money,
)

data class DashboardTrendPoint(
    val month: YearMonth,
    val total: Money,
)

/** Presupuesto de una categoría con el gastado del mes en curso. */
data class BudgetProgress(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val amount: Money,
    val spent: Money,
) {
    val exceeded: Boolean get() = spent > amount

    /** 0..1 para la barra; satura al 100 % y `exceeded` avisa del exceso. */
    val ratio: Float
        get() =
            if (amount.value.signum() <= 0) {
                0f
            } else {
                (spent.value.divide(amount.value, 4, java.math.RoundingMode.HALF_UP).toFloat()).coerceIn(0f, 1f)
            }
}

data class DashboardMovement(
    val id: Long,
    val type: DashboardMovementType,
    val description: String,
    val amount: Money,
    val currency: CurrencyCode,
    val usdAmount: Money,
    val date: LocalDate,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
)

data class DashboardSection(
    val totals: DashboardTotals = DashboardTotals(),
    val categories: List<DashboardCategory> = emptyList(),
    val sources: List<DashboardSource> = emptyList(),
    val trend: List<DashboardTrendPoint> = emptyList(),
    val budgets: List<BudgetProgress> = emptyList(),
    val recent: List<DashboardMovement> = emptyList(),
) {
    val isEmpty: Boolean get() = recent.isEmpty()
}
