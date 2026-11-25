package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.data.remote.ApiError
import kotlinx.serialization.SerializationException

/**
 * Traduce [ApiError] a texto visible según docs/04 ("estados de sesión").
 * Los mensajes de validación (422) se muestran tal cual los envía el backend:
 * son su fuente de verdad.
 */
@Composable
fun apiErrorText(error: ApiError): String =
    when (error) {
        ApiError.InvalidUrl -> stringResource(R.string.onboarding_error_invalid_url)
        ApiError.Network -> stringResource(R.string.onboarding_error_network)
        is ApiError.Validation ->
            error.fieldErrors.values
                .flatten()
                .firstOrNull()
                ?: stringResource(R.string.onboarding_error_credentials)
        is ApiError.Suspended -> error.message.ifBlank { stringResource(R.string.onboarding_error_suspended) }
        ApiError.Throttled -> stringResource(R.string.onboarding_error_throttled)
        is ApiError.Http ->
            when (error.code) {
                404 -> stringResource(R.string.onboarding_error_not_spentz)
                else -> error.message ?: stringResource(R.string.onboarding_error_generic)
            }
        is ApiError.Unexpected ->
            if (error.cause is SerializationException) {
                // El servidor respondió algo que no es JSON de spentz-trackr.
                stringResource(R.string.onboarding_error_not_spentz)
            } else {
                stringResource(R.string.onboarding_error_generic)
            }
    }
