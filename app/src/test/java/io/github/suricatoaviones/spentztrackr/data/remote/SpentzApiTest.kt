package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.data.remote.dto.ExpenseRequestDto
import io.github.suricatoaviones.spentztrackr.data.remote.dto.LoginRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * El cliente contra respuestas reales del backend (MockWebServer), con las
 * dos formas que manda Laravel: números (`"amount": 150.50`) y strings
 * (`"exchange_rate": "28.5000"`). Los requests serializan decimales como
 * texto canónico.
 */
class SpentzApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: SpentzApi

    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/api/v1/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(SpentzApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `login parsea el envelope con usuario`() =
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"success":true,"data":{"token":"1|abc","token_id":7,
                      "user":{"id":1,"name":"Luis","email":"luis@example.com","is_admin":false,
                      "locale":"es","tracking_type":"both","created_at":"2026-01-01 10:00:00"}},
                      "message":"Inicio de sesión exitoso"}
                    """.trimIndent(),
                ),
            )

            val response = api.login(LoginRequest(email = "luis@example.com", password = "secret", deviceName = "android"))

            assertEquals(true, response.success)
            assertEquals("1|abc", response.data?.token)
            assertEquals("luis@example.com", response.data?.user?.email)
            assertEquals("both", response.data?.user?.trackingType)
        }

    @Test
    fun `listExpenses parsea numeros y strings indistintamente`() =
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"success":true,"data":{
                      "expenses":[{
                        "id":1,"description":"Mercado semanal","note":"Supermercado",
                        "amount":150.50,"currency":"ves","payment_method":"pago_movil",
                        "commission":14.00,"exchange_rate":"28.5000","rate_provider":"bcv",
                        "usd_amount":5.78,"usdt_amount":"5.78","spent_at":"2026-11-25",
                        "category":{"id":1,"name":"Alimentación","icon":"shopping-cart","color":"#10B981"},
                        "source":{"id":1,"name":"Efectivo","icon":"banknote","color":"#F59E0B"},
                        "has_receipt":false,
                        "items":[{"id":1,"currency":"usd","amount":5,"exchange_rate":null,
                                  "usd_amount":5,"usdt_amount":5}]
                      }],
                      "totals":{"usd":5.78,"usdt":5.78,"byCurrency":{"usd":5.78,"ves":0,"usdt":5.78}},
                      "filters":{"search":"","currency":"","category_id":null},
                      "categories":[],"sources":[]
                    }}
                    """.trimIndent(),
                ),
            )

            val response = api.listExpenses(null, null, null, null, null)
            val expense = response.data?.expenses?.first()

            assertEquals("150.50", expense?.amount?.toPlainString())
            assertEquals("28.5000", expense?.exchangeRate?.toPlainString())
            assertEquals("5.78", expense?.usdAmount?.toPlainString())
            assertEquals("5.78", expense?.usdtAmount?.toPlainString())
            assertEquals(1, expense?.items?.size)
            assertEquals("2026-11-25", expense?.spentAt)
        }

    @Test
    fun `createExpense envia decimales como texto canonico y parsea el id`() =
        runBlocking {
            server.enqueue(MockResponse().setBody("""{"success":true,"data":{"id":2},"message":"Gasto creado"}"""))

            val request =
                ExpenseRequestDto(
                    categoryId = 1L,
                    paymentSourceId = 1L,
                    currency = "ves",
                    paymentMethod = "pago_movil",
                    commission = null,
                    amount = java.math.BigDecimal("25.50"),
                    exchangeRate = java.math.BigDecimal("28.5"),
                    rateProvider = "bcv",
                    description = "Mercado",
                    note = null,
                    spentAt = "2026-11-25",
                    items = null,
                )

            val response = api.createExpense(request)

            assertEquals(2L, response.data?.id)
            val body = server.takeRequest().body.readUtf8()
            assertTrue(body.contains("\"amount\":\"25.50\""))
            assertTrue(body.contains("\"exchange_rate\":\"28.5\""))
            assertTrue(body.contains("\"category_id\":1"))
            // explicitNulls = false: los null no viajan.
            assertTrue(!body.contains("\"note\""))
            assertTrue(!body.contains("\"commission\""))
        }

    @Test
    fun `getRates parsea el snapshot de tasas`() =
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {"success":true,"data":{"rates":{"bcv":"30.5000","paralelo":"32.1000","manual":"29.7500"}}}
                    """.trimIndent(),
                ),
            )

            val rates = api.getRates().data?.rates

            assertEquals("30.5000", rates?.get("bcv"))
            assertEquals("32.1000", rates?.get("paralelo"))
            assertEquals("29.7500", rates?.get("manual"))
        }
}
