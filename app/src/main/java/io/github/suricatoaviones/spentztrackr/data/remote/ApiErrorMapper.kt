package io.github.suricatoaviones.spentztrackr.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Traduce exceptions de red a [ApiError] (docs/04, "estados de sesión").
 * Shapes de error verificados contra el backend: Laravel 422
 * `{message, errors}` y `apiError()` `{success:false, message}`.
 */
@Singleton
class ApiErrorMapper
    @Inject
    constructor(
        private val json: Json,
    ) {
        fun map(throwable: Throwable): ApiError =
            when (throwable) {
                is IOException -> ApiError.Network
                is HttpException -> mapHttp(throwable)
                is SerializationException -> ApiError.Unexpected(throwable)
                else -> ApiError.Unexpected(throwable)
            }

        private fun mapHttp(error: HttpException): ApiError =
            when (error.code()) {
                422 -> parseValidation(error) ?: ApiError.Http(422, null)
                403 -> ApiError.Suspended(parseMessage(error).orEmpty())
                429 -> ApiError.Throttled
                else -> ApiError.Http(error.code(), parseMessage(error))
            }

        private fun parseValidation(error: HttpException): ApiError.Validation? {
            val body = error.response()?.errorBody()?.string() ?: return null
            val parsed = runCatching { json.decodeFromString(ErrorEnvelopeDto.serializer(), body) }
            return parsed.getOrNull()?.let { dto ->
                if (dto.errors.isNullOrEmpty()) null else ApiError.Validation(dto.errors)
            }
        }

        private fun parseMessage(error: HttpException): String? {
            val body = error.response()?.errorBody()?.string() ?: return null
            return runCatching { json.decodeFromString(ErrorEnvelopeDto.serializer(), body) }
                .getOrNull()
                ?.message
        }

        /** `{message, errors?}` — forma de error de Laravel. */
        @Serializable
        private data class ErrorEnvelopeDto(
            val message: String? = null,
            val errors: Map<String, List<String>>? = null,
        )
    }
