package io.github.suricatoaviones.spentztrackr

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.github.suricatoaviones.spentztrackr.data.rates.RatesSyncModule
import javax.inject.Inject

/**
 * Application de Spentz Trackr.
 *
 * `@HiltAndroidApp` genera el componente raíz de Hilt. Todas las dependencias
 * (DataStore, Room, Retrofit) se cablean aquí a partir de la Fase 1.
 *
 * Desde la Fase 5 también es el `Configuration.Provider` de WorkManager: así
 * el worker de tasas recibe sus dependencias por Hilt (`HiltWorkerFactory`).
 */
@HiltAndroidApp
class SpentzApplication :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        // Programa el refresco periódico de tasas (~6 h). El worker decide si
        // aplica (solo modo local) y sale sin hacer nada en remoto.
        RatesSyncModule.schedulePeriodicSync(WorkManager.getInstance(this))
    }
}
