package io.github.suricatoaviones.spentztrackr.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.suricatoaviones.spentztrackr.R

/**
 * Set fijo de iconos y paleta para categorías y orígenes (docs/06).
 * Los nombres se guardan como string (mismo vocabulario que el backend) y
 * se resuelven a Material Icons aquí; desconocido ⇒ etiqueta genérica.
 */
object CatalogIcons {
    val palette: List<String> =
        listOf(
            "#10B981",
            "#3B82F6",
            "#F59E0B",
            "#EF4444",
            "#8B5CF6",
            "#EC4899",
            "#06B6D4",
            "#6B7280",
            "#14B8A6",
            "#84CC16",
            "#F97316",
            "#6366F1",
        )

    val iconNames: List<String> =
        listOf(
            "shopping-cart",
            "car",
            "zap",
            "heart-pulse",
            "gamepad-2",
            "shirt",
            "graduation-cap",
            "tag",
            "banknote",
            "wallet",
            "smartphone",
            "landmark",
            "bitcoin",
            "home",
            "plane",
            "music",
            "movie",
            "book",
            "dumbbell",
            "coffee",
            "wifi",
            "phone",
            "gift",
            "pets",
            "person",
        )

    fun vectorFor(name: String): ImageVector =
        when (name) {
            "shopping-cart" -> Icons.Filled.ShoppingCart
            "car" -> Icons.Filled.DirectionsCar
            "zap" -> Icons.Filled.Bolt
            "heart-pulse" -> Icons.Filled.Favorite
            "gamepad-2" -> Icons.Filled.SportsEsports
            "shirt" -> Icons.Filled.Checkroom
            "graduation-cap" -> Icons.Filled.School
            "tag" -> Icons.Filled.Label
            "banknote" -> Icons.Filled.Payments
            "wallet" -> Icons.Filled.AccountBalanceWallet
            "smartphone" -> Icons.Filled.Smartphone
            "landmark" -> Icons.Filled.AccountBalance
            "bitcoin" -> Icons.Filled.CurrencyBitcoin
            "home" -> Icons.Filled.Home
            "plane" -> Icons.Filled.Flight
            "music" -> Icons.Filled.MusicNote
            "movie" -> Icons.Filled.Movie
            "book" -> Icons.Filled.MenuBook
            "dumbbell" -> Icons.Filled.FitnessCenter
            "coffee" -> Icons.Filled.LocalCafe
            "wifi" -> Icons.Filled.Wifi
            "phone" -> Icons.Filled.PhoneAndroid
            "gift" -> Icons.Filled.CardGiftcard
            "pets" -> Icons.Filled.Pets
            "person" -> Icons.Filled.Person
            else -> Icons.AutoMirrored.Filled.ReceiptLong
        }

    fun colorFor(hex: String?): Color =
        hex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
            ?: Color(0xFF6B7280)
}

/**
 * Diálogo reutilizable: rejilla de iconos + fila de colores. Se usa en los
 * formularios de categoría y origen.
 */
@Composable
fun CatalogIconPickerDialog(
    selectedIcon: String,
    selectedColor: String,
    onIconPick: (String) -> Unit,
    onColorPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.catalog_picker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                ) {
                    items(CatalogIcons.iconNames) { name ->
                        val isSelected = name == selectedIcon
                        Icon(
                            imageVector = CatalogIcons.vectorFor(name),
                            contentDescription = name,
                            tint = CatalogIcons.colorFor(selectedColor),
                            modifier =
                                Modifier
                                    .padding(6.dp)
                                    .size(32.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        CircleShape,
                                    ).border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape,
                                    ).clickable { onIconPick(name) }
                                    .padding(4.dp),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatalogIcons.palette.forEach { hex ->
                        val isSelected = hex.equals(selectedColor, ignoreCase = true)
                        Box(
                            modifier =
                                Modifier
                                    .size(if (isSelected) 30.dp else 24.dp)
                                    .background(CatalogIcons.colorFor(hex), CircleShape)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape,
                                    ).clickable { onColorPick(hex) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_ok)) }
        },
    )
}

/** Píldora con icono + color de la categoría/origen, para listas. */
@Composable
fun CatalogIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = CatalogIcons.colorFor(colorHex).copy(alpha = 0.15f),
        modifier = modifier,
    ) {
        Icon(
            imageVector = CatalogIcons.vectorFor(iconName),
            contentDescription = null,
            tint = CatalogIcons.colorFor(colorHex),
            modifier =
                Modifier
                    .padding(6.dp)
                    .size(20.dp),
        )
    }
}
