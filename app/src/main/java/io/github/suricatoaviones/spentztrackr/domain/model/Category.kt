package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.Money

/**
 * Tipo de categoría, espejo del enum `CategoryType` del backend.
 * Separa categorías de gasto e ingreso; el presupuesto solo vive en EXPENSE.
 */
enum class CategoryType(
    val value: String,
) {
    EXPENSE("expense"),
    INCOME("income"),
    ;

    companion object {
        fun fromValue(value: String?): CategoryType? = entries.firstOrNull { it.value == value }
    }
}

/** Categoría del usuario — espejo de `categories` (docs/03). */
data class Category(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    /** Presupuesto mensual en USD; null = sin tope (solo EXPENSE). */
    val budget: Money?,
    val isSystem: Boolean,
)

data class CategoryDraft(
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    val budget: Money? = null,
)
