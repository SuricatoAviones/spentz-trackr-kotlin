package io.github.suricatoaviones.spentztrackr.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Normalización de la URL del servidor (docs/08, F1): casos reales de lo que
 * un usuario pega desde el navegador o escribe a mano.
 */
class ServerUrlNormalizerTest {
    @Test
    fun `entrada vacia o de espacios es invalida`() {
        assertNull(ServerUrlNormalizer.normalize(""))
        assertNull(ServerUrlNormalizer.normalize("   "))
    }

    @Test
    fun `url irrecuperable es invalida`() {
        assertNull(ServerUrlNormalizer.normalize("http://"))
        assertNull(ServerUrlNormalizer.normalize("https://??"))
    }

    @Test
    fun `dominio sin esquema asume https`() {
        assertEquals(
            "https://gastos.example.com",
            ServerUrlNormalizer.normalize("gastos.example.com"),
        )
    }

    @Test
    fun `https se conserva y http tambien`() {
        assertEquals(
            "https://gastos.example.com",
            ServerUrlNormalizer.normalize("https://gastos.example.com"),
        )
        assertEquals(
            "http://gastos.example.com",
            ServerUrlNormalizer.normalize("http://gastos.example.com"),
        )
    }

    @Test
    fun `se descartan rutas pegadas del navegador`() {
        assertEquals(
            "https://gastos.example.com",
            ServerUrlNormalizer.normalize("https://gastos.example.com/public"),
        )
        assertEquals(
            "https://gastos.example.com",
            ServerUrlNormalizer.normalize("gastos.example.com/api/v1"),
        )
        assertEquals(
            "http://localhost:8080",
            ServerUrlNormalizer.normalize("http://localhost:8080/login"),
        )
    }

    @Test
    fun `host con puerto se conserva completo`() {
        assertEquals(
            "http://192.168.1.10:8080",
            ServerUrlNormalizer.normalize("http://192.168.1.10:8080"),
        )
    }

    @Test
    fun `espacios y barra final se limpian`() {
        assertEquals(
            "https://gastos.example.com",
            ServerUrlNormalizer.normalize("  gastos.example.com/ "),
        )
    }
}
