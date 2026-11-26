package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ApiEnvelope
import io.github.suricatoaviones.spentztrackr.data.remote.dto.DashboardDataDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.toDomain
import io.github.suricatoaviones.spentztrackr.domain.model.DashboardMovementType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** Shape real de `GET /dashboard` del backend (DashboardController). */
class DashboardMappingTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Test
    fun `mapea totales categorias presupuestos y recientes del backend`() {
        val envelope = json.decodeFromString<ApiEnvelope<DashboardDataDto>>(SAMPLE)
        val dashboard = envelope.data!!.toDomain(YearMonth(2026, 11))

        assertEquals(
            "25.50",
            dashboard.expenses.totals.usd
                .toCanonicalString(),
        )
        assertEquals(
            "25.50",
            dashboard.expenses.totals.usdt
                .toCanonicalString(),
        )
        assertEquals(
            "280.00",
            dashboard.expenses.totals.byCurrency[CurrencyCode.VES]
                ?.toCanonicalString(),
        )

        val category = dashboard.expenses.categories.first()
        assertEquals(1L, category.id)
        assertEquals("Comida", category.name)
        assertEquals("20.50", category.total.toCanonicalString())
        assertEquals(2, category.count)

        val budget = dashboard.expenses.budgets.single()
        assertEquals("100.00", budget.amount.toCanonicalString())
        assertEquals("20.50", budget.spent.toCanonicalString())

        val recent = dashboard.expenses.recent.first()
        assertEquals(DashboardMovementType.EXPENSE, recent.type)
        assertEquals("Almuerzo", recent.description)
        assertEquals(LocalDate.parse("2026-11-25"), recent.date)
        assertEquals(CurrencyCode.USD, recent.currency)

        assertEquals(
            "110.50",
            dashboard.incomes.totals.usd
                .toCanonicalString(),
        )
        assertEquals(2, dashboard.incomes.trend.size)
        assertEquals(
            YearMonth(2026, 11),
            dashboard.incomes.trend
                .last()
                .month,
        )
    }

    private companion object {
        val SAMPLE =
            """
            {
              "success": true,
              "data": {
                "mode": "both",
                "month": "noviembre 2026",
                "expenses": {
                  "totals": { "usd": 25.5, "usdt": 25.5, "byCurrency": { "usd": 10, "ves": 280, "usdt": 5 } },
                  "categories": [
                    { "id": 1, "name": "Comida", "color": "#FF8800", "icon": "coffee", "total": 20.5, "count": 2 },
                    { "id": 2, "name": "Transporte", "color": "#0088FF", "icon": "car", "total": 5, "count": 1 }
                  ],
                  "sources": [
                    { "id": 5, "name": "Efectivo", "color": "#333333", "icon": "wallet", "total": 25.5 }
                  ],
                  "trend": [
                    { "month": "2026-10", "total": 40 },
                    { "month": "2026-11", "total": 25.5 }
                  ],
                  "budgets": [
                    { "id": 1, "name": "Comida", "icon": "coffee", "color": "#FF8800", "amount": 100, "spent": 20.5 }
                  ],
                  "recent": [
                    {
                      "id": 1,
                      "description": "Almuerzo",
                      "amount": "10.00",
                      "currency": "usd",
                      "usd_amount": "10.00",
                      "spent_at": "2026-11-25",
                      "category": { "id": 1, "name": "Comida", "icon": "coffee", "color": "#FF8800" }
                    }
                  ]
                },
                "incomes": {
                  "totals": { "usd": 110.5, "usdt": 110.5, "byCurrency": { "usd": 100, "ves": 280, "usdt": 0 } },
                  "categories": [],
                  "trend": [
                    { "month": "2026-10", "total": 80 },
                    { "month": "2026-11", "total": 110.5 }
                  ],
                  "recent": [
                    {
                      "id": 7,
                      "description": "Salario",
                      "amount": 100,
                      "currency": "usd",
                      "usd_amount": 100,
                      "received_at": "2026-11-20",
                      "category": { "id": 10, "name": "Salario", "icon": "wallet", "color": "#00AA55" }
                    }
                  ]
                }
              },
              "message": null
            }
            """.trimIndent()
    }
}
