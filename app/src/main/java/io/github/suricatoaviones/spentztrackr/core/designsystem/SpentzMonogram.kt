package io.github.suricatoaviones.spentztrackr.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Monograma de la marca: "S" sobre el color primario. Se usa en el splash,
 * el onboarding y el placeholder de Inicio.
 */
@Composable
fun SpentzMonogram(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    corner: Dp = 20.dp,
    contentDescription: String? = null,
) {
    Surface(
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .size(size)
                .then(
                    if (contentDescription != null) {
                        Modifier.semantics { this.contentDescription = contentDescription }
                    } else {
                        Modifier
                    },
                ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "S",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
