package io.github.suricatoaviones.spentztrackr.feature.incomes

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.suricatoaviones.spentztrackr.R
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.util.DateText
import io.github.suricatoaviones.spentztrackr.core.util.MoneyFormat
import io.github.suricatoaviones.spentztrackr.domain.model.Income
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

/** Lista de ingresos (docs/06): como gastos, sin origen en las filas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    onAddIncome: () -> Unit,
    onOpenIncome: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.incomes_title)) },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = stringResource(R.string.incomes_filter_open),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.incomes_new)) },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                onClick = onAddIncome,
            )
        },
    ) { padding ->
        when {
            uiState.loading ->
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            uiState.error ->
                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.incomes_error),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(onClick = viewModel::retry, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            uiState.groups.isEmpty() ->
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.incomes_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.incomes_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            else -> IncomesList(uiState, onOpenIncome, Modifier.padding(padding))
        }
    }

    if (showFilters) {
        IncomesFilterSheet(
            uiState = uiState,
            onMonthChange = viewModel::setMonth,
            onCategoryChange = viewModel::setCategory,
            onCurrencyChange = viewModel::setCurrency,
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun IncomesList(
    uiState: IncomesUiState,
    onOpenIncome: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "totals") {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.incomes_month_total),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.incomes_totals, uiState.monthTotalUsd, uiState.monthTotalUsdt),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        uiState.groups.forEach { group ->
            item(key = "header-${group.date}") {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = DateText.formatLong(group.date),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.incomes_day_total, group.dayTotalUsd),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(group.entries, key = { it.id }) { income ->
                IncomeRow(
                    income = income,
                    categoryName =
                        uiState.categories
                            .firstOrNull { it.id == income.categoryId }
                            ?.name
                            .orEmpty(),
                    onClick = { onOpenIncome(income.id) },
                )
            }
        }
    }
}

@Composable
private fun IncomeRow(
    income: Income,
    categoryName: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text = income.description,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (categoryName.isNotBlank()) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = MoneyFormat.withCurrency(income.amount, income.currency),
                style = MaterialTheme.typography.titleMedium,
            )
            if (income.currency != CurrencyCode.USD) {
                Text(
                    text = "$" + income.usdAmount.toCanonicalString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomesFilterSheet(
    uiState: IncomesUiState,
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
            FilterSection(stringResource(R.string.incomes_filter_month)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    FilterChip(
                        selected = uiState.filter.month == null,
                        onClick = { onMonthChange(null) },
                        label = { Text(stringResource(R.string.expenses_filter_all)) },
                    )
                    (0 until 12).forEach { offset ->
                        val month = uiState.currentMonth.minus(offset, DateTimeUnit.MONTH)
                        FilterChip(
                            selected = uiState.filter.month == month,
                            onClick = { onMonthChange(month) },
                            label = { Text(DateText.formatMonth(month)) },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            FilterSection(stringResource(R.string.incomes_filter_category)) {
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
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    FilterChip(
                        selected = uiState.filter.currency == null,
                        onClick = { onCurrencyChange(null) },
                        label = { Text(stringResource(R.string.expenses_filter_all)) },
                    )
                    CurrencyCode.entries.forEach { currency ->
                        FilterChip(
                            selected = uiState.filter.currency == currency,
                            onClick = { onCurrencyChange(currency) },
                            label = { Text(currency.value.uppercase()) },
                            modifier = Modifier.padding(start = 8.dp),
                        )
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
