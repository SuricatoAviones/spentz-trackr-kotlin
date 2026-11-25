package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Modo de uso de la app (D-01/ADR-A01). Se elige en el primer arranque y se
 * puede cambiar en Ajustes; los datos de cada modo son independientes.
 */
enum class AppMode(
    val value: String,
) {
    /** Todo local: Room/SQLite, sin backend. */
    LOCAL("local"),

    /** Conectado a una instancia del backend vía API `/api/v1` (token Sanctum). */
    REMOTE("remote"),
    ;

    companion object {
        fun fromValue(value: String?): AppMode? = entries.firstOrNull { it.value == value }
    }
}
