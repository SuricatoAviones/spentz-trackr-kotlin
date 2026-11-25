package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.data.remote.dto.ApiEnvelope
import io.github.suricatoaviones.spentztrackr.data.remote.dto.AuthTokenData
import io.github.suricatoaviones.spentztrackr.data.remote.dto.LoginRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.RegisterRequest
import io.github.suricatoaviones.spentztrackr.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Cliente de la API `/api/v1` del backend. La URL base es un placeholder:
 * [DynamicBaseUrlInterceptor] la reescribe con la que el usuario guardó en
 * settings (docs/05, "reglas del cliente HTTP").
 */
interface SpentzApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): ApiEnvelope<AuthTokenData>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): ApiEnvelope<AuthTokenData>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<UserDto>

    /** `data` viene null: basta con el envelope. */
    @DELETE("auth/logout")
    suspend fun logout(): ApiEnvelope<String?>
}
