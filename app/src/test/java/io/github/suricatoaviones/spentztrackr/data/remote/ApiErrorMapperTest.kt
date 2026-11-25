package io.github.suricatoaviones.spentztrackr.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Mapeo de errores HTTP del backend a [ApiError] (docs/08, F1) con los shapes
 * reales: Laravel 422 `{message, errors}` y `apiError()` `{success, message}`.
 */
class ApiErrorMapperTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = ApiErrorMapper(json)
    private val contentType = "application/json".toMediaType()

    @Test
    fun `ioexception mapea a network`() {
        assertEquals(ApiError.Network, mapper.map(IOException("sin red")))
    }

    @Test
    fun `422 de laravel mapea a validation con errores por campo`() {
        val body =
            """
            {"message":"Estas credenciales no coinciden.","errors":{"email":["Estas credenciales no coinciden."]}}
            """.trimIndent().toResponseBody(contentType)
        val error = mapper.map(HttpException(Response.error<Any>(422, body)))

        assertTrue(error is ApiError.Validation)
        val validation = error as ApiError.Validation
        assertEquals(listOf("Estas credenciales no coinciden."), validation.fieldErrors["email"])
    }

    @Test
    fun `403 mapea a suspended con el mensaje del backend`() {
        val body =
            """{"success":false,"message":"Tu cuenta está suspendida."}"""
                .trimIndent()
                .toResponseBody(contentType)
        val error = mapper.map(HttpException(Response.error<Any>(403, body)))

        assertTrue(error is ApiError.Suspended)
        assertEquals("Tu cuenta está suspendida.", (error as ApiError.Suspended).message)
    }

    @Test
    fun `429 mapea a throttled`() {
        val error = mapper.map(HttpException(Response.error<Any>(429, "[]".toResponseBody(contentType))))

        assertEquals(ApiError.Throttled, error)
    }

    @Test
    fun `otros codigos mapean a http con mensaje cuando se puede leer`() {
        val error500 =
            mapper.map(
                HttpException(Response.error<Any>(500, """{"message":"boom"}""".toResponseBody(contentType))),
            )
        assertEquals(ApiError.Http(500, "boom"), error500)

        val error404 =
            mapper.map(
                HttpException(Response.error<Any>(404, "<html>404</html>".toResponseBody(contentType))),
            )
        assertEquals(ApiError.Http(404, null), error404)
    }
}
