package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/** Fila de `ve.dolarapi.com/v1/dolares`. `fuente = "oficial"` ⇒ BCV. */
@Serializable
data class DolarApiRow(
    val moneda: String? = null,
    val fuente: String? = null,
    val nombre: String? = null,
    val compra: Double? = null,
    val venta: Double? = null,
    val promedio: Double? = null,
    val fechaActualizacion: String? = null,
)

/** Cliente HTTP aparte para dolarapi — sin auth, sin base URL dinámica. */
interface DolarApi {
    @GET("v1/dolares")
    suspend fun dolares(): List<DolarApiRow>
}

/** Abstracción para testear el sync sin red (docs/08 F5). */
interface DolarApiSource {
    suspend fun fetchTodayRates(): List<ExchangeRate>
}

/**
 * Sincroniza tasas BCV y Paralelo desde `ve.dolarapi.com` — el equivalente
 * móvil del job `SyncExchangeRates` del backend (docs/05, ADR-002).
 *
 * Formato verificado contra la API real: lista de objetos con
 * `moneda`/`fuente`/`promedio` (`fuente = "oficial"` ⇒ BCV). Se tolera
 * también el formato antiguo `{"usd":{"bcv":…}}` por compatibilidad.
 */
@Singleton
class DolarApiClient
    @Inject
    constructor() : DolarApiSource {
        private val json =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }

        private val api: DolarApi =
            Retrofit
                .Builder()
                .baseUrl("https://ve.dolarapi.com/")
                .client(
                    OkHttpClient
                        .Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build(),
                ).addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(DolarApi::class.java)

        /**
         * @return las tasas BCV y Paralelo del día; vacío si la API no responde.
         * Nunca lanza: el llamador decide qué hacer sin tasas frescas.
         */
        override suspend fun fetchTodayRates(): List<ExchangeRate> =
            try {
                api.dolares().mapNotNull { row -> row.toExchangeRate() }
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) throw t
                emptyList()
            }
    }

/** Mapea una fila de dolarapi a la tasa del día (`oficial` ⇒ BCV). */
internal fun DolarApiRow.toExchangeRate(today: LocalDate = currentDate()): ExchangeRate? {
    val provider =
        when (fuente?.lowercase()) {
            "oficial", "bcv" -> RateProvider.BCV
            "paralelo" -> RateProvider.PARALELO
            else -> return null
        }
    val value =
        promedio
            ?: listOfNotNull(compra, venta).takeIf { it.isNotEmpty() }?.average()
            ?: return null
    if (value <= 0.0) return null
    val date =
        fechaActualizacion
            ?.take(10)
            ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
            ?: today
    return ExchangeRate(
        source = RateSource.API,
        provider = provider,
        rate = Rate.parse(BigDecimal.valueOf(value).toPlainString()),
        rateDate = date,
    )
}

private fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
