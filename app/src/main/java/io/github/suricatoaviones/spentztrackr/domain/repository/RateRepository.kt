package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import kotlinx.coroutines.flow.Flow

/**
 * Tasas vigentes (BCV, paralelo, manual) en cualquier modo. En local salen
 * de las filas del día en Room; en remoto de `GET /rates` (el historial
 * completo vive en el servidor).
 */
data class RateSnapshot(
    val bcv: Rate? = null,
    val paralelo: Rate? = null,
    val manual: Rate? = null,
) {
    /** Tasa preferida para precargar el formulario: manual > BCV > paralelo. */
    fun preferred(): Rate? = manual ?: bcv ?: paralelo
}

interface RateRepository {
    fun observeSnapshot(): Flow<RateSnapshot>

    suspend fun snapshot(): RateSnapshot

    /** Guarda la tasa manual del día (gana sobre la API en formularios). */
    suspend fun saveManualRate(rate: Rate)
}
