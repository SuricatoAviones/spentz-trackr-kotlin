package io.github.suricatoaviones.spentztrackr.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs de autenticación — shapes verificados contra
 * `Api\V1\Auth\AuthenticatedSessionController` del backend (docs/08, deudas
 * de verificación: resueltas para login/register/me).
 */

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String? = null,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
    @SerialName("device_name") val deviceName: String? = null,
)

/** `data` de login/register: token Sanctum (expira a 90 días) + usuario. */
@Serializable
data class AuthTokenData(
    val token: String,
    @SerialName("token_id") val tokenId: Long? = null,
    val user: UserDto,
)

/** `userShape()` del backend. */
@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    val locale: String? = null,
    @SerialName("tracking_type") val trackingType: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * `PUT /user/profile` — `ProfileController::update`. `null` = no enviar
 * (el servidor usa `sometimes`); con `explicitNulls = false` los nulos se
 * omiten del JSON.
 */
@Serializable
data class ProfileRequestDto(
    val name: String? = null,
    val email: String? = null,
    @SerialName("default_display_currency") val defaultDisplayCurrency: String? = null,
    @SerialName("tracking_type") val trackingType: String? = null,
)
