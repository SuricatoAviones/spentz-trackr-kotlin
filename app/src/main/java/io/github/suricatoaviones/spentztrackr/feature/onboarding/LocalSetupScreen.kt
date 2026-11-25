package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType

/**
 * Setup rápido del modo local (docs/04): moneda de visualización, tipo de
 * seguimiento y presupuesto opcional. Todo se puede cambiar después.
 */
@Composable
fun LocalSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_local_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.onboarding_local_currency_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DisplayCurrency.entries.forEachIndexed { index, currency ->
                    SegmentedButton(
                        selected = uiState.displayCurrency == currency,
                        onClick = { viewModel.onCurrencyChange(currency) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = DisplayCurrency.entries.size),
                    ) {
                        Text(currency.value.uppercase())
                    }
                }
            }

            Text(
                text = stringResource(R.string.onboarding_local_tracking_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            val trackingOptions =
                listOf(
                    TrackingType.EXPENSES to R.string.onboarding_local_tracking_expenses,
                    TrackingType.INCOME to R.string.onboarding_local_tracking_income,
                    TrackingType.BOTH to R.string.onboarding_local_tracking_both,
                )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                trackingOptions.forEachIndexed { index, (tracking, label) ->
                    SegmentedButton(
                        selected = uiState.trackingType == tracking,
                        onClick = { viewModel.onTrackingChange(tracking) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = trackingOptions.size),
                    ) {
                        Text(stringResource(label))
                    }
                }
            }

            OutlinedTextField(
                value = uiState.budgetText,
                onValueChange = viewModel::onBudgetChange,
                label = { Text(stringResource(R.string.onboarding_local_budget_label)) },
                supportingText = {
                    Text(
                        stringResource(
                            if (uiState.budgetError) {
                                R.string.onboarding_local_budget_invalid
                            } else {
                                R.string.onboarding_local_budget_support
                            },
                        ),
                    )
                },
                isError = uiState.budgetError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            )

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.loading && !uiState.budgetError,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.onboarding_local_start))
                }
            }
        }
    }
}
