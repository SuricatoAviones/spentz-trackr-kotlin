package io.github.suricatoaviones.spentztrackr.domain.repository

/**
 * Categoría u origen con movimientos asociados: el backend lo bloquea con
 * FK restrict (422/409 con mensaje) y el Room local con RESTRICT.
 */
class EntityInUseException(
    message: String? = null,
) : IllegalStateException(message ?: "No se puede eliminar: tiene movimientos asociados")
