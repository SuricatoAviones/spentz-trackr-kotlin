package io.github.suricatoaviones.spentztrackr.core.money

/**
 * Procedencia de una tasa, espejo del backend.
 * BCV y PARALELO vienen de dolarapi; USER es el override manual del usuario;
 * CUSTOM se acepta en requests (validación `rate_provider` del backend).
 */
enum class RateProvider(
    val value: String,
) {
    BCV("bcv"),
    PARALELO("paralelo"),
    USER("user"),
    CUSTOM("custom"),
    ;

    companion object {
        fun fromValue(value: String?): RateProvider? = entries.firstOrNull { it.value == value }
    }
}
