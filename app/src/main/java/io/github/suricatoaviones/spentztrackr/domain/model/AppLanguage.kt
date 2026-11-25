package io.github.suricatoaviones.spentztrackr.domain.model

/** Idioma de la UI. `SYSTEM` sigue el locale del teléfono. */
enum class AppLanguage(
    val value: String,
) {
    SYSTEM("system"),
    SPANISH("es"),
    ENGLISH("en"),
    ;

    companion object {
        fun fromValue(value: String?): AppLanguage = entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}
