package io.github.suricatoaviones.spentztrackr.feature.rates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R

/**
 * Tasas (Ajustes → Tasas, docs/06): tarjetas BCV/Paralelo del día, override
 * manual ("Usar mi tasa de hoy"), histórico de 30 días y "Actualizar ahora".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RatesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val syncFailedMessage = stringResource(R.string.rates_sync_failed)
    val manualSavedMessage = stringResource(R.string.rates_manual_saved)

    LaunchedEffect(uiState.syncFailed) {
        if (uiState.syncFailed) {
            snackbarHostState.showSnackbar(syncFailedMessage)
            viewModel.dismissSyncFailed()
        }
    }
    LaunchedEffect(uiState.manualSaved) {
        if (uiState.manualSaved) {
            snackbarHostState.showSnackbar(manualSavedMessage)
            viewModel.consumeManualSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rates_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.loading ->
                Box(
                    modifier =
                        Modifier
                            .padding(padding)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            uiState.error ->
                Box(
                    modifier =
                        Modifier
                            .padding(padding)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.rates_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

            else ->
                RatesContent(
                    uiState = uiState,
                    onManualTextChange = viewModel::onManualTextChange,
                    onSaveManual = viewModel::saveManualRate,
                    onSyncNow = viewModel::syncNow,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun RatesContent(
    uiState: RatesUiState,
    onManualTextChange: (String) -> Unit,
    onSaveManual: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedLabel = updatedLabel(uiState.updatedAt)
    val manualRate = uiState.manual

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RateCard(
                    title = stringResource(R.string.rates_bcv),
                    value = uiState.bcv,
                    updatedLabel = updatedLabel,
                    modifier = Modifier.weight(1f),
                )
                RateCard(
                    title = stringResource(R.string.rates_paralelo),
                    value = uiState.paralelo,
                    updatedLabel = updatedLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (uiState.estimated) {
            item {
                Text(
                    text = stringResource(R.string.rates_estimated_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (manualRate != null) {
            item {
                Text(
                    text = stringResource(R.string.rates_manual_active, manualRate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rates_manual_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.rates_manual_support),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = uiState.manualText,
                        onValueChange = onManualTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.manualError,
                        label = { Text(stringResource(R.string.rates_manual_label)) },
                        supportingText =
                            if (uiState.manualError) {
                                { Text(stringResource(R.string.rates_manual_invalid)) }
                            } else {
                                null
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Button(
                        onClick = onSaveManual,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.rates_manual_save))
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSyncNow,
                enabled = !uiState.syncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text =
                        stringResource(
                            if (uiState.syncing) R.string.rates_syncing else R.string.rates_sync_now,
                        ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.rates_history_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (uiState.history.isEmpty()) {
            item {
                Text(
                    text =
                        stringResource(
                            if (uiState.localMode) {
                                R.string.rates_history_empty
                            } else {
                                R.string.rates_history_remote_unavailable
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(uiState.history) { row ->
                HistoryRow(row)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun RateCard(
    title: String,
    value: String?,
    updatedLabel: String?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value ?: "—",
                style = MaterialTheme.typography.headlineSmall,
            )
            if (updatedLabel != null) {
                Text(
                    text = updatedLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(row: RateHistoryRow) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = row.date.toString(),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text =
                listOfNotNull(
                    row.bcv?.let { "${stringResource(R.string.rates_bcv)} $it" },
                    row.paralelo?.let { "${stringResource(R.string.rates_paralelo)} $it" },
                    row.manual?.let { "${stringResource(R.string.rates_manual_badge)} $it" },
                ).joinToString("  ·  "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun updatedLabel(updatedAt: Long?): String? {
    if (updatedAt == null) return null
    val minutes = ((System.currentTimeMillis() - updatedAt) / 60_000L).coerceAtLeast(0L)
    return when {
        minutes < 1L -> stringResource(R.string.rates_updated_now)
        minutes < 60L -> stringResource(R.string.rates_updated_minutes, minutes)
        else -> stringResource(R.string.rates_updated_hours, minutes / 60L)
    }
}
