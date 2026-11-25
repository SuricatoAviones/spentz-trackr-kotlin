package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.util.DateText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

/**
 * Filtros del listado (docs/06): mes (últimos 12 + "todos"), categoría y
 * moneda. Se aplica en vivo al tocar cada chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesFilterSheet(
    uiState: ExpensesUiState,
    onMonthChange: (YearMonth?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onCurrencyChange: (CurrencyCode?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilterSection(stringResource(R.string.expenses_filter_month)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    FilterChip(
                        selected = uiState.filter.month == null,
                        onClick = { onMonthChange(null) },
                        label = { Text(stringResource(R.string.expenses_filter_all)) },
                    )
                    recentMonths(uiState.currentMonth, count = 12).forEach { month ->
                        FilterChip(
                            selected = uiState.filter.month == month,
                            onClick = { onMonthChange(month) },
                            label = { Text(DateText.formatMonth(month)) },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            FilterSection(stringResource(R.string.expenses_filter_category)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    FilterChip(
                        selected = uiState.filter.categoryId == null,
                        onClick = { onCategoryChange(null) },
                        label = { Text(stringResource(R.string.expenses_filter_all)) },
                    )
                    uiState.categories.forEach { category ->
                        FilterChip(
                            selected = uiState.filter.categoryId == category.id,
                            onClick = { onCategoryChange(category.id) },
                            label = { Text(category.name) },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            FilterSection(stringResource(R.string.expenses_filter_currency)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options: List<CurrencyCode?> =
                        listOf(null, CurrencyCode.USD, CurrencyCode.VES, CurrencyCode.USDT)
                    options.forEachIndexed { index, currency ->
                        SegmentedButton(
                            selected = uiState.filter.currency == currency,
                            onClick = { onCurrencyChange(currency) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        ) {
                            Text(currency?.value?.uppercase() ?: stringResource(R.string.expenses_filter_all))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        content()
    }
}

/** Últimos N meses, del más reciente al más antiguo. */
private fun recentMonths(
    current: YearMonth,
    count: Int,
): List<YearMonth> = (0 until count).map { current.minus(it, DateTimeUnit.MONTH) }
