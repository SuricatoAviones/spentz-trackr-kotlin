package io.github.suricatoaviones.spentztrackr.data.remote.dto

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.BudgetProgress
import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardCategory
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovement
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovementType
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardSection
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardSource
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardTotals
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardTrendPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTOs del dashboard — shape verificado contra `DashboardController` del
 * backend (routes/api.php: `GET /api/v1/dashboard`). El backend devuelve
 * `expenses`/`incomes` según `tracking_type`; los ausentes quedan vacíos.
 */
@Serializable
data class DashboardDataDto(
    val mode: String? = null,
    val month: String? = null,
    val expenses: DashboardSectionDto? = null,
    val incomes: DashboardSectionDto? = null,
)

@Serializable
data class DashboardSectionDto(
    val totals: DashboardTotalsDto? = null,
    val categories: List<DashboardCategoryDto> = emptyList(),
    val sources: List<DashboardSourceDto> = emptyList(),
    val trend: List<DashboardTrendDto> = emptyList(),
    val budgets: List<DashboardBudgetDto> = emptyList(),
    val recent: List<DashboardRecentDto> = emptyList(),
)

@Serializable
data class DashboardTotalsDto(
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usd: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdt: BigDecimal? = null,
    @SerialName("byCurrency") val byCurrency: DashboardCurrencyTotalsDto? = null,
)

@Serializable
data class DashboardCurrencyTotalsDto(
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usd: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val ves: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdt: BigDecimal? = null,
)

@Serializable
data class DashboardCategoryDto(
    val id: Long,
    val name: String = "",
    val color: String? = null,
    val icon: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val total: BigDecimal? = null,
    val count: Int = 0,
)

@Serializable
data class DashboardSourceDto(
    val id: Long,
    val name: String = "",
    val color: String? = null,
    val icon: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val total: BigDecimal? = null,
)

@Serializable
data class DashboardTrendDto(
    val month: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val total: BigDecimal? = null,
)

@Serializable
data class DashboardBudgetDto(
    val id: Long,
    val name: String = "",
    val icon: String? = null,
    val color: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val spent: BigDecimal? = null,
)

@Serializable
data class DashboardRecentDto(
    val id: Long,
    val description: String = "",
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal? = null,
    val currency: String? = null,
    @SerialName("usd_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdAmount: BigDecimal? = null,
    @SerialName("spent_at") val spentAt: String? = null,
    @SerialName("received_at") val receivedAt: String? = null,
    val category: CategoryRefDto? = null,
)

/** Mapea el dashboard remoto al mismo modelo que usa la UI en modo local. */
internal fun DashboardDataDto.toDomain(month: YearMonth): Dashboard =
    Dashboard(
        month = month,
        expenses = expenses?.toSection(DashboardMovementType.EXPENSE, month) ?: DashboardSection(),
        incomes = incomes?.toSection(DashboardMovementType.INCOME, month) ?: DashboardSection(),
    )

private fun DashboardSectionDto.toSection(
    type: DashboardMovementType,
    fallbackMonth: YearMonth,
): DashboardSection =
    DashboardSection(
        totals =
            DashboardTotals(
                usd = totals?.usd.toMoney(),
                usdt = totals?.usdt.toMoney(),
                byCurrency =
                    CurrencyCode.entries.associateWith { code ->
                        val raw =
                            when (code) {
                                CurrencyCode.USD -> totals?.byCurrency?.usd
                                CurrencyCode.VES -> totals?.byCurrency?.ves
                                CurrencyCode.USDT -> totals?.byCurrency?.usdt
                            }
                        raw.toMoney()
                    },
            ),
        categories =
            categories.map { dto ->
                DashboardCategory(
                    id = dto.id,
                    name = dto.name,
                    icon = dto.icon.orEmpty(),
                    color = dto.color.orEmpty(),
                    total = dto.total.toMoney(),
                    count = dto.count,
                )
            },
        sources =
            sources.map { dto ->
                DashboardSource(
                    id = dto.id,
                    name = dto.name,
                    icon = dto.icon.orEmpty(),
                    color = dto.color.orEmpty(),
                    total = dto.total.toMoney(),
                )
            },
        trend =
            trend.mapNotNull { dto ->
                dto.month?.toYearMonthOrNull()?.let { trendMonth ->
                    DashboardTrendPoint(month = trendMonth, total = dto.total.toMoney())
                }
            },
        budgets =
            budgets.map { dto ->
                BudgetProgress(
                    id = dto.id,
                    name = dto.name,
                    icon = dto.icon.orEmpty(),
                    color = dto.color.orEmpty(),
                    amount = dto.amount.toMoney(),
                    spent = dto.spent.toMoney(),
                )
            },
        recent =
            recent.map { dto ->
                DashboardMovement(
                    id = dto.id,
                    type = type,
                    description = dto.description,
                    amount = dto.amount.toMoney(),
                    currency = CurrencyCode.fromValue(dto.currency) ?: CurrencyCode.USD,
                    usdAmount = dto.usdAmount.toMoney(),
                    date =
                        (dto.receivedAt ?: dto.spentAt)
                            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                            ?: fallbackMonth.firstDay,
                    categoryName = dto.category?.name.orEmpty(),
                    categoryIcon = dto.category?.icon.orEmpty(),
                    categoryColor = dto.category?.color.orEmpty(),
                )
            },
    )

private fun BigDecimal?.toMoney(): Money = this?.let(Money::of) ?: Money.ZERO

private fun String.toYearMonthOrNull(): YearMonth? {
    val parts = trim().split("-")
    if (parts.size != 2) return null
    return runCatching { YearMonth(parts[0].toInt(), parts[1].toInt()) }.getOrNull()
}
