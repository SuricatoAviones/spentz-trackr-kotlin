package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.dto.toDomain
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.DashboardRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeFilter
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.domain.service.DashboardAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dashboard local (docs/08 F6): toma los movimientos de los últimos 6 meses
 * (Room filtra por fecha), agrega en BigDecimal con [DashboardAggregator] y
 * emite el mismo [Dashboard] que el modo remoto.
 */
@Singleton
class LocalDashboardRepository
    @Inject
    constructor(
        @LocalSource private val expenses: ExpenseRepository,
        @LocalSource private val incomes: IncomeRepository,
        @LocalSource private val categories: CategoryRepository,
        @LocalSource private val sources: PaymentSourceRepository,
        private val aggregator: DashboardAggregator,
    ) : DashboardRepository {
        override fun observe(month: YearMonth): Flow<Dashboard> {
            val from = month.minus(TREND_MONTHS_OFFSET, DateTimeUnit.MONTH).firstDay
            val to = month.lastDay
            return combine(
                expenses.observe(ExpenseFilter(from = from, to = to)),
                incomes.observe(IncomeFilter(from = from, to = to)),
                categories.observe(),
                sources.observe(),
            ) { expenseList, incomeList, categoryList, sourceList ->
                aggregator.build(
                    month = month,
                    expenses = expenseList,
                    incomes = incomeList,
                    categories = categoryList,
                    sources = sourceList,
                )
            }
        }

        private companion object {
            /** 6 meses de tendencia: 5 anteriores + el actual. */
            const val TREND_MONTHS_OFFSET = 5L
        }
    }

/** Dashboard remoto: `GET /dashboard` ya trae los totales calculados. */
@Singleton
class RemoteDashboardRepository
    @Inject
    constructor(
        private val api: SpentzApi,
    ) : DashboardRepository {
        override fun observe(month: YearMonth): Flow<Dashboard> =
            flow {
                val data = api.getDashboard().data ?: error("Respuesta de dashboard sin datos")
                emit(data.toDomain(month))
            }
    }

@Singleton
class DelegatingDashboardRepository
    @Inject
    constructor(
        @LocalSource private val local: DashboardRepository,
        @RemoteSource private val remote: DashboardRepository,
        private val settings: SettingsStore,
    ) : DashboardRepository {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(month: YearMonth): Flow<Dashboard> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe(month) else local.observe(month)
                }
    }
