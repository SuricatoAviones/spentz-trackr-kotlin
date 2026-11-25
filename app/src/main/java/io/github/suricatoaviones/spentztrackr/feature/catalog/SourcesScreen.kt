package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

/** Gestión de orígenes de pago (docs/06): lista + CRUD en diálogo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = hiltViewModel(),
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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.catalog_sources_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.catalog_new)) },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                onClick = viewModel::openNewDialog,
            )
        },
    ) { padding ->
        when {
            uiState.loading ->
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            uiState.error ->
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.catalog_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            else ->
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.items, key = { it.id }) { source ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CatalogIconBadge(iconName = source.icon, colorHex = source.color)
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                            )
                            IconButton(onClick = { viewModel.openEditDialog(source.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.expense_detail_edit),
                                )
                            }
                            IconButton(onClick = { viewModel.delete(source.id) }) {
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

    // Diálogo de alta/edición: `dialogEditingId == null` con `dialogOpen`
    // implícito en el estado (null ⇒ cerrado, -1 ⇒ alta, ≥0 ⇒ edición).
    val dialogKey = uiState.dialogEditingId
    if (dialogKey != null) {
        SourceFormDialogHost(
            editing = if (dialogKey >= 0) uiState.items.firstOrNull { it.id == dialogKey } else null,
            onSave = { name, icon, color ->
                viewModel.save(dialogKey.takeIf { it >= 0 }, name, icon, color)
            },
            onDismiss = viewModel::closeDialog,
        )
    }
}
