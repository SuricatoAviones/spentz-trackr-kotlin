package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.util.MoneyFormat
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType

/** Gestión de categorías (docs/06): pestañas gasto/ingreso, editar/borrar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNewCategory: (CategoryType) -> Unit,
    onEditCategory: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val blockedMessage = stringResource(R.string.catalog_delete_blocked)

    LaunchedEffect(uiState.deleteBlocked) {
        if (uiState.deleteBlocked) {
            snackbarHostState.showSnackbar(blockedMessage)
            viewModel.dismissDeleteBlocked()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.catalog_categories_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.catalog_new)) },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                onClick = { onNewCategory(uiState.type) },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            ) {
                val types = listOf(CategoryType.EXPENSE, CategoryType.INCOME)
                types.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.setType(type) },
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

            when {
                uiState.loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                uiState.error ->
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.catalog_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                else ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(uiState.items, key = { it.id }) { category ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CatalogIconBadge(iconName = category.icon, colorHex = category.color)
                                Column(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                ) {
                                    Text(text = category.name, style = MaterialTheme.typography.titleMedium)
                                    category.budget?.let { budget ->
                                        Text(
                                            text = stringResource(R.string.catalog_budget, MoneyFormat.format(budget)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = { onEditCategory(category.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(R.string.expense_detail_edit),
                                    )
                                }
                                IconButton(onClick = { viewModel.delete(category.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.expense_detail_delete),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}
