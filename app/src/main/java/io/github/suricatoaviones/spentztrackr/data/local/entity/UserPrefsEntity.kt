package io.github.suricatoaviones.spentztrackr.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Fila única (id = 1) con las preferencias del usuario local — espejo de las
 * columnas de negocio de `users` del backend (docs/03). Dinero como texto
 * decimal canónico (ADR-A03).
 */
@Entity(tableName = "user_prefs")
data class UserPrefsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "default_display_currency") val defaultDisplayCurrency: String = "usd",
    @ColumnInfo(name = "tracking_type") val trackingType: String = "both",
    @ColumnInfo(name = "monthly_budget") val monthlyBudget: String? = null,
    @ColumnInfo(name = "min_commission") val minCommission: String = "14.00",
    @ColumnInfo(name = "commission_rate") val commissionRate: String = "0.30",
    @ColumnInfo(name = "locale") val locale: String? = null,
    @ColumnInfo(name = "appearance") val appearance: String = "system",
)
