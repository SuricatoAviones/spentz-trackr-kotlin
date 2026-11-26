package io.github.suricatoaviones.spentztrackr.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType

/**
 * Ajustes completos (docs/06, Fase 6): modo/sesión, preferencias de negocio,
 * apariencia/idioma, catálogos y Acerca de.
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
    val preferences = uiState.preferences

    var budgetText by rememberSaveable { mutableStateOf("") }
    var minCommissionText by rememberSaveable { mutableStateOf("") }
    var commissionRateText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(preferences.monthlyBudget) {
        budgetText = preferences.monthlyBudget?.toCanonicalString().orEmpty()
    }
    LaunchedEffect(preferences.minCommission) {
        minCommissionText = preferences.minCommission.toCanonicalString()
    }
    LaunchedEffect(preferences.commissionRate) {
        commissionRateText = preferences.commissionRate.toPlainString()
    }

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

            FeedbackLine(uiState.feedback)

            ModeCard(
                uiState = uiState,
                onConnectServer = onConnectServer,
                onSwitchLocal = viewModel::switchToLocal,
                onSwitchRemote = viewModel::switchToRemote,
                onLogout = viewModel::logoutRemote,
            )

            PreferencesCard(
                uiState = uiState,
                budgetText = budgetText,
                onBudgetChange = {
                    budgetText = it
                    viewModel.clearFeedback()
                },
                onSaveBudget = { viewModel.saveBudget(budgetText) },
                minCommissionText = minCommissionText,
                onMinCommissionChange = {
                    minCommissionText = it
                    viewModel.clearFeedback()
                },
                commissionRateText = commissionRateText,
                onCommissionRateChange = {
                    commissionRateText = it
                    viewModel.clearFeedback()
                },
                onSaveCommission = { viewModel.saveCommission(minCommissionText, commissionRateText) },
                onDisplayCurrency = viewModel::setDisplayCurrency,
                onTrackingType = viewModel::setTrackingType,
            )

            AppearanceCard(
                theme = preferences.theme,
                language = preferences.language,
                onTheme = viewModel::setTheme,
                onLanguage = viewModel::setLanguage,
            )

            Text(
                text = stringResource(R.string.settings_catalogs_title),
                style = MaterialTheme.typography.titleMedium,
            )
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
            OutlinedButton(
                onClick = onOpenRates,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_open_rates))
            }

            AboutCard()
        }
    }
}

@Composable
private fun FeedbackLine(feedback: SettingsFeedback?) {
    if (feedback == null) return
    val text =
        when (feedback) {
            SettingsFeedback.SAVED -> stringResource(R.string.settings_saved)
            SettingsFeedback.INVALID_BUDGET -> stringResource(R.string.settings_invalid_budget)
            SettingsFeedback.INVALID_COMMISSION -> stringResource(R.string.settings_invalid_commission)
            SettingsFeedback.SAVE_ERROR -> stringResource(R.string.settings_save_error)
        }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color =
            when (feedback) {
                SettingsFeedback.SAVED -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.error
            },
    )
}

@Composable
private fun ModeCard(
    uiState: SettingsUiState,
    onConnectServer: () -> Unit,
    onSwitchLocal: () -> Unit,
    onSwitchRemote: () -> Unit,
    onLogout: () -> Unit,
) {
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
                            onClick = onSwitchRemote,
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
                            onClick = onSwitchRemote,
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
                    Text(
                        text = stringResource(R.string.settings_remote_sync_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    OutlinedButton(
                        onClick = onSwitchLocal,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                    ) {
                        Text(stringResource(R.string.settings_switch_local))
                    }
                    TextButton(
                        onClick = onLogout,
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
}

@Composable
private fun PreferencesCard(
    uiState: SettingsUiState,
    budgetText: String,
    onBudgetChange: (String) -> Unit,
    onSaveBudget: () -> Unit,
    minCommissionText: String,
    onMinCommissionChange: (String) -> Unit,
    commissionRateText: String,
    onCommissionRateChange: (String) -> Unit,
    onSaveCommission: () -> Unit,
    onDisplayCurrency: (DisplayCurrency) -> Unit,
    onTrackingType: (TrackingType) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_preferences_title),
                style = MaterialTheme.typography.titleMedium,
            )

            ChoiceRow(
                label = stringResource(R.string.settings_display_currency),
                options =
                    listOf(
                        DisplayCurrency.USD to stringResource(R.string.settings_currency_usd),
                        DisplayCurrency.USDT to stringResource(R.string.settings_currency_usdt),
                    ),
                selected = uiState.preferences.displayCurrency,
                onSelect = onDisplayCurrency,
            )

            ChoiceRow(
                label = stringResource(R.string.settings_tracking),
                options =
                    listOf(
                        TrackingType.EXPENSES to stringResource(R.string.onboarding_local_tracking_expenses),
                        TrackingType.INCOME to stringResource(R.string.onboarding_local_tracking_income),
                        TrackingType.BOTH to stringResource(R.string.onboarding_local_tracking_both),
                    ),
                selected = uiState.preferences.trackingType,
                onSelect = onTrackingType,
            )

            OutlinedTextField(
                value = budgetText,
                onValueChange = onBudgetChange,
                label = { Text(stringResource(R.string.settings_budget)) },
                supportingText = { Text(stringResource(R.string.settings_budget_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onSaveBudget,
                enabled = !uiState.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_save))
            }

            Text(
                text = stringResource(R.string.settings_commission_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedTextField(
                value = minCommissionText,
                onValueChange = onMinCommissionChange,
                label = { Text(stringResource(R.string.settings_commission_min)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = commissionRateText,
                onValueChange = onCommissionRateChange,
                label = { Text(stringResource(R.string.settings_commission_rate)) },
                supportingText = { Text(stringResource(R.string.settings_commission_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onSaveCommission,
                enabled = !uiState.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_save))
            }
        }
    }
}

@Composable
private fun AppearanceCard(
    theme: AppTheme,
    language: AppLanguage,
    onTheme: (AppTheme) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_appearance_title),
                style = MaterialTheme.typography.titleMedium,
            )
            ChoiceRow(
                label = stringResource(R.string.settings_theme),
                options =
                    listOf(
                        AppTheme.SYSTEM to stringResource(R.string.settings_theme_system),
                        AppTheme.LIGHT to stringResource(R.string.settings_theme_light),
                        AppTheme.DARK to stringResource(R.string.settings_theme_dark),
                    ),
                selected = theme,
                onSelect = onTheme,
            )
            ChoiceRow(
                label = stringResource(R.string.settings_language),
                options =
                    listOf(
                        AppLanguage.SYSTEM to stringResource(R.string.settings_language_system),
                        AppLanguage.SPANISH to stringResource(R.string.settings_language_spanish),
                        AppLanguage.ENGLISH to stringResource(R.string.settings_language_english),
                    ),
                selected = language,
                onSelect = onLanguage,
            )
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val version =
        remember(context) {
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull().orEmpty()
        }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_about_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_about_version, version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.settings_about_license),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.settings_about_repo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (value, text) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(text) },
                )
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
