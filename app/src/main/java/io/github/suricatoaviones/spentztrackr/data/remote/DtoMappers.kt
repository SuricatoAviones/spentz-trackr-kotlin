package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.data.remote.dto.CategoryDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.CategoryRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseItemDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseItemRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.IncomeDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.IncomeRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.SourceDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.SourceRequestDto
import io.github.suricatoaviones.spentztrackr.domain.model.Category
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryDraft
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.Expense
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseItem
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseItemDraft
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import io.github.suricatoaviones.spentztrackr.domain.model.IncomeDraft
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import kotlinx.datetime.LocalDate

/**
 * Mapeadores DTO ↔ dominio. Los DTOs son la forma del wire; el dominio, la
 * forma de la app. Fechas ISO `YYYY-MM-DD`; enums por su valor backend.
 */

fun ExpenseDto.toDomain(): Expense =
    Expense(
        id = id,
        categoryId = category.id,
        paymentSourceId = source.id,
        currency =
            CurrencyCode.fromValue(currency)
                ?: error("Moneda desconocida en la respuesta: '$currency'"),
        paymentMethod = PaymentMethod.fromValue(paymentMethod),
        commission = commission?.let(Money::of),
        amount = Money.of(amount),
        exchangeRate = exchangeRate?.let(Rate::of),
        rateProvider = RateProvider.fromValue(rateProvider),
        usdAmount = Money.of(usdAmount),
        usdtAmount = Money.of(usdtAmount),
        description = description,
        note = note,
        spentAt = LocalDate.parse(spentAt),
        items = items.map(ExpenseItemDto::toDomain),
    )

fun ExpenseItemDto.toDomain(): ExpenseItem =
    ExpenseItem(
        id = id,
        currency =
            CurrencyCode.fromValue(currency)
                ?: error("Moneda desconocida en la respuesta: '$currency'"),
        amount = Money.of(amount),
        exchangeRate = exchangeRate?.let(Rate::of),
        usdAmount = Money.of(usdAmount),
        usdtAmount = Money.of(usdtAmount),
    )

fun ExpenseDraft.toRequestDto(): ExpenseRequestDto =
    ExpenseRequestDto(
        categoryId = categoryId,
        paymentSourceId = paymentSourceId,
        currency = currency.value,
        paymentMethod = paymentMethod?.value,
        commission = commission?.value,
        amount = amount.value,
        exchangeRate = exchangeRate?.value,
        rateProvider = rateProvider?.value,
        description = description,
        note = note,
        spentAt = spentAt.toString(),
        items = items.takeIf { it.isNotEmpty() }?.map { it.toRequestDto() },
    )

fun ExpenseItemDraft.toRequestDto(): ExpenseItemRequestDto =
    ExpenseItemRequestDto(
        currency = currency.value,
        amount = amount.value,
        exchangeRate = exchangeRate?.value,
    )

// ---------- Ingresos ----------

fun IncomeDto.toDomain(): Income =
    Income(
        id = id,
        categoryId = category.id,
        currency =
            CurrencyCode.fromValue(currency)
                ?: error("Moneda desconocida en la respuesta: '$currency'"),
        amount = Money.of(amount),
        exchangeRate = exchangeRate?.let(Rate::of),
        rateProvider = RateProvider.fromValue(rateProvider),
        usdAmount = Money.of(usdAmount),
        usdtAmount = Money.of(usdtAmount),
        description = description,
        note = note,
        receivedAt = LocalDate.parse(receivedAt),
    )

fun IncomeDraft.toRequestDto(): IncomeRequestDto =
    IncomeRequestDto(
        categoryId = categoryId,
        currency = currency.value,
        amount = amount.value,
        exchangeRate = exchangeRate?.value,
        rateProvider = rateProvider?.value,
        description = description,
        note = note,
        receivedAt = receivedAt.toString(),
    )

// ---------- Catálogos ----------

fun CategoryDto.toDomain(): Category =
    Category(
        id = id,
        name = name,
        icon = icon.orEmpty(),
        color = color.orEmpty(),
        type = CategoryType.fromValue(type) ?: CategoryType.EXPENSE,
        budget = budget?.let(Money::of),
        isSystem = isSystem,
    )

fun CategoryDraft.toRequestDto(): CategoryRequestDto =
    CategoryRequestDto(
        name = name,
        icon = icon,
        color = color,
        type = type.value,
        budget = budget?.value,
    )

fun SourceDto.toDomain(): PaymentSource =
    PaymentSource(
        id = id,
        name = name,
        icon = icon.orEmpty(),
        color = color.orEmpty(),
        isSystem = isSystem,
    )

fun PaymentSourceDraft.toRequestDto(): SourceRequestDto =
    SourceRequestDto(
        name = name,
        icon = icon,
        color = color,
    )
