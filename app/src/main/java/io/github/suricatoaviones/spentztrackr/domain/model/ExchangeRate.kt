package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import kotlinx.datetime.LocalDate

/**
 * Procedencia de una fila de `exchange_rates` — el backend permite `seed`
 * pero en la práctica solo existen api y manual (docs/03).
 */
enum class RateSource(
    val value: String,
) {
    API("api"),
    MANUAL("manual"),
    ;

    companion object {
        fun fromValue(value: String?): RateSource? = entries.firstOrNull { it.value == value }
    }
}

/**
 * Tasa de un día concreto (espejo de `exchange_rates`):
 * API = la trajo dolarapi (BCV/paralelo); MANUAL = override del usuario.
 * El histórico se conserva: los gastos viejos ya congelaron su propia tasa.
 */
data class ExchangeRate(
    val id: Long = 0L,
    val source: RateSource,
    val provider: RateProvider,
    val rate: Rate,
    val rateDate: LocalDate,
    /** Epoch ms del último upsert (para "hace X min" en la UI); 0 si no aplica. */
    val updatedAt: Long = 0L,
)
