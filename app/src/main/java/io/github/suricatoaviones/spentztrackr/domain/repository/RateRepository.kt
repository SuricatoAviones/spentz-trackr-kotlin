package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
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
    /** true si no hay tasa de hoy y se usa la última conocida (ADR-002). */
    val estimated: Boolean = false,
    /** Proveedor de la tasa preferida (USER si hay override manual). */
    val provider: RateProvider? = null,
    /** Epoch ms del último refresco conocido; null si no aplica (remoto). */
    val updatedAt: Long? = null,
) {
    /** Tasa preferida para precargar el formulario: manual > BCV > paralelo. */
    fun preferred(): Rate? = manual ?: bcv ?: paralelo
}

interface RateRepository {
    fun observeSnapshot(): Flow<RateSnapshot>

    suspend fun snapshot(): RateSnapshot

    /** Guarda la tasa manual del día (gana sobre la API en formularios). */
    suspend fun saveManualRate(rate: Rate)

    /**
     * Histórico reciente para la pantalla de Tasas (últimos [days] días).
     * En remoto la API v1 solo expone las tasas vigentes: lista vacía.
     */
    fun observeHistory(days: Int = 30): Flow<List<ExchangeRate>>

    /**
     * Fuerza la sincronización: local contra dolarapi, remoto con
     * `POST /rates/sync`. @return true si trajo datos (o el servidor aceptó).
     */
    suspend fun syncNow(): Boolean

    /**
     * Refresco perezoso y **no bloqueante** al abrir formularios: solo actúa
     * si la última sincronización es más vieja que [maxAgeMinutes]. En remoto
     * no hace nada (el servidor es el dueño de la tasa).
     */
    suspend fun refreshIfStale(maxAgeMinutes: Int = 60)
}
