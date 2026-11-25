package io.github.suricatoaviones.spentztrackr.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode

/**
 * Ajustes (stub de la Fase 1): estado del modo, sesión remota (auth/me) y
 * cambio de modo. Las preferencias completas llegan con la Fase 6.
 */
@Composable
fun SettingsScreen(
    onConnectServer: () -> Unit,
    onOpenCatalogs: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenRates: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshRemoteSession()
        viewModel.needsLogin.collect { onConnectServer() }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (uiState.mode) {
                        AppMode.LOCAL, null -> {
                            Text(
                                text = stringResource(R.string.settings_mode_local),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.settings_mode_switch_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            if (uiState.serverUrl != null) {
                                OutlinedButton(
                                    onClick = viewModel::switchToRemote,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                ) {
                                    Text(
                                        text =
                                            stringResource(
                                                R.string.settings_switch_remote_saved,
                                                uiState.serverUrl.orEmpty(),
                                            ),
                                    )
                                }
                                TextButton(
                                    onClick = onConnectServer,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.settings_switch_remote))
                                }
                            } else {
                                Button(
                                    onClick = viewModel::switchToRemote,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                ) {
                                    Text(stringResource(R.string.settings_switch_remote))
                                }
                            }
                        }

                        AppMode.REMOTE -> {
                            Text(
                                text =
                                    stringResource(
                                        R.string.settings_mode_remote,
                                        uiState.serverUrl.orEmpty(),
                                    ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            SessionLine(uiState)
                            OutlinedButton(
                                onClick = viewModel::switchToLocal,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                            ) {
                                Text(stringResource(R.string.settings_switch_local))
                            }
                            TextButton(
                                onClick = viewModel::logoutRemote,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_logout_remote),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Catálogos (Fase 4)
            OutlinedButton(
                onClick = onOpenCatalogs,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_open_categories))
            }
            OutlinedButton(
                onClick = onOpenSources,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_open_sources))
            }
            // Tasas (Fase 5)
            OutlinedButton(
                onClick = onOpenRates,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_open_rates))
            }
        }
    }
}

@Composable
private fun SessionLine(uiState: SettingsUiState) {
    when {
        uiState.checkingSession ->
            Text(
                text = stringResource(R.string.settings_mode_remote_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        else -> {
            val user = uiState.remoteUser
            val sessionText =
                if (user != null) {
                    stringResource(R.string.settings_mode_remote_user, "${user.name} (${user.email})")
                } else {
                    uiState.serverEmail.orEmpty()
                }
            Text(
                text = sessionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
