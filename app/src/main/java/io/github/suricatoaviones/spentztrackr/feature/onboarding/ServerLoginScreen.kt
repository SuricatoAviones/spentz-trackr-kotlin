package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.designsystem.SpentzMonogram

/**
 * Conexión al servidor: URL + credenciales, con registro opcional
 * (docs/04, rama remota). Los errores se explican solos y la URL `http://`
 * advierte que va sin cifrar (ADR-A11).
 */
@Composable
fun ServerLoginScreen(
    fromSettings: Boolean,
    onConnected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.done) {
        if (uiState.done && fromSettings) onConnected()
    }

    val fieldsValid =
        uiState.url.isNotBlank() &&
            uiState.email.isNotBlank() &&
            uiState.password.isNotBlank() &&
            (!uiState.register || (uiState.name.isNotBlank() && uiState.confirmPassword.isNotBlank()))

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (!fromSettings) {
                SpentzMonogram(size = 56.dp)
            }
            Text(
                text = stringResource(R.string.onboarding_server_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = if (fromSettings) 0.dp else 16.dp),
            )

            OutlinedTextField(
                value = uiState.url,
                onValueChange = viewModel::onUrlChange,
                label = { Text(stringResource(R.string.onboarding_server_url_label)) },
                supportingText = { Text(stringResource(R.string.onboarding_server_url_support)) },
                singleLine = true,
                isError = uiState.error is io.github.suricatoaviones.spentztrackr.data.remote.ApiError.InvalidUrl,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            )

            if (uiState.url.trim().startsWith("http://", ignoreCase = true)) {
                CleartextWarning(modifier = Modifier.padding(top = 8.dp))
            }

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.onboarding_server_email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.onboarding_server_password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_server_register_toggle),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = uiState.register, onCheckedChange = viewModel::onRegisterToggle)
            }

            AnimatedVisibility(visible = uiState.register) {
                Column {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.onboarding_server_name_label)) },
                        singleLine = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text(stringResource(R.string.onboarding_server_confirm_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = apiErrorText(error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = { viewModel.submit(fromSettings) },
                enabled = !uiState.loading && fieldsValid,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text =
                            stringResource(
                                if (uiState.register) {
                                    R.string.onboarding_server_register
                                } else {
                                    R.string.onboarding_server_connect
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CleartextWarning(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.onboarding_server_cleartext_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
