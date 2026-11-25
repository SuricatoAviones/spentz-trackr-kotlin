package io.github.suricatoaviones.spentztrackr.feature.incomes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.util.DateText
import io.github.suricatoaviones.spentztrackr.feature.expenses.ExpenseFormTags
import io.github.suricatoaviones.spentztrackr.feature.expenses.badgeText
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** Formulario de ingreso: moneda, monto, tasa (Bs), categoría, fecha, texto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeFormScreen(
    editingId: Long?,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomeFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(editingId) { viewModel.load(editingId) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onSaved() }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isEditing) R.string.income_form_edit else R.string.income_form_new,
                        ),
                    )
                },
            )

            if (uiState.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text(stringResource(R.string.income_form_amount)) },
                    supportingText = {
                        if (uiState.amountError) {
                            Text(stringResource(R.string.expense_form_amount_invalid))
                        }
                    },
                    isError = uiState.amountError,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(ExpenseFormTags.AMOUNT),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CurrencyCode.entries.forEachIndexed { index, currency ->
                        SegmentedButton(
                            selected = uiState.currency == currency,
                            onClick = { viewModel.onCurrencyChange(currency) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CurrencyCode.entries.size),
                        ) {
                            Text(currency.value.uppercase())
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.currency == CurrencyCode.VES) {
                    OutlinedTextField(
                        value = uiState.rateText,
                        onValueChange = viewModel::onRateChange,
                        label = { Text(stringResource(R.string.expense_form_rate)) },
                        supportingText = {
                            when {
                                uiState.rateError -> Text(stringResource(R.string.expense_form_rate_invalid))
                                uiState.rateBadge == null && uiState.rateText.isBlank() ->
                                    Text(stringResource(R.string.expense_form_rate_missing))
                                else -> Text(badgeText(uiState.rateBadge))
                            }
                        },
                        trailingIcon = {
                            uiState.rateBadge?.let { badge ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                ) {
                                    Text(
                                        text = badgeText(badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        },
                        isError = uiState.rateError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .testTag(ExpenseFormTags.RATE),
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.expense_form_preview),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val preview = uiState.previewUsd
                        Text(
                            text =
                                if (preview != null) {
                                    stringResource(R.string.expense_form_preview_line, preview, preview)
                                } else {
                                    stringResource(R.string.expense_form_preview_pending)
                                },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.income_form_category),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        if (uiState.categoryError) {
                            Text(
                                text = " — " + stringResource(R.string.expense_form_category_required),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Row(
                        modifier =
                            Modifier
                                .padding(top = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                    ) {
                        uiState.categories.forEachIndexed { index, category ->
                            FilterChip(
                                selected = uiState.categoryId == category.id,
                                onClick = { viewModel.onCategoryChange(category.id) },
                                label = { Text(category.name) },
                                modifier = if (index > 0) Modifier.padding(start = 8.dp) else Modifier,
                            )
                        }
                    }
                }

                DateField(uiState, viewModel)

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text(stringResource(R.string.expense_form_description)) },
                    supportingText = {
                        if (uiState.descriptionError) {
                            Text(stringResource(R.string.expense_form_description_required))
                        }
                    },
                    isError = uiState.descriptionError,
                    singleLine = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(ExpenseFormTags.DESCRIPTION),
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text(stringResource(R.string.expense_form_note)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.saving,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(ExpenseFormTags.SAVE),
                ) {
                    if (uiState.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(2.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                    } else {
                        Text(
                            text =
                                stringResource(
                                    if (uiState.isEditing) R.string.expense_form_update else R.string.income_form_save,
                                ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (uiState.saveError) {
                    Text(
                        text = stringResource(R.string.income_form_save_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    uiState: IncomeFormUiState,
    viewModel: IncomeFormViewModel,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = uiState.date?.let { DateText.formatLong(it) }.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.expense_form_date)) },
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = stringResource(R.string.expense_form_date),
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    if (showPicker && uiState.date != null) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = uiState.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateChange(
                                Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date,
                            )
                        }
                        showPicker = false
                    },
                ) { Text(stringResource(R.string.action_ok)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
