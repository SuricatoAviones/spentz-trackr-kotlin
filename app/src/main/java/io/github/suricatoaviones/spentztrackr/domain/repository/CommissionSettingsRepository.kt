package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.Money
import java.math.BigDecimal

/**
 * Ajustes de comisión Bs del usuario (ADR-006 backend): piso y porcentaje con
 * los que el formulario precarga `max(mín, monto × %)`. El valor sigue siendo
 * editable en el formulario ("sin comisión" disponible).
 */
data class CommissionSettings(
    val minCommission: Money,
    /** Porcentaje (ej. 0.30 = 0.30 %). */
    val ratePercent: BigDecimal,
)

interface CommissionSettingsRepository {
    suspend fun settings(): CommissionSettings
}
