package io.github.suricatoaviones.spentztrackr.data.remote

/**
 * Errores de la capa remota, mapeados desde exceptions (ver
 * [ApiErrorMapper]). La UI los traduce a mensajes con strings.
 */
sealed interface ApiError {
    /** La URL escrita no es válida (vacía o malformada). */
    data object InvalidUrl : ApiError

    /** Sin red / timeout / servidor inalcanzable (IOException). */
    data object Network : ApiError

    /** 422 de Laravel: errores de validación por campo (login: credenciales). */
    data class Validation(
        val fieldErrors: Map<String, List<String>>,
    ) : ApiError

    /** 403: la cuenta está suspendida en el servidor. */
    data class Suspended(
        val message: String,
    ) : ApiError

    /** 429: throttling de la API (auth: 5/min). */
    data object Throttled : ApiError

    /** Cualquier otro código HTTP (404/5xx/…): el mensaje llega si se pudo leer. */
    data class Http(
        val code: Int,
        val message: String?,
    ) : ApiError

    /** Error inesperado (JSON inválido, etc.). */
    data class Unexpected(
        val cause: Throwable,
    ) : ApiError
}
