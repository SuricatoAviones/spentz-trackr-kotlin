package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.util.DateText
import io.github.suricatoaviones.spentztrackr.core.util.MoneyFormat

/**
 * Detalle del gasto (docs/06): todos los campos, la tasa congelada visible
 * ("Tasa aplicada: 28.0000 Bs/USD (BCV)") y eliminar con confirmación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) { viewModel.load(expenseId) }
    LaunchedEffect(uiState.deleted) { if (uiState.deleted) onBack() }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.expense_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    uiState.expense?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.expense_detail_edit),
                            )
                        }
                    }
                },
            )

            when {
                uiState.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                uiState.notFound || uiState.expense == null ->
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.expense_detail_not_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                else -> DetailContent(uiState, onConfirmDelete = { confirmDelete = true })
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.expense_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.expense_detail_delete_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    },
                ) { Text(stringResource(R.string.expense_detail_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun DetailContent(
    uiState: ExpenseDetailUiState,
    onConfirmDelete: () -> Unit,
) {
    val expense = uiState.expense ?: return

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = DateText.formatLong(expense.spentAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AmountRow(
                    label = stringResource(R.string.expense_detail_amount),
                    value = MoneyFormat.withCurrency(expense.amount, expense.currency),
                    emphasized = true,
                )
                expense.commission?.let { commission ->
                    AmountRow(
                        label = stringResource(R.string.expense_detail_commission),
                        value = MoneyFormat.withCurrency(commission, CurrencyCode.VES),
                    )
                }
                expense.exchangeRate?.let { rate ->
                    AmountRow(
                        label =
                            stringResource(
                                R.string.expense_detail_rate,
                                rate.toCanonicalString(),
                                badgeName(expense.rateProvider?.value),
                            ),
                        value = "",
                    )
                }
                AmountRow(
                    label = stringResource(R.string.expense_detail_frozen),
                    value =
                        stringResource(
                            R.string.expense_form_preview_line,
                            expense.usdAmount.toCanonicalString(),
                            expense.usdtAmount.toCanonicalString(),
                        ),
                    emphasized = true,
                )
            }
        }

        val meta =
            listOfNotNull(
                uiState.categoryName?.let { stringResource(R.string.expense_form_category) + ": " + it },
                uiState.sourceName?.let { stringResource(R.string.expense_form_source) + ": " + it },
                expense.note?.let { stringResource(R.string.expense_form_note_detail) + ": " + it },
            )
        meta.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (uiState.deleteError) {
            Text(
                text = stringResource(R.string.expense_detail_delete_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        OutlinedButton(
            onClick = onConfirmDelete,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.expense_detail_delete),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasized) FontWeight.Bold else null,
            )
        }
    }
}

@Composable
private fun badgeName(providerValue: String?): String =
    when (providerValue) {
        "bcv" -> stringResource(R.string.expense_badge_bcv)
        "paralelo" -> stringResource(R.string.expense_badge_paralelo)
        "user", "custom" -> stringResource(R.string.expense_badge_manual)
        else -> ""
    }
