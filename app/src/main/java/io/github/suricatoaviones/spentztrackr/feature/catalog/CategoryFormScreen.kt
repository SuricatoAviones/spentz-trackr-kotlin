package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType

/** Crear/editar categoría: nombre, tipo, icono/color y presupuesto (gasto). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormScreen(
    editingId: Long?,
    defaultType: CategoryType,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(editingId, defaultType) { viewModel.load(editingId, defaultType) }
    LaunchedEffect(uiState.saved) { if (uiState.saved) onSaved() }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.editingId != null) R.string.catalog_edit else R.string.catalog_new,
                        ),
                    )
                },
            )

            if (uiState.loading) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
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
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text(stringResource(R.string.catalog_name)) },
                    supportingText = {
                        if (uiState.nameError) Text(stringResource(R.string.catalog_name_required))
                    },
                    isError = uiState.nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val types = listOf(CategoryType.EXPENSE, CategoryType.INCOME)
                    types.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = uiState.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                        ) {
                            Text(
                                stringResource(
                                    if (type == CategoryType.EXPENSE) R.string.catalog_type_expense else R.string.catalog_type_income,
                                ),
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CatalogIconBadge(iconName = uiState.icon, colorHex = uiState.color)
                    Text(
                        text = stringResource(R.string.catalog_pick_icon),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                if (uiState.type == CategoryType.EXPENSE) {
                    OutlinedTextField(
                        value = uiState.budgetText,
                        onValueChange = viewModel::onBudgetChange,
                        label = { Text(stringResource(R.string.catalog_budget_label)) },
                        supportingText = {
                            if (uiState.budgetError) {
                                Text(stringResource(R.string.onboarding_local_budget_invalid))
                            }
                        },
                        isError = uiState.budgetError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.saving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(2.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                    } else {
                        Text(stringResource(R.string.catalog_save))
                    }
                }

                if (uiState.saveError) {
                    Text(
                        text = stringResource(R.string.catalog_save_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showPicker) {
        CatalogIconPickerDialog(
            selectedIcon = uiState.icon,
            selectedColor = uiState.color,
            onIconPick = viewModel::onIconChange,
            onColorPick = viewModel::onColorChange,
            onDismiss = { showPicker = false },
        )
    }
}
