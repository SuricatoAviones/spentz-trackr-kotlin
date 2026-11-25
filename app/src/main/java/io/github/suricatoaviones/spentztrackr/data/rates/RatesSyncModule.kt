package io.github.suricatoaviones.spentztrackr.data.rates

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.suricatoaviones.spentztrackr.data.remote.DolarApiClient
import io.github.suricatoaviones.spentztrackr.data.remote.DolarApiSource
import java.util.concurrent.TimeUnit

/** Programa el worker de tasas (~6 h, red no medida) y expone dolarapi. */
@Module
@InstallIn(SingletonComponent::class)
object RatesSyncModule {
    @Provides
    fun provideDolarApiSource(client: DolarApiClient): DolarApiSource = client

    fun schedulePeriodicSync(workManager: WorkManager) {
        val request =
            PeriodicWorkRequestBuilder<RatesSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                        .build(),
                ).build()

        workManager.enqueueUniquePeriodicWork(
            RatesSyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
