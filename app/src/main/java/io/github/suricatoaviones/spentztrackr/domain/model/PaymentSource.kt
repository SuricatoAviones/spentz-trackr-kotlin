package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Origen de pago — etiqueta sin estado (ADR-012 backend: las tarjetas de
 * crédito, con saldo propio, llegan en fases posteriores).
 */
data class PaymentSource(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val isSystem: Boolean,
)

data class PaymentSourceDraft(
    val name: String,
    val icon: String,
    val color: String,
)
