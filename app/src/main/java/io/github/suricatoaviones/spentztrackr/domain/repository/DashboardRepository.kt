package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.domain.model.Dashboard
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/**
 * Dashboard en cualquier modo (ADR-A01): local agrega los movimientos del mes
 * de Room; remoto consume `GET /dashboard` (el backend ya calcula los totales).
 */
interface DashboardRepository {
    fun observe(month: YearMonth): Flow<Dashboard>
}
