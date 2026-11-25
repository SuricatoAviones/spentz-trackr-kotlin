package io.github.suricatoaviones.spentztrackr.data.rates

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository

/**
 * Worker de WorkManager (~6 h): mantiene el histórico de tasas BCV/Paralelo
 * al día — el equivalente móvil del scheduler del backend (docs/05).
 *
 * Solo opera en modo local: en remoto la app delega la sincronización al
 * servidor (`POST /rates/sync`) y no llama a dolarapi directamente.
 */
@HiltWorker
class RatesSyncWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val settings: SettingsStore,
        private val rates: RateRepository,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val mode = settings.snapshot().mode ?: return Result.success()
            if (mode != AppMode.LOCAL) return Result.success()
            return if (rates.syncNow()) Result.success() else Result.retry()
        }

        companion object {
            const val UNIQUE_NAME = "rates-sync"
        }
    }
