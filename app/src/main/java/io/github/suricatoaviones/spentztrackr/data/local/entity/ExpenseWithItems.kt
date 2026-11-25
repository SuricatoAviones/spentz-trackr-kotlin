package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Gasto con sus porciones mixtas (relación 1-N). */
data class ExpenseWithItems(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "id", entityColumn = "expense_id")
    val items: List<ExpenseItemEntity>,
)
