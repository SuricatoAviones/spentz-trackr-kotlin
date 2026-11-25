package io.github.suricatoaviones.spentztrackr.domain.model

/** Apariencia de la app. El override manual llega con los Ajustes (Fase 6). */
enum class AppTheme(
    val value: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromValue(value: String?): AppTheme = entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}
