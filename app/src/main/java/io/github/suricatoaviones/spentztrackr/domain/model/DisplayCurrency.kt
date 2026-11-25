package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Moneda de visualización principal del dashboard (espejo de
 * `users.default_display_currency` del backend: usd | usdt).
 */
enum class DisplayCurrency(
    val value: String,
) {
    USD("usd"),
    USDT("usdt"),
}
