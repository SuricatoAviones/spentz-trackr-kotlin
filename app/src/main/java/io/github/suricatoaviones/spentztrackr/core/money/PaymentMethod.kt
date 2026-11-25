package io.github.suricatoaviones.spentztrackr.core.money

/**
 * Método de pago en Bs — espejo del enum `PaymentMethod` del backend.
 * Solo aplica a transacciones en Bs; lleva la regla de comisión (ADR-006).
 */
enum class PaymentMethod(
    val value: String,
) {
    PAGO_MOVIL("pago_movil"),
    TRANSFERENCIA("transferencia"),
    ;

    companion object {
        fun fromValue(value: String?): PaymentMethod? = entries.firstOrNull { it.value == value }
    }
}
