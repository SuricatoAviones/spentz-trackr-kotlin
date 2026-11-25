package io.github.suricatoaviones.spentztrackr.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Envelope de todas las respuestas del backend
 * (`BaseApiController::apiResponse`): `{success, data, message?}`.
 * `data` puede ser null (p. ej. logout); los errores NO usan este envelope
 * (ver [io.github.suricatoaviones.spentztrackr.data.remote.ApiErrorMapper]).
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
)
