package io.github.suricatoaviones.spentztrackr.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Tests del store de settings (docs/08, F1): persistencia del modo/servidor
 * y limpieza. DataStore sobre archivo temporal — 100% JVM.
 */
class SettingsStoreTest {
    private lateinit var file: File
    private lateinit var store: SettingsStore

    @Before
    fun setUp() {
        file = File.createTempFile("settings", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        store = SettingsStore(dataStore)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `estado inicial sin onboarding y sin modo`() =
        runBlocking {
            val initial = store.snapshot()

            assertFalse(initial.onboardingCompleted)
            assertNull(initial.mode)
            assertNull(initial.serverUrl)
            assertNull(initial.serverEmail)
        }

    @Test
    fun `modo servidor y onboarding persisten y se leen del flow`() =
        runBlocking {
            store.setMode(AppMode.REMOTE)
            store.setServer("https://gastos.example.com", "user@example.com")
            store.setOnboardingCompleted()

            store.settings.test {
                val settings = awaitItem()

                assertTrue(settings.onboardingCompleted)
                assertEquals(AppMode.REMOTE, settings.mode)
                assertEquals("https://gastos.example.com", settings.serverUrl)
                assertEquals("user@example.com", settings.serverEmail)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `clearServer borra la sesion sin tocar el resto`() =
        runBlocking {
            store.setMode(AppMode.REMOTE)
            store.setServer("https://gastos.example.com", "user@example.com")
            store.clearServer()

            val settings = store.snapshot()

            assertNull(settings.serverUrl)
            assertNull(settings.serverEmail)
            // El modo no se toca: clearServer solo borra la sesión (docs/04).
            assertEquals(AppMode.REMOTE, settings.mode)
        }
}
