package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Snapshot de la configuración de la app (DataStore). Solo estado de sesión y
 * preferencias de UI: los datos de negocio viven en Room (docs/03).
 */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    /** Modo activo; null hasta que el usuario elige en el primer arranque. */
    val mode: AppMode? = null,
    /** URL base del servidor normalizada (sin `/api/v1`); solo modo remoto. */
    val serverUrl: String? = null,
    /** Email de la sesión remota (informativo). */
    val serverEmail: String? = null,
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
