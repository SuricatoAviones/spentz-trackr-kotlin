package io.github.suricatoaviones.spentztrackr.data.remote

import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Reescribe scheme/host/puerto de cada request con la URL del servidor que
 * el usuario guardó en settings — la URL es decisión de runtime, no de
 * compilación (docs/05). Corre en hilos de OkHttp: el bloqueo es aceptable.
 */
class DynamicBaseUrlInterceptor
    @Inject
    constructor(
        private val settings: SettingsStore,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val configured = runBlocking { settings.snapshot().serverUrl } ?: return chain.proceed(request)
            val baseUrl = configured.toHttpUrlOrNull() ?: return chain.proceed(request)

            val newUrl =
                request.url
                    .newBuilder()
                    .scheme(baseUrl.scheme)
                    .host(baseUrl.host)
                    .port(baseUrl.port)
                    .build()

            return chain.proceed(request.newBuilder().url(newUrl).build())
        }
    }
