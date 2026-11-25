package io.github.suricatoaviones.spentztrackr

import io.github.suricatoaviones.spentztrackr.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Smoke test de la Fase 0: verifica que la infraestructura de tests (JUnit,
 * task `test`) está operativa. Los tests reales (dominio del dinero) llegan
 * en la Fase 2.
 */
class RoutesTest {
    @Test
    fun homeRouteIsStable() {
        assertEquals("home", Routes.HOME)
    }
}
