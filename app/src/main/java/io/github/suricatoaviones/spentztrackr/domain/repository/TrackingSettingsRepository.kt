package io.github.suricatoaviones.spentztrackr.domain.repository

/**
 * Qué módulos registra el usuario — espejo de `users.tracking_type` del
 * backend (expenses | income | both). Gobierna qué pestañas se muestran
 * (middleware `EnsureTrackingFeature` del backend).
 */
enum class TrackingMode(
    val value: String,
) {
    EXPENSES_ONLY("expenses"),
    INCOME_ONLY("income"),
    BOTH("both"),
    ;

    companion object {
        /** Nunca falla: valor desconocido → BOTH (como el backend). */
        fun fromValue(value: String?): TrackingMode = entries.firstOrNull { it.value == value } ?: BOTH
    }
}

interface TrackingSettingsRepository {
    suspend fun trackingMode(): TrackingMode
}
