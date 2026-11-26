package io.github.suricatoaviones.spentztrackr.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.util.DateText
import io.github.suricatoaviones.spentztrackr.core.util.MoneyFormat
import io.github.suricatoaviones.spentztrackr.domain.model.BudgetProgress
import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardCategory
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovement
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovementType
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardSection
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardTotals
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import io.github.suricatoaviones.spentztrackr.feature.catalog.CatalogIcons
import java.math.RoundingMode

/**
 * Inicio (docs/06): totales del mes en la moneda de visualización, presupuesto
 * global y por categoría, mini-gráfico de gasto por categoría y últimos
 * movimientos. Estados vacío/carga/error incluidos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenIncome: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.nav_home))
                        Text(
                            text = DateText.formatMonth(uiState.month),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.trackingType == TrackingType.INCOME) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.incomes_new)) },
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    onClick = onAddIncome,
                )
            } else {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.expenses_new)) },
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    onClick = onAddExpense,
                )
            }
        },
    ) { padding ->
        when {
            uiState.loading ->
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

            uiState.error ->
                ErrorState(
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(padding),
                )

            uiState.dashboard == null || uiState.dashboard?.isEmpty == true ->
                EmptyState(
                    trackingType = uiState.trackingType,
                    onAddExpense = onAddExpense,
                    onAddIncome = onAddIncome,
                    modifier = Modifier.padding(padding),
                )

            else ->
                DashboardContent(
                    uiState = uiState,
                    onOpenExpense = onOpenExpense,
                    onOpenIncome = onOpenIncome,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: HomeUiState,
    onOpenExpense: (Long) -> Unit,
    onOpenIncome: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dashboard = uiState.dashboard ?: return
    val latestMovements =
        when (uiState.trackingType) {
            TrackingType.EXPENSES -> dashboard.expenses.recent
            TrackingType.INCOME -> dashboard.incomes.recent
            TrackingType.BOTH -> dashboard.expenses.recent + dashboard.incomes.recent
        }.sortedWith(compareByDescending<DashboardMovement> { it.date }.thenByDescending { it.id })
            .take(10)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
    ) {
        item(key = "totals") {
            MonthTotalsCard(
                dashboard = dashboard,
                displayCurrency = uiState.displayCurrency,
                trackingType = uiState.trackingType,
            )
        }

        if (uiState.trackingType != TrackingType.INCOME) {
            item(key = "budget") {
                GlobalBudgetCard(
                    budget = uiState.globalBudget,
                    spent = dashboard.expenses.totals.usd,
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        val categorySection =
            if (uiState.trackingType == TrackingType.INCOME) dashboard.incomes else dashboard.expenses
        if (categorySection.categories.isNotEmpty()) {
            item(key = "categories") {
                CategoryBarsCard(
                    section = categorySection,
                    isIncome = uiState.trackingType == TrackingType.INCOME,
                )
            }
        }

        if (dashboard.expenses.budgets.isNotEmpty() && uiState.trackingType != TrackingType.INCOME) {
            item(key = "category-budgets") {
                CategoryBudgetsCard(dashboard.expenses.budgets)
            }
        }

        if (latestMovements.isNotEmpty()) {
            item(key = "recent-title") {
                Text(
                    text = stringResource(R.string.home_recent_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            items(latestMovements, key = { "${it.type}-${it.id}" }) { movement ->
                MovementRow(
                    movement = movement,
                    onClick = {
                        if (movement.type == DashboardMovementType.EXPENSE) {
                            onOpenExpense(movement.id)
                        } else {
                            onOpenIncome(movement.id)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MonthTotalsCard(
    dashboard: Dashboard,
    displayCurrency: DisplayCurrency,
    trackingType: TrackingType,
) {
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
                text = DateText.formatMonth(dashboard.month),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (trackingType != TrackingType.INCOME) {
                    TotalsBlock(
                        label = stringResource(R.string.home_expenses),
                        totals = dashboard.expenses.totals,
                        displayCurrency = displayCurrency,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (trackingType != TrackingType.EXPENSES) {
                    TotalsBlock(
                        label = stringResource(R.string.home_incomes),
                        totals = dashboard.incomes.totals,
                        displayCurrency = displayCurrency,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsBlock(
    label: String,
    totals: DashboardTotals,
    displayCurrency: DisplayCurrency,
    modifier: Modifier = Modifier,
) {
    val primary =
        when (displayCurrency) {
            DisplayCurrency.USD -> MoneyFormat.withCurrency(totals.usd, CurrencyCode.USD)
            DisplayCurrency.USDT -> MoneyFormat.withCurrency(totals.usdt, CurrencyCode.USDT)
        }
    val secondary =
        when (displayCurrency) {
            DisplayCurrency.USD -> MoneyFormat.withCurrency(totals.usdt, CurrencyCode.USDT)
            DisplayCurrency.USDT -> MoneyFormat.withCurrency(totals.usd, CurrencyCode.USD)
        }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        )
        Text(
            text = primary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = secondary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GlobalBudgetCard(
    budget: Money?,
    spent: Money,
    onOpenSettings: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_budget_title),
                style = MaterialTheme.typography.titleSmall,
            )
            if (budget == null) {
                Text(
                    text = stringResource(R.string.home_budget_none_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.home_budget_set))
                }
            } else {
                val ratio = ratio(spent, budget)
                val exceeded = spent > budget
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string.home_budget_spent_of,
                                MoneyFormat.withCurrency(spent, CurrencyCode.USD),
                                MoneyFormat.withCurrency(budget, CurrencyCode.USD),
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${(ratio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    color =
                        when {
                            exceeded -> MaterialTheme.colorScheme.error
                            ratio >= 0.8f -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(8.dp),
                )
                Text(
                    text =
                        if (exceeded) {
                            stringResource(
                                R.string.home_budget_exceeded,
                                MoneyFormat.withCurrency(spent - budget, CurrencyCode.USD),
                            )
                        } else {
                            stringResource(
                                R.string.home_budget_remaining,
                                MoneyFormat.withCurrency(budget - spent, CurrencyCode.USD),
                            )
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (exceeded) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryBarsCard(
    section: DashboardSection,
    isIncome: Boolean,
) {
    val total = section.totals.usd
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text =
                    stringResource(
                        if (isIncome) R.string.home_income_categories_title else R.string.home_categories_title,
                    ),
                style = MaterialTheme.typography.titleSmall,
            )
            section.categories.forEach { category ->
                CategoryBarRow(
                    category = category,
                    share = ratio(category.total, total),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryBarRow(
    category: DashboardCategory,
    share: Float,
    modifier: Modifier = Modifier,
) {
    val color = CatalogIcons.colorFor(category.color)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = CatalogIcons.vectorFor(category.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = MoneyFormat.withCurrency(category.total, CurrencyCode.USD),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LinearProgressIndicator(
            progress = { share.coerceIn(0f, 1f) },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp),
        )
    }
}

@Composable
private fun CategoryBudgetsCard(budgets: List<BudgetProgress>) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_category_budgets_title),
                style = MaterialTheme.typography.titleSmall,
            )
            budgets.forEach { budget ->
                BudgetRow(budget = budget, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun BudgetRow(
    budget: BudgetProgress,
    modifier: Modifier = Modifier,
) {
    val color =
        when {
            budget.exceeded -> MaterialTheme.colorScheme.error
            budget.ratio >= 0.8f -> MaterialTheme.colorScheme.tertiary
            else -> CatalogIcons.colorFor(budget.color)
        }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = budget.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    stringResource(
                        R.string.home_budget_spent_of,
                        MoneyFormat.withCurrency(budget.spent, CurrencyCode.USD),
                        MoneyFormat.withCurrency(budget.amount, CurrencyCode.USD),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = if (budget.exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { budget.ratio },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp),
        )
    }
}

@Composable
private fun MovementRow(
    movement: DashboardMovement,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .background(CatalogIcons.colorFor(movement.categoryColor), CircleShape),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text = movement.description,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta =
                listOf(movement.categoryName, DateText.formatShort(movement.date))
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = MoneyFormat.withCurrency(movement.amount, movement.currency),
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (movement.type == DashboardMovementType.INCOME) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            if (movement.currency != CurrencyCode.USD) {
                Text(
                    text = MoneyFormat.withCurrency(movement.usdAmount, CurrencyCode.USD),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    trackingType: TrackingType,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.home_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = if (trackingType == TrackingType.INCOME) onAddIncome else onAddExpense,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.home_empty_action))
            }
        }
    }
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.home_error),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

private fun ratio(
    part: Money,
    total: Money,
): Float =
    if (total.value.signum() <= 0) {
        0f
    } else {
        part.value.divide(total.value, 4, RoundingMode.HALF_UP).toFloat()
    }
