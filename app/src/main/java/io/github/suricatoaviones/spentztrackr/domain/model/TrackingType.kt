package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Qué módulos registra el usuario (espejo de `users.tracking_type` del
 * backend: expenses | income | both). Gobierna qué pestañas se muestran.
 */
enum class TrackingType(
    val value: String,
) {
    EXPENSES("expenses"),
    INCOME("income"),
    BOTH("both"),
    ;

    companion object {
        /** Nunca falla: valor desconocido → BOTH (como el backend resuelve al default). */
        fun fromValue(value: String?): TrackingType = entries.firstOrNull { it.value == value } ?: BOTH
    }
}
