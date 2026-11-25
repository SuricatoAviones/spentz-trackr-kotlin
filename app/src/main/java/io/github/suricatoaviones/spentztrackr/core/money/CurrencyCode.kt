package io.github.suricatoaviones.spentztrackr.core.money

/**
 * Monedas soportadas, espejo del enum `Currency` del backend.
 * USDT se trata 1:1 con USD (ADR-001 backend).
 */
enum class CurrencyCode(
    val value: String,
) {
    USD("usd"),
    VES("ves"),
    USDT("usdt"),
    ;

    companion object {
        fun fromValue(value: String?): CurrencyCode? = entries.firstOrNull { it.value == value }
    }
}
