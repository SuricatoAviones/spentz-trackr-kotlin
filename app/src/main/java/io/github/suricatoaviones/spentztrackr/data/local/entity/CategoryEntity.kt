package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categoría de gasto/ingreso — espejo de `categories` del backend (docs/03).
 * `type` ∈ {expense, income}; los sets por defecto del backend son de gasto.
 */
@Entity(
    tableName = "categories",
    indices = [Index("type")],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** Identificador de icono del set fijo (mismo vocabulario que el backend). */
    val icon: String,
    /** Color hex "#RRGGBB". */
    val color: String,
    /** "expense" | "income". */
    val type: String,
    /** Presupuesto mensual de la categoría, decimal canónico; null = sin tope. */
    val budget: String? = null,
    @ColumnInfo(name = "is_system") val isSystem: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
