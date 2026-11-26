package io.github.suricatoaviones.spentztrackr.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode

/**
 * Paso 2: radio-cards con la elección de modo (docs/06). La copia es honesta
 * sobre qué implica cada uno; cambiar de modo después no es una cárcel.
 */
@Composable
fun ModeSelectionScreen(
    onChoose: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<AppMode?>(null) }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.onboarding_mode_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            ModeCard(
                selected = selected == AppMode.LOCAL,
                title = stringResource(R.string.onboarding_mode_local_title),
                description = stringResource(R.string.onboarding_mode_local_desc),
                onClick = { selected = AppMode.LOCAL },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            )
            ModeCard(
                selected = selected == AppMode.REMOTE,
                title = stringResource(R.string.onboarding_mode_remote_title),
                description = stringResource(R.string.onboarding_mode_remote_desc),
                onClick = { selected = AppMode.REMOTE },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
            )
            Button(
                onClick = { selected?.let(onChoose) },
                enabled = selected != null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
            ) {
                Text(stringResource(R.string.onboarding_mode_continue))
            }
        }
    }
}

@Composable
private fun ModeCard(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
