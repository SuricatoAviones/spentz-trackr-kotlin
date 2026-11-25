package io.github.suricatoaviones.spentztrackr.data.local

import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.ExpenseWithItems
import io.github.suricatoaviones.spentztrackr.data.local.entity.IncomeEntity
import io.github.suricatoaviones.spentztrackr.data.local.entity.PaymentSourceEntity
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseItem
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.IncomeDraft
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import io.github.suricatoaviones.spentztrackr.domain.service.ConversionService
import kotlinx.datetime.LocalDate

/**
 * Mapeadores entidad Room ↔ dominio (ADR-A05: espejo 1:1 del backend).
 * Las entidades guardan dinero como texto canónico; el dominio materializa
 * `Money`/`Rate` (BigDecimal, ADR-A03).
 */

private fun String.toMoney(): Money = Money.parse(this)

private fun String?.toMoneyOrNull(): Money? = this?.let(Money::parse)

private fun String?.toRateOrNull(): Rate? = this?.let(Rate::parse)

private fun Money.toText(): String = toCanonicalString()

private fun Rate?.toTextOrNull(): String? = this?.toCanonicalString()

fun ExpenseWithItems.toDomain(): Expense =
    Expense(
        id = expense.id,
        categoryId = expense.categoryId,
        paymentSourceId = expense.paymentSourceId,
        currency = expense.currency,
        paymentMethod = expense.paymentMethod,
        commission = expense.commission.toMoneyOrNull(),
        amount = expense.amount.toMoney(),
        exchangeRate = expense.exchangeRate.toRateOrNull(),
        rateProvider = expense.rateProvider,
        usdAmount = expense.usdAmount.toMoney(),
        usdtAmount = expense.usdtAmount.toMoney(),
        description = expense.description,
        note = expense.note,
        spentAt = LocalDate.parse(expense.spentAt),
        items =
            items.map { entity ->
                ExpenseItem(
                    id = entity.id,
                    expenseId = entity.expenseId,
                    currency = entity.currency,
                    amount = entity.amount.toMoney(),
                    exchangeRate = entity.exchangeRate.toRateOrNull(),
                    usdAmount = entity.usdAmount.toMoney(),
                    usdtAmount = entity.usdtAmount.toMoney(),
                )
            },
    )

fun ExpenseDraft.toEntity(
    frozen: ConversionService.FrozenAmounts,
    now: Long,
    id: Long = 0L,
): ExpenseEntity =
    ExpenseEntity(
        id = id,
        categoryId = categoryId,
        paymentSourceId = paymentSourceId,
        currency = currency,
        paymentMethod = paymentMethod,
        commission = commission?.toText(),
        amount = amount.toText(),
        exchangeRate = exchangeRate.toTextOrNull(),
        rateProvider = rateProvider,
        usdAmount = frozen.usd.toText(),
        usdtAmount = frozen.usdt.toText(),
        description = description,
        note = note,
        spentAt = spentAt.toString(),
        createdAt = now,
        updatedAt = now,
    )

fun IncomeEntity.toDomain(): Income =
    Income(
        id = id,
        categoryId = categoryId,
        currency = currency,
        amount = amount.toMoney(),
        exchangeRate = exchangeRate.toRateOrNull(),
        rateProvider = rateProvider,
        usdAmount = usdAmount.toMoney(),
        usdtAmount = usdtAmount.toMoney(),
        description = description,
        note = note,
        receivedAt = LocalDate.parse(receivedAt),
    )

fun IncomeDraft.toEntity(
    frozen: ConversionService.FrozenAmounts,
    now: Long,
    id: Long = 0L,
): IncomeEntity =
    IncomeEntity(
        id = id,
        categoryId = categoryId,
        currency = currency,
        amount = amount.toText(),
        exchangeRate = exchangeRate.toTextOrNull(),
        rateProvider = rateProvider,
        usdAmount = frozen.usd.toText(),
        usdtAmount = frozen.usdt.toText(),
        description = description,
        note = note,
        receivedAt = receivedAt.toString(),
        createdAt = now,
        updatedAt = now,
    )

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        budget = budget.toMoneyOrNull(),
        isSystem = isSystem,
    )

fun CategoryDraft.toEntity(
    now: Long,
    isSystem: Boolean = false,
    id: Long = 0L,
): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        budget = budget?.toText(),
        isSystem = isSystem,
        createdAt = now,
        updatedAt = now,
    )

fun PaymentSourceEntity.toDomain(): PaymentSource =
    PaymentSource(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isSystem = isSystem,
    )

fun PaymentSourceDraft.toEntity(
    now: Long,
    isSystem: Boolean = false,
    id: Long = 0L,
): PaymentSourceEntity =
    PaymentSourceEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isSystem = isSystem,
        createdAt = now,
        updatedAt = now,
    )
