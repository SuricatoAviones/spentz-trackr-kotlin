package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource

/** Diálogo de alta/edición de origen (reusado desde la lista). */
@Composable
fun SourceFormDialogHost(
    editing: PaymentSource?,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(editing?.name.orEmpty()) }
    var icon by remember { mutableStateOf(editing?.icon ?: "wallet") }
    var color by remember { mutableStateOf(editing?.color ?: "#F59E0B") }
    var showPicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (editing == null) R.string.catalog_new else R.string.catalog_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(stringResource(R.string.catalog_name)) },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CatalogIconBadge(iconName = icon, colorHex = color)
                    TextButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text(stringResource(R.string.catalog_pick_icon))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(name, icon, color)
                    }
                },
            ) { Text(stringResource(R.string.catalog_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )

    if (showPicker) {
        CatalogIconPickerDialog(
            selectedIcon = icon,
            selectedColor = color,
            onIconPick = { icon = it },
            onColorPick = { color = it },
            onDismiss = { showPicker = false },
        )
    }
}
