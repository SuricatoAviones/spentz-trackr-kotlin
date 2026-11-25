package io.github.suricatoaviones.spentztrackr.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTOs de gastos e ingresos — shapes verificados contra
 * `ExpensePresenter` del backend, `StoreExpenseRequest`,
 * `StoreIncomeRequest` y `docs/api/api-documentation.md`.
 * Los decimales aceptan número o string ([FlexibleBigDecimalSerializer]).
 */

@Serializable
data class CategoryRefDto(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
)

@Serializable
data class SourceRefDto(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
)

// ---------- Gastos ----------

@Serializable
data class ExpenseItemRequestDto(
    val currency: String,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
)

@Serializable
data class ExpenseRequestDto(
    @SerialName("category_id") val categoryId: Long,
    @SerialName("payment_source_id") val paymentSourceId: Long,
    val currency: String,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val commission: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
    @SerialName("rate_provider") val rateProvider: String? = null,
    val description: String,
    val note: String? = null,
    @SerialName("spent_at") val spentAt: String,
    val items: List<ExpenseItemRequestDto>? = null,
)

@Serializable
data class ExpenseItemDto(
    val id: Long,
    val currency: String,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
    @SerialName("usd_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdAmount: BigDecimal,
    @SerialName("usdt_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdtAmount: BigDecimal,
)

/** Shape exacto de `ExpensePresenter::present`. */
@Serializable
data class ExpenseDto(
    val id: Long,
    val description: String,
    val note: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    val currency: String,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val commission: BigDecimal? = null,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
    @SerialName("rate_provider") val rateProvider: String? = null,
    @SerialName("usd_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdAmount: BigDecimal,
    @SerialName("usdt_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdtAmount: BigDecimal,
    @SerialName("spent_at") val spentAt: String,
    val category: CategoryRefDto,
    val source: SourceRefDto,
    @SerialName("has_receipt") val hasReceipt: Boolean = false,
    val items: List<ExpenseItemDto> = emptyList(),
)

@Serializable
data class TotalsDto(
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usd: BigDecimal? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdt: BigDecimal? = null,
)

@Serializable
data class ExpensesListDataDto(
    val expenses: List<ExpenseDto> = emptyList(),
    val totals: TotalsDto? = null,
)

// ---------- Ingresos ----------

@Serializable
data class IncomeRequestDto(
    @SerialName("category_id") val categoryId: Long,
    val currency: String,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
    @SerialName("rate_provider") val rateProvider: String? = null,
    val description: String,
    val note: String? = null,
    @SerialName("received_at") val receivedAt: String,
)

/** Análogo al de gastos, sin método/comisión/ítems ni origen. */
@Serializable
data class IncomeDto(
    val id: Long,
    val description: String,
    val note: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val amount: BigDecimal,
    val currency: String,
    @SerialName("exchange_rate")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val exchangeRate: BigDecimal? = null,
    @SerialName("rate_provider") val rateProvider: String? = null,
    @SerialName("usd_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdAmount: BigDecimal,
    @SerialName("usdt_amount")
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val usdtAmount: BigDecimal,
    @SerialName("received_at") val receivedAt: String,
    val category: CategoryRefDto,
)

@Serializable
data class IncomesListDataDto(
    val incomes: List<IncomeDto> = emptyList(),
    val totals: TotalsDto? = null,
)
